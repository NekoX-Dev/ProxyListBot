package io.github.nekohasekai.plbot

import io.github.nekohasekai.plbot.channel.impl.createHttpChannels
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoImpl
import io.github.nekohasekai.plbot.saver.LinkSaver
import kotlin.system.exitProcess

object Tester {

    @JvmStatic
    fun main(args: Array<String>) {

        MTProtoImpl.init()

        // to whatever u want

//        val flyChat = createHttpChannels().filter { it.name == "FlyChat"; }[0]
//
//        println(flyChat.fetchProxies().joinToString("\n") { LinkSaver.toLink("tg",it) })

        exitProcess(0)

    }

}