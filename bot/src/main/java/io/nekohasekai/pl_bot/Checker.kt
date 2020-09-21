@file:Suppress("ConstantConditionIf")

package io.nekohasekai.pl_bot

import cn.hutool.core.date.SystemClock
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
import io.nekohasekai.td.proxy.tester.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.or
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Checker : TdClient() {

    init {

        MTProtoImpl.init()

        MTProtoTester.onLoad(this)

        testDcTarget = 5
        testTimeout = 10.0

    }

    override fun onLoad() {

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

                ProxyEntity.find { (status neq INVALID) or (failedCount less 3) }.toMutableLinkedList()
//                ProxyEntity.find { (status neq AVAILABLE) }.toMutableLinkedList()
            }

        }

        val totalCount = proxies.size

        val threads = 32

        val exec = newSingleThreadContext("Proxy Checker")

        val index = AtomicInteger()

        repeat(threads) {

            GlobalScope.launch(exec) {

                while (proxies.isNotEmpty()) {

                    val entity = synchronized(proxies) { proxies.remove() }

                    val start = SystemClock.now()

                    try {

                        val ping = ProxyTester.testProxy(entity.proxy)

                        val i = index.incrementAndGet()

                        val end = (SystemClock.now() - start) / 1000L

                        println("[${i}/$totalCount][$end] ${entity.proxy}: 可用, ${ping}ms.")

                        database.write {

                            entity.status = AVAILABLE
                            entity.failedCount = 0
                            entity.message = "$ping"

                            entity.flush()

                        }

                    } catch (e: TdException) {

                        val i = index.incrementAndGet()

                        val end = (SystemClock.now() - start) / 1000L

                        println("[${i}/$totalCount][$end] ${entity.proxy}: ${e.message}.")

                        database.write {

                            entity.status = INVALID
                            entity.message = e.message
                            entity.failedCount += 1

                            entity.flush()

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