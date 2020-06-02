package io.github.nekohasekai.plbot

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.plbot.database.ProxyDatabase
import io.github.nekohasekai.plbot.database.ProxyEntity
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.saver.LinkSaver
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.objects.filters.ObjectFilters
import java.io.File
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

        val totalCount = available.totalCount()

        println("可用: $totalCount, 正在输出.")

        File("proxy_list_output.json").writeText(JSONArray().apply {

            siMap.values.forEach {

                add(JSONObject().apply {

                    set("proxy",LinkSaver.toLink(it.proxy))
                    set("desc","")

                })


            }

        }.toString())

        exitProcess(0)

    }

}