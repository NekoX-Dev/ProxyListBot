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

/**
 * 代理检查脚本
 *
 * 需要国内网络环境
 */
object ProxyChecker : TdClient() {

    init {

        // 数据文件目录
        options databaseDirectory "data/checker"

    }

    // 重新检查 (跳过超时无效记录)
    const val recheck = false

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        val count = mkTimeCount()

        // 加载 TDLib
        TdLoader.tryLoad()

        // 等待启动
        waitForStart()

        count.printTime("启动完成")

        // 删除已有代理 ( 但手动删除数据文件目录也可 )
        removeAllProxies()

        count.printTime("重置代理列表")

        // 读入代理
        val proxyArray = proxyList.toMutableSet()

        val size = proxyArray.size

        // 读入无效记录
        val invalidMap = invalidList

        // 删去无效记录
        if (!recheck) {

            proxyArray.removeAll(invalidMap.keys)

        } else {

            proxyArray.removeAll(invalidMap.filter { !(it.value.contains("timeout") || it.value.contains("closed")) }.keys)

        }

        count.printTime("打开完成")

        val proxyDict = hashMapOf<String, TdApi.Proxy>()

        // 添加代理到 TDLib 使用异步
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

        // 测速
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

                        // 代理无效 保存无效记录

                        invalidMap[origin] = error.message

                        if (invalidMap.size % 20 == 0) {

                            // 二十条无效记录写入一次

                            invalidList = invalidMap

                        }

                    }

                    println("[${current + 1}/${proxyDict.size}] $server: ${error.message}")

                })

            }

        }.awaitAll()

        // 保存无效记录
        invalidList = invalidMap

        count.printTime("测速完成, 共: ${size}, 可用: ${resultArray.size}")

        // 写出代理列表
        outputList = resultArray.map { it.url }

        waitForClose()

        exitProcess(0)

    }

    // 排序
    class PingResult(
            val url: String,
            val ping: Int
    ) : Comparable<PingResult> {

        override fun compareTo(other: PingResult) = (ping - other.ping).takeIf { it != 0 } ?: url.compareTo(other.url)

    }

}