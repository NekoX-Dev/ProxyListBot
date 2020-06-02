package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.addProxy
import io.github.nekohasekai.nekolib.core.raw.pingProxy
import io.github.nekohasekai.plbot.tester.ProxyTester
import td.TdApi

object MTProtoTester : TdHandler(), ProxyTester<MTProtoProxy> {

    override suspend fun testProxy(proxy: MTProtoProxy): Int {

        val proxyId = addProxy(proxy.server, proxy.port, false, TdApi.ProxyTypeMtproto(proxy.secret)).id

        return (pingProxy(proxyId).seconds * 100).toInt()

    }

}