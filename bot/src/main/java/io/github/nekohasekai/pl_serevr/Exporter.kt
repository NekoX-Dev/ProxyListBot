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

object Exporter : TdClient() {

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

        val siMap = hashMapOf<String, ProxyEntity>()

        database {

            ProxyEntity.find { ProxyEntities.status eq AVAILABLE }.toList().forEach {

                siMap[it.proxy.strictKey()] = it

            }

        }

        val node = siMap.values.map { ExportItem(it.proxy, LinkSaver.toLink(it.proxy), it.message!!.toInt()) }.let { TreeSet(it) }

        println("可用: ${node.size}, 正在输出.")

        File("proxy_list_output").writeText(node.joinToString("\n") { it.link }.let { Base64.encode(it) })

        exitProcess(0)

    }

    class ExportItem(val proxy: Proxy, val link: String, val ping: Int) : Comparable<ExportItem> {

        override fun compareTo(other: ExportItem): Int {

            if (ping == other.ping) return link.compareTo(other.link)

            return ping - other.ping

        }

    }

}