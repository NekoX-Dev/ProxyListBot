@file:Suppress("ConstantConditionIf")

package io.nekohasekai.pl_bot

import cn.hutool.core.codec.Base64
import io.nekohasekai.ktlib.td.cli.TdCli
import io.nekohasekai.ktlib.td.core.TdLoader
import io.nekohasekai.pl_bot.database.ProxyEntities
import io.nekohasekai.pl_bot.database.ProxyEntities.AVAILABLE
import io.nekohasekai.pl_bot.database.ProxyEntity
import io.nekohasekai.td.proxy.impl.Proxy
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoImpl
import io.nekohasekai.td.proxy.saver.LinkSaver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import td.TdApi
import java.io.File
import java.util.*
import kotlin.system.exitProcess

object Exporter : TdCli() {

    init {

        MTProtoImpl.init()

    }

    override suspend fun onAuthorizationState(authorizationState: TdApi.AuthorizationState) {
        if (authorizationState is TdApi.AuthorizationStateWaitPhoneNumber) return
        super.onAuthorizationState(authorizationState)
    }

    @ObsoleteCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        initDatabase("proxy_list.db", "data")

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