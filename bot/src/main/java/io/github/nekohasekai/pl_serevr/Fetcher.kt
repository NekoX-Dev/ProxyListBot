package io.github.nekohasekai.pl_serevr

import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.utils.invoke
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoProxy
import io.github.nekohasekai.pl_serevr.channel.Channel
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelFlameProxy
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelTeleVpn
import io.github.nekohasekai.pl_serevr.channel.impl.createHttpChannels
import io.github.nekohasekai.pl_serevr.channel.impl.createTelegramChannels
import io.github.nekohasekai.pl_serevr.database.ChatByName
import io.github.nekohasekai.pl_serevr.database.ProxyEntities
import io.github.nekohasekai.pl_serevr.database.ProxyEntities.UNCHECKED
import io.github.nekohasekai.pl_serevr.database.ProxyEntity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.jetbrains.exposed.sql.SchemaUtils
import java.util.*
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

    val exists = hashSetOf<String>()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        initDatabase("../proxy_list.db")

        database {

            SchemaUtils.create(ProxyEntities, ChatByName)

        }

        waitForLogin()

        println("开始拉取.")

        val proxies = hashSetOf<Proxy>()

        proxies.addAll(database {

            ProxyEntity.all().map { it.proxy.apply { exists.add(toString()) } }

        })

        val deferreds = LinkedList<Deferred<Unit>>()

        channels.forEach { channel ->

            fun runFetch() {

                runCatching {

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

                            it as MTProtoProxy

                            database {

                                ProxyEntity.new {

                                    proxy = it
                                    status = UNCHECKED
                                    failedCount = 0
                                    message = null

                                }

                            }

                        }

                    })

                    synchronized(this@Fetcher) {

                        print("${channel.name}: ")

                        println("代理数量: $size, 不重复数量: ${proxies.size - before}.")

                    }

                }.onFailure {

                    it.printStackTrace()

                    println("出错")

                }

            }

            if (channel.async) {

                deferreds.add(GlobalScope.async(Dispatchers.IO) { runFetch() })

            } else {

                deferreds.awaitAll()

                runFetch()

            }

        }

        waitForClose()

        exitProcess(0)

    }

}