package io.github.nekohasekai.plbot

import cn.hutool.core.codec.Base64
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.plbot.database.ProxyDatabase
import io.github.nekohasekai.plbot.database.ProxyEntity
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.saver.LinkSaver
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.objects.filters.ObjectFilters
import java.io.File
import java.util.*
import kotlin.system.exitProcess

object Exporter {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        MTProtoImpl.init()

        val available = ProxyDatabase.table.find(ObjectFilters.eq("status", ProxyEntity.AVAILABLE))

        val siMap = hashMapOf<String, ProxyEntity>()

        available.toList().forEach {

            siMap[it.toString()] = it

        }

        val node = siMap.values.map { ExportItem(LinkSaver.toLink(it.proxy), it.message!!.toInt()) }.let { TreeSet(it) }

        println("可用: ${node.size}, 正在输出.")

        // 旧格式

        File("proxy_list_output.json").writeText(JSONArray().apply {

            /*

            add(JSONObject().apply {

                set("proxy", "https://t.me/socks?server=127.0.0.1&port=1080#PLEASE UPDATE TO LATEST VERSION")
                set("desc", "")

            })

            */

            node.forEach {

                add(JSONObject().apply {

                    set("proxy", it.proxy)
                    set("desc", "")

                })


            }

        }.toString())

        File("proxy_list_output").writeText(node.joinToString("\n") { it.proxy }.let { Base64.encode(it) })

        exitProcess(0)

    }

    class ExportItem(val proxy: String, val ping: Int) : Comparable<ExportItem> {

        override fun compareTo(other: ExportItem): Int {

            if (ping == other.ping) return proxy.compareTo(other.proxy)

            return ping - other.ping

        }

    }

}