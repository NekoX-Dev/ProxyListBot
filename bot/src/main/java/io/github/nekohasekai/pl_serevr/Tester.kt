package io.github.nekohasekai.pl_serevr

import cn.hutool.core.io.FileUtil
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoTester
import io.github.nekohasekai.nekolib.proxy.impl.shadowsocks.ShadowsocksImpl
import io.github.nekohasekai.nekolib.proxy.impl.shadowsocks.ShadowsocksTester
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

object Tester : TdClient() {

    init {

        MTProtoImpl.init()
        ShadowsocksImpl.init()

        MTProtoTester.onLoad(this)
        ShadowsocksTester.onLoad(this)

        options databaseDirectory "data/checker"

        FileUtil.del(options.databaseDirectory)

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        val resp = HttpUtil.post("https://mtproxyer.pw/api.php", "method=apishowapp%2FMainList")

        println(JSONObject(resp).toStringPretty())

//        waitForStart()

        // some tests

//        val kg = ChannelMyProxy//createHttpChannels().first { it.name == "CityPlus" }
//
//        kg.fetchProxies().toMutableSet().apply {
//
//            iterator().apply {
//
//                forEach {
//
//                    if (Fetcher.exists.contains(it.toString())) {
//
//                        remove()
//
//                    }
//
//                }
//
//            }
//
//            forEach {
//
//                println(it)
//
//                ProxyDatabase.table.insert(ProxyEntity().apply { proxy = it })
//
//            }
//
//        }
//
//        waitForClose()

        exitProcess(0)

    }

}