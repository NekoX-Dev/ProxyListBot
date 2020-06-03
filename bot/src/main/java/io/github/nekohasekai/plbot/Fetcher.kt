package io.github.nekohasekai.plbot

import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.raw.setLogStream
import io.github.nekohasekai.nekolib.core.raw.setLogVerbosityLevel
import io.github.nekohasekai.plbot.channel.Channel
import io.github.nekohasekai.plbot.channel.impl.ChannelTeleVpn
import io.github.nekohasekai.plbot.channel.impl.createHttpChannels
import io.github.nekohasekai.plbot.channel.impl.createTelegramChannels
import io.github.nekohasekai.plbot.database.ProxyDatabase
import io.github.nekohasekai.plbot.database.ProxyEntity
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import td.TdApi
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Fetcher : TdCli() {

    override val loginType = LoginType.USER

    init {

        // 数据文件目录
        options databaseDirectory "data/fetcher"

        // 不要缓存聊天 否则 getChatHistory 一次之后只会返回缓存
        // 还没试过有没有用 建议每次都删除 *.sqlite
        // binlog 是认证信息 其他都是缓存
        options useChatInfoDatabase false

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

        channels.addAll(createHttpChannels())
        channels.add(ChannelTeleVpn)
        channels.addAll(createTelegramChannels().map { it.apply { onLoad(this@Fetcher) } })

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        waitForLogin()

        println("开始拉取, 已有: ${ProxyDatabase.table.find().totalCount()}.")

        val proxies = hashSetOf<Proxy>()

        val exists = ProxyDatabase.table.find().map { it.proxy }.toMutableSet()

        channels.forEach { channel ->

            runCatching {

                println("搜索频道: ${channel.name}")

                val before = proxies.size
                val size: Int

                proxies.addAll(channel.fetchProxies().also {

                    size = it.size

                }.toMutableSet().apply {

                    removeAll(exists)

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