package io.github.nekohasekai.plbot

import cn.hutool.core.codec.Base64
import cn.hutool.core.date.DateUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.plbot.database.ProxyDatabase
import io.github.nekohasekai.plbot.database.ProxyEntity
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoProxy
import io.github.nekohasekai.plbot.saver.LinkSaver
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.objects.filters.ObjectFilters
import java.io.File
import java.text.DateFormat
import java.util.*
import kotlin.system.exitProcess

object Exporter {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        MTProtoImpl.init()

        val available = ProxyDatabase.table.find(ObjectFilters.eq("status", ProxyEntity.AVAILABLE))

        val all = available.totalCount()

        val siMap = hashMapOf<String, ProxyEntity>()

        available.toList().forEach {

            siMap[it.proxy.strictKey()] = it

        }

        val node = siMap.values.map { ExportItem(it.proxy, LinkSaver.toLink(it.proxy), it.message!!.toInt()) }.let { TreeSet(it) }

        println("所有: $all, 可用: ${node.size}, 正在输出.")

        // 旧格式

        var mdText = "# ProxyList\n\n由 [ProxyListBot](https://github.com/NekoX-Dev/ProxyListBot) 自动采集, 自动获取完整列表请使用 [Nekogram X](https://github.com/NekoX-Dev/NekoX) :)\n"

        (if (node.size > 50) node.toList().subList(0, 50) else node).forEachIndexed { index, item ->

            mdText += "\n${index + 1}. [${item.proxy}](${item.link})  "
            val proxy = item.proxy
            if (proxy is MTProtoProxy) {
                mdText += "\n  **服务器**: `${proxy.server}`  "
                mdText += "\n  **端口**: `${proxy.port}`  "
                mdText += "\n  **密钥**: `${proxy.secret}`  "
            }
            mdText += "\n"

        }

        val time = DateUtil.formatChineseDate(Date(), false)

        mdText += "\n\n上次更新时间: $time"

        File("proxy_list_output.md").writeText(mdText)

        File("proxy_list_output.json").writeText(JSONArray().apply {

            node.toList().subList(0, 30).forEach {

                add(JSONObject().apply {

                    set("proxy", it.proxy)
                    set("desc", "")

                })

            }

        }.toString())

        File("proxy_list_output").writeText(node.joinToString("\n") { it.link }.let { Base64.encode(it) })

        exitProcess(0)

    }

    class ExportItem(val proxy: Proxy, val link: String, val ping: Int) : Comparable<ExportItem> {

        override fun compareTo(other: ExportItem): Int {

            if (ping == other.ping) return proxy.compareTo(other.proxy)

            return ping - other.ping

        }

    }

}