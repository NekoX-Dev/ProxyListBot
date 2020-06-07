package io.github.nekohasekai.plbot.proxy.shadowsocks

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.RuntimeUtil
import cn.hutool.core.util.ZipUtil
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.plbot.parser.LinkParser
import io.github.nekohasekai.plbot.tester.ProxyTester
import okhttp3.Request
import java.io.File

object ShadowsocksImpl {

    const val ssRustVersion = "1.8.12"

    val target = TdLoader.NativeTarget.current()

    val ssExecutable =  File("libs/shadowsocks/sslocal${if (target == TdLoader.NativeTarget.Win64) ".exe" else ""}")

    fun init() {

        if (!ssExecutable.isFile) {

            val url: String = if (target == TdLoader.NativeTarget.Linux &&
                    RuntimeUtil.execForStr("uanme -m") in arrayOf("amd64", "x86_64")
            ) {

                "https://github.com/shadowsocks/shadowsocks-rust/releases/download/v$ssRustVersion/shadowsocks-v$ssRustVersion.x86_64-unknown-linux-gnu.tar.xz"

            } else if (target == TdLoader.NativeTarget.Win64) {

                "https://github.com/shadowsocks/shadowsocks-rust/releases/download/v$ssRustVersion/shadowsocks-v$ssRustVersion.x86_64-pc-windows-msvc.zip"

            } else error("unknown dist")

            val cacheFile = File("libs", FileUtil.getName(url))

            TdLoader.okHttpClient.newCall(Request.Builder()
                    .url(url)
                    .build())
                    .execute().body?.byteStream()?.use {

                        cacheFile.parentFile?.mkdirs()

                        cacheFile.createNewFile()

                        cacheFile.outputStream().use { out -> it.copyTo(out) }

                    }

            val outDir = File("libs/shadowsocks")

            outDir.mkdirs()

            if (cacheFile.extension == "zip") {

                ZipUtil.unzip(cacheFile, outDir)

            } else if (cacheFile.name.endsWith("tar.xz")) {

                RuntimeUtil.exec("tar -O $outDir -xf $cacheFile")

            }

            FileUtil.del(cacheFile)

        }

        LinkParser.addParser(ShadowsocksLinkParser)
        ProxyTester.addTester(ShadowsocksTester)

    }

}