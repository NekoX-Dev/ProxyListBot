@file:Suppress("ConstantConditionIf")

package io.nekohasekai.pl_bot

import cn.hutool.core.codec.Base64
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
import io.nekohasekai.td.proxy.saver.LinkSaver
import io.nekohasekai.td.proxy.tester.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.jetbrains.exposed.sql.or
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Checker : TdClient() {

    init {

        MTProtoImpl.init()

        MTProtoTester.onLoad(this)

        testDcTarget = 5

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

        val exec = newSingleThreadContext("Proxy Checker")

        suspend fun doCheck(proxyList: LinkedList<ProxyEntity>, threads: Int) {

            val totalCount = proxyList.size

            val index = AtomicInteger()
            val finishLock = Mutex(true)

            repeat(threads) {

                GlobalScope.launch(exec) {

                    while (proxyList.isNotEmpty()) {

                        val entity = synchronized(proxyList) { proxyList.remove() }

                        val start = SystemClock.now()

                        val i = try {

                            val ping = ProxyTester.testProxy(entity.proxy)

                            database.write {

                                entity.status = AVAILABLE
                                entity.failedCount = 0
                                entity.message = "$ping"

                                entity.flush()

                            }

                            val i = index.incrementAndGet()

                            val end = (SystemClock.now() - start) / 1000L

                            println("[${i}/$totalCount][$end] ${entity.proxy}: 可用, ${ping}ms.")

                            i

                        } catch (e: TdException) {

                            database.write {

                                entity.status = INVALID
                                entity.message = e.message
                                entity.failedCount += 1

                                entity.flush()

                            }

                            val i = index.incrementAndGet()

                            val end = (SystemClock.now() - start) / 1000L

                            println("[${i}/$totalCount][$end] ${entity.proxy}: ${e.message}.")

                            i

                        }

                        if (i == totalCount) finishLock.unlock()

                    }

                }

            }

            finishLock.lock()

        }

        val proxies = database {

            with(ProxyEntities) {

                ProxyEntity.find { (status neq INVALID) or (failedCount less 3) }.toMutableLinkedList()
//                ProxyEntity.find { (status neq AVAILABLE) }.toMutableLinkedList()
            }

        }

        val availableProxies = proxies.filter { it.status != INVALID }.toMutableLinkedList()

        proxies.removeAll(availableProxies)

        testTimeout = 10.0

        doCheck(proxies, if (proxies.size > 500) 64 else 32)

        testTimeout = 15.0

        doCheck(availableProxies, 16)

        val siMap = hashMapOf<String, ProxyEntity>()

        database {

            ProxyEntity.find { ProxyEntities.status eq AVAILABLE }.toList().forEach {

                siMap[it.proxy.strictKey()] = it

            }

        }

        val node = siMap.values.map { ExportItem(it.proxy, LinkSaver.toLink(it.proxy), it.message!!.toInt()) }.let { TreeSet(it) }

        println("可用: ${node.size}, 正在输出.")

        File("proxy_list_output").writeText(node.joinToString("\n") { it.link }.let { Base64.encode(it) })

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