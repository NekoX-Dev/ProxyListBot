@file:Suppress("ConstantConditionIf")

package io.nekohasekai.pl_bot

import cn.hutool.core.io.FileUtil
import io.nekohasekai.ktlib.core.toMutableLinkedList
import io.nekohasekai.ktlib.td.core.*
import io.nekohasekai.pl_bot.database.ProxyEntities
import io.nekohasekai.pl_bot.database.ProxyEntities.AVAILABLE
import io.nekohasekai.pl_bot.database.ProxyEntities.INVALID
import io.nekohasekai.pl_bot.database.ProxyEntity
import io.nekohasekai.td.proxy.impl.Proxy
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoImpl
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoTester
import io.nekohasekai.td.proxy.impl.shadowsocks.testDcTarget
import io.nekohasekai.td.proxy.tester.ProxyTester
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.or
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Checker : TdClient() {


    init {

        MTProtoImpl.init()

        MTProtoTester.onLoad(this)

        testDcTarget = 5

        options databaseDirectory "data/checker"

        FileUtil.del(options.databaseDirectory)

    }

    @ObsoleteCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        initDatabase("proxy_list.db", "data")

        waitForStart()

        val proxies = database {

            with(ProxyEntities) {

                ProxyEntity.find { (status neq INVALID) or (failedCount less 4) }.toMutableLinkedList()

            }

        }

        val totalCount = proxies.size

        val threads = 16

        val exec = Executors.newFixedThreadPool(threads)

        val index = AtomicInteger()

        val sqlThread = Executors.newSingleThreadExecutor()

        repeat(threads) {

            exec.execute {

                runBlocking {

                    while (proxies.isNotEmpty()) {

                        val entity = synchronized(proxies) { proxies.remove() }

                        try {

                            val ping = ProxyTester.testProxy(entity.proxy, 1)

                            val i = index.incrementAndGet()

                            println("[${i}/$totalCount] ${entity.proxy}: 可用, ${ping}ms.")

                            sqlThread.execute {

                                database {

                                    entity.status = AVAILABLE
                                    entity.failedCount = 0
                                    entity.message = "$ping"

                                    entity.flush()

                                }

                            }

                        } catch (e: TdException) {

                            val i = index.incrementAndGet()

                            println("[${i}/$totalCount] ${entity.proxy}: ${e.message}.")

                            sqlThread.execute {

                                database {

                                    entity.status = INVALID
                                    entity.message = e.message
                                    entity.failedCount += 1

                                    entity.flush()

                                }

                            }

                        }

                    }

                }

            }

        }

        while (index.get() < totalCount) delay(1000L)

        waitForClose()

        exitProcess(0)

    }

    class ExportItem(val proxy: Proxy, val link: String, val ping: Int) : Comparable<ExportItem> {

        override fun compareTo(other: ExportItem): Int {

            if (ping == other.ping) return link.compareTo(other.link)

            return ping - other.ping

        }

    }

}