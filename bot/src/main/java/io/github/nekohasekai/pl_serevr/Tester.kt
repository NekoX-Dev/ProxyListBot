package io.github.nekohasekai.pl_serevr

import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.raw.getChat
import io.github.nekohasekai.nekolib.core.raw.getChats
import io.github.nekohasekai.nekolib.core.raw.setLogVerbosityLevel
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoTester
import io.github.nekohasekai.nekolib.proxy.impl.shadowsocks.ShadowsocksImpl
import io.github.nekohasekai.nekolib.proxy.impl.shadowsocks.ShadowsocksTester
import kotlinx.coroutines.runBlocking
import td.TdApi

object Tester : TdCli() {

    override val loginType = LoginType.USER

    init {

        MTProtoImpl.init()
        ShadowsocksImpl.init()

        MTProtoTester.onLoad(this)
        ShadowsocksTester.onLoad(this)

        options databaseDirectory "data"

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

       // setLogVerbosityLevel(5)

        start()

    }

    override suspend fun onLogin() {

        super.onLogin()

       val chatIds = getChats(TdApi.ChatListMain(), 2 xor 63 - 1,0,100).apply {

           println(this)

       }.chatIds

        chatIds.forEach {

            val c = getChat(it)

            print(c.title)

            val s = c.notificationSettings

            println(": $s")

        }

    }

}