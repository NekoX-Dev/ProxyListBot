@file:Suppress("ConstantConditionIf")

package io.github.nekohasekai.pl_serevr

import cn.hutool.core.codec.Base64
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.utils.invoke
import io.github.nekohasekai.nekolib.core.utils.toMutableLinkedList
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoProxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoTester
import io.github.nekohasekai.nekolib.proxy.saver.LinkSaver
import io.github.nekohasekai.nekolib.proxy.tester.ProxyTester
import io.github.nekohasekai.pl_serevr.database.ProxyEntities
import io.github.nekohasekai.pl_serevr.database.ProxyEntities.AVAILABLE
import io.github.nekohasekai.pl_serevr.database.ProxyEntities.INVALID
import io.github.nekohasekai.pl_serevr.database.ProxyEntity
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.or
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Checker : TdClient() {


    init {

        MTProtoImpl.init()

        MTProtoTester.onLoad(this)

        options databaseDirectory "data/checker"

        FileUtil.del(options.databaseDirectory)

    }

    @ObsoleteCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        initDatabase("../proxy_list.db")

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