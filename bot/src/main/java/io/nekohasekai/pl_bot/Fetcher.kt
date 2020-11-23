package io.nekohasekai.pl_bot

import io.nekohasekai.ktlib.td.cli.TdCli
import io.nekohasekai.ktlib.td.core.TdLoader
import io.nekohasekai.pl_bot.channel.Channel
import io.nekohasekai.pl_bot.channel.impl.*
import io.nekohasekai.pl_bot.database.*
import io.nekohasekai.pl_bot.database.ProxyEntities.UNCHECKED
import io.nekohasekai.td.proxy.impl.Proxy
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoImpl
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoProxy
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.jetbrains.exposed.sql.SchemaUtils
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Fetcher : TdCli() {

    override val loginType = LoginType.USER

    override fun onLoad() {

        options databaseDirectory "data/fetcher"

        options apiId 971882
        options apiHash "1232533dd027dc2ec952ba91fc8e3f27"

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

        initDatabase("proxy_list.db", "data")

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

                    val before: Int
                    val size: Int

                    proxies.addAll(channel.fetchProxies().also {

                        before = proxies.size
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

                    println("出错 (${channel.name}): ")

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