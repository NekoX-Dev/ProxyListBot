@file:Suppress("ConstantConditionIf")

package io.github.nekohasekai.pl_serevr

import cn.hutool.core.io.FileUtil
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.utils.toMutableLinkedList
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoTester
import io.github.nekohasekai.nekolib.proxy.tester.ProxyTester
import io.github.nekohasekai.pl_serevr.database.ProxyDatabase
import io.github.nekohasekai.pl_serevr.database.ProxyEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.objects.filters.ObjectFilters
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Checker : TdClient() {

    // 0: 从未测试中测试
    // 1: 从可用中重新测试
    // 2: 从不可用中重新测试
    // 3: 未测试 / 可用
    // 4: 全部
    const val mode = 3

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

        waitForStart()

        val proxies = ProxyDatabase.table.find(when (mode) {
            0 -> ObjectFilters.eq("status", ProxyEntity.UNCHECKED)
            1 -> ObjectFilters.eq("status", ProxyEntity.AVAILABLE)
            2 -> ObjectFilters.eq("status", ProxyEntity.INVALID)
            3 -> ObjectFilters.or(ObjectFilters.eq("status", ProxyEntity.UNCHECKED), ObjectFilters.eq("status", ProxyEntity.AVAILABLE))
            4 -> ObjectFilters.ALL
            else -> error("no this mode")
        }).toMutableLinkedList()

        val totalCount = proxies.size

        val threads = 16

        val exec = Executors.newFixedThreadPool(threads)

        val index = AtomicInteger()

        repeat(threads) {

            exec.execute {

                runBlocking {

                    while (proxies.isNotEmpty()) {

                        val entity = synchronized(proxies) { proxies.remove() }

                        try {

                            val ping = ProxyTester.testProxy(entity.proxy, if (mode in arrayOf(1, 3)) 1 else 0)

                            val i = index.incrementAndGet()

                            println("[${i}/$totalCount] ${entity.proxy}: 可用, ${ping}ms.")

                            entity.status = ProxyEntity.AVAILABLE
                            entity.message = "$ping"

                            ProxyDatabase.table.update(entity)

                            continue

                        } catch (e: TdException) {

                            val i = index.incrementAndGet()

                            println("[${i}/$totalCount] ${entity.proxy}: ${e.message}.")

                            entity.status = ProxyEntity.INVALID
                            entity.message = e.message

                            ProxyDatabase.table.update(entity)

                        }

                    }

                }

            }

        }

        while (index.get() < totalCount) delay(1000L)

        waitForClose()

        exitProcess(0)

    }

}