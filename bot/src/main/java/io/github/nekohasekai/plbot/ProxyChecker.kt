package io.github.nekohasekai.plbot

import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.utils.mkAsync
import io.github.nekohasekai.nekolib.core.utils.mkTimeCount
import io.github.nekohasekai.nekolib.core.utils.parseProxyLink
import io.github.nekohasekai.nekolib.core.utils.removeAllProxies
import kotlinx.coroutines.runBlocking
import td.TdApi
import java.util.*
import kotlin.system.exitProcess

object ProxyChecker : TdClient() {

    init {

        options databaseDirectory "data/checker"

    }

    const val recheck = false

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        val count = mkTimeCount()

        TdLoader.tryLoad()

        waitForStart()

        count.printTime("启动完成")

        removeAllProxies()

        count.printTime("重置代理列表")

        val proxyArray = proxyList.toMutableSet()

        val size = proxyArray.size

        val invalidMap = invalidList

        if (!recheck) {

            proxyArray.removeAll(invalidMap.keys)

        } else {

            proxyArray.removeAll(invalidMap.filter { !(it.value.contains("timeout") || it.value.contains("closed")) }.keys)

        }

        count.printTime("打开完成")

        val proxyDict = hashMapOf<String, TdApi.Proxy>()

        mkAsync<TdApi.Proxy>().apply {

            proxyArray.forEach { link ->

                add(link.parseProxyLink(), { _, proxy ->

                    proxyDict[link] = proxy

                }, { _, error ->

                    invalidMap[link] = error.message

                })

            }

        }.awaitAll()

        count.printTime("载入完成")

        val resultArray = TreeSet<PingResult>()

        mkAsync<TdApi.Seconds>(if (proxyDict.size > 1000) 32 else 4).apply {

            proxyDict.forEach { (origin, proxy) ->

                val server = "${proxy.server}:${proxy.port}"

                add(TdApi.PingProxy(proxy.id), { current, result ->

                    val ping = (result.seconds * 100).toInt()

                    invalidMap.remove(origin)

                    resultArray.add(PingResult(origin, ping))

                    println("[${current + 1}/${proxyDict.size}] $server: ${ping}ms")

                }, { current, error ->

                    synchronized(invalidMap) {

                        invalidMap[origin] = error.message

                        if (invalidMap.size % 10 == 0) {

                            invalidList = invalidMap

                        }

                    }

                    println("[${current + 1}/${proxyDict.size}] $server: ${error.message}")

                })

            }

        }.awaitAll()

        invalidList = invalidMap

        count.printTime("测速完成, 共: ${size}, 可用: ${resultArray.size}")

        outputList = resultArray.map { it.url }

        waitForClose()

        exitProcess(0)

    }

    class PingResult(
            val url: String,
            val ping: Int
    ) : Comparable<PingResult> {

        override fun compareTo(other: PingResult) = (ping - other.ping).takeIf { it != 0 } ?: url.compareTo(other.url)

    }

}