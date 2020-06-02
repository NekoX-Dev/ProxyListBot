package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.plbot.parser.LinkParser
import io.github.nekohasekai.plbot.parser.MapParser
import io.github.nekohasekai.plbot.proxy.ProxyImplement
import io.github.nekohasekai.plbot.saver.LinkSaver
import io.github.nekohasekai.plbot.tester.ProxyTester

object MTProtoImpl : ProxyImplement {

    override fun init() {

        LinkParser.addParser(MTProtoLinkParser)
        LinkSaver.addSaver(MTProtoLinkSaver)
        MapParser.addParser(MTProtoMapParser)
        ProxyTester.addTester(MTProtoTester)

    }

}