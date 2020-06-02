package io.github.nekohasekai.plbot

import cn.hutool.core.io.FileUtil
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.plbot.database.ProxyDatabase
import io.github.nekohasekai.plbot.database.ProxyEntity
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoTester
import io.github.nekohasekai.plbot.tester.ProxyTester
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.objects.filters.ObjectFilters
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

        waitForStart()

        val unchecked = ProxyDatabase.table.find(ObjectFilters.eq("status", ProxyEntity.UNCHECKED))

        val totalCount = unchecked.totalCount()

        val threads = 9

        val exec = Executors.newFixedThreadPool(threads)

        val iter = unchecked.iterator()

        val index = AtomicInteger()

        repeat(threads) {

            exec.execute {

                runBlocking {

                    while (iter.hasNext()) {

                        val entity = iter.next()

                        for (times in 0 until 3) {

                            try {

                                val ping = ProxyTester.testProxy(entity.proxy)

                                val i = index.incrementAndGet()

                                println("[${i}/$totalCount] ${entity.proxy}: 可用, ${ping}ms.")

                                entity.status = ProxyEntity.AVAILABLE

                                ProxyDatabase.table.update(entity)

                                break

                            } catch (e: TdException) {

                                if (times == 2) {

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

            }

        }

        while (index.get() < totalCount) delay(1000L)

        waitForClose()

        exitProcess(0)

    }

}