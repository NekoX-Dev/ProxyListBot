package io.github.nekohasekai.plbot.proxy.shadowsocks

import cn.hutool.core.codec.Base64
import io.github.nekohasekai.plbot.parser.LinkParser
import io.github.nekohasekai.plbot.proxy.Proxy
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ShadowsocksLinkParser : LinkParser {

    override val name = "shadowsocks"

    override fun parseProxy(link: String): Proxy {

        if (link.contains("@")) {

            // ss-android style

            val url = link.replace("ss://", "https://").toHttpUrlOrNull()
                    ?: error("invalid ss link $link")

            if (url.password.isNotBlank()) {

                return ShadowsocksProxy(url.host,
                        url.port,
                        url.password,
                        url.username,
                        url.queryParameter("plugin") ?: "",
                        url.fragment)

            }

            val methodAndPswd = Base64.decodeStr(url.username)

            return ShadowsocksProxy(url.host,
                    url.port,
                    methodAndPswd.substringAfter(":"),
                    methodAndPswd.substringBefore(":"),
                    url.queryParameter("plugin") ?: "",
                    url.fragment)

        } else {

            // v2rayNG style

            var v2Url = link

            if (v2Url.contains("#")) v2Url = v2Url.substringBefore("#")

            val url = ("https://" + Base64.decodeStr(v2Url.substringAfter("ss://"))).toHttpUrlOrNull() ?: error("invalid v2rayNG link $link")

            return ShadowsocksProxy(url.host,
                    url.port,
                    url.password,
                    url.username,
                    "",
                    url.fragment)


        }

    }

}