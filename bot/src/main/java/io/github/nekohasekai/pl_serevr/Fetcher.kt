package io.github.nekohasekai.pl_serevr

import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.pl_serevr.channel.Channel
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelFlameProxy
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelTeleVpn
import io.github.nekohasekai.pl_serevr.channel.impl.createHttpChannels
import io.github.nekohasekai.pl_serevr.channel.impl.createTelegramChannels
import io.github.nekohasekai.pl_serevr.database.ProxyDatabase
import io.github.nekohasekai.pl_serevr.database.ProxyEntity
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Fetcher : TdCli() {

    override val loginType = LoginType.USER

    init {

        // 数据文件目录
        options databaseDirectory "data/fetcher"

        // getChatHistory 有缓存
        options useMessageDatabase false

    }

    val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    val channels = arrayListOf<Channel>()

    init {

        MTProtoImpl.init()

        channels.add(ChannelTeleVpn)
        channels.add(ChannelFlameProxy)
        // channels.add(ChannelMyProxy)
        channels.addAll(createHttpChannels())
        channels.addAll(createTelegramChannels().map { it.apply { onLoad(this@Fetcher) } })

    }

    val exists = ProxyDatabase.table.find().map { it.proxy.toString() }.toMutableSet()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        waitForLogin()

        println("开始拉取, 已有: ${ProxyDatabase.table.find().totalCount()}.")

        val proxies = hashSetOf<Proxy>()

        channels.forEach { channel ->

            runCatching {

                println("搜索频道: ${channel.name}")

                val before = proxies.size
                val size: Int

                proxies.addAll(channel.fetchProxies().also {

                    size = it.size

                }.toMutableSet().apply {

                    iterator().apply {

                        forEach {

                            if (exists.contains(it.toString())) {

                                remove()

                            }

                        }

                    }

                    forEach {

                        ProxyDatabase.table.insert(ProxyEntity().apply { proxy = it })

                    }

                })

                println("代理数量: $size, 不重复数量: ${proxies.size - before}.")

            }.onFailure {

                it.printStackTrace()

                println("出错")

            }

        }

        waitForClose()

        exitProcess(0)

    }

}