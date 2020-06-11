package io.github.nekohasekai.plbot

import cn.hutool.core.io.FileUtil
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.plbot.channel.impl.ChannelFlameProxy
import io.github.nekohasekai.plbot.channel.impl.createHttpChannels
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoTester
import io.github.nekohasekai.plbot.proxy.shadowsocks.ShadowsocksImpl
import io.github.nekohasekai.plbot.proxy.shadowsocks.ShadowsocksTester
import io.github.nekohasekai.plbot.tester.ProxyTester
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

        waitForStart()

        // some tests

        val kg = createHttpChannels().first { it.name == "CityPlus" }

        kg.fetchProxies().forEach { println(it) }

        waitForClose()

        exitProcess(0)

    }

}