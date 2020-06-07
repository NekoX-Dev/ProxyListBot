package io.github.nekohasekai.plbot.proxy.shadowsocks

import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.addProxy
import io.github.nekohasekai.nekolib.core.raw.pingProxy
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.proxy.mtproto.testPing
import io.github.nekohasekai.plbot.tester.ProxyTester
import kotlinx.coroutines.delay
import td.TdApi
import java.io.File
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.random.Random

abstract class ExternalSocks5Tester<T : Proxy> : TdHandler(),ProxyTester<T> {

    fun mkNewPort(): Int {

        var port = Random.nextInt(2048, 32768)

        if (!isProxyAvailable(port)) {

            port = Random.nextInt(2048, 32768)

        }

        return port

    }

    private fun isProxyAvailable(port: Int): Boolean {

        if (port !in 2048 until 32768) return false

        runCatching {

            val server = ServerSocket()

            server.bind(InetSocketAddress("127.0.0.1", port))

            server.close()

            Thread.sleep(1000L)

        }.onFailure {

            return false

        }

        return true

    }

    fun createProxyProcess(localDir: File,vararg cmd: String): GuardedProcessPool {

        return GuardedProcessPool(localDir) {

            println(it.message)

        }.apply {

            start(cmd.toList(), null)

        }

    }

    suspend fun testPort(port: Int,retryCount: Int): Int {

        val proxyId = addProxy("127.0.0.1",port,false,TdApi.ProxyTypeSocks5()).id

        return testPing(proxyId,retryCount, 100L)

    }

}