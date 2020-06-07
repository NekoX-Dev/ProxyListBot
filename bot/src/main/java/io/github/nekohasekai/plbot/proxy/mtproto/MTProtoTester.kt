package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.addProxy
import io.github.nekohasekai.nekolib.core.raw.pingProxy
import io.github.nekohasekai.plbot.tester.ProxyTester
import kotlinx.coroutines.delay
import td.TdApi

suspend fun TdHandler.testPing(proxyId: Int, retryCount: Int, delay: Long = 0L): Int {

    repeat(retryCount + 1) { index ->

        try {

            return (pingProxy(proxyId).seconds * 100).toInt()

        } catch (e: TdException) {

            if (index == retryCount) {

                throw e

            }

            delay(delay)

        }

    }

    error("unreachable")

}

object MTProtoTester : TdHandler(), ProxyTester<MTProtoProxy> {

    override suspend fun testProxy(proxy: MTProtoProxy, retryCount: Int): Int {

        val proxyId = addProxy(proxy.server, proxy.port, false, TdApi.ProxyTypeMtproto(proxy.secret)).id

        return testPing(proxyId, retryCount)

    }

}