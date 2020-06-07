package io.github.nekohasekai.plbot.proxy.shadowsocks

import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.utils.getFile
import io.github.nekohasekai.nekolib.core.utils.getPath
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.File

object ShadowsocksTester : ExternalSocks5Tester<ShadowsocksProxy>() {

    override suspend fun testProxy(proxy: ShadowsocksProxy, retryCount: Int) = coroutineScope<Int> {

        val cacheDir = File("cache/ss")

        cacheDir.mkdirs()

        val cacheFile = File(cacheDir, "${System.currentTimeMillis()}.json")

        cacheFile.writeText(JSONObject().also {

            it["server"] = proxy.server
            it["server_port"] = proxy.port
            it["password"] = proxy.password
            it["method"] = proxy.method
            it["ipv6"] = true

            if (proxy.plugin.isNotBlank()) {

                it["plugin"] = proxy.plugin
                it["plugin_opts"] = proxy.pluginOpts

            }

        }.toStringPretty())

        val localPort = mkNewPort()

        var process: GuardedProcessPool? = null

        try {

            process = createProxyProcess(
                    getFile("libs/shadowsocks"),
                    ShadowsocksImpl.ssExecutable.canonicalPath,
                    "--local-addr", "127.0.0.1:$localPort",
                    "--config", cacheFile.canonicalPath
            )

            delay(1000L)

            return@coroutineScope testPort(localPort, retryCount)

        } catch (e: Exception) {

            throw TdException(e.message ?: e.javaClass.simpleName)

        } finally {

            process?.close(this)

            cacheFile.delete()

        }

    }

}