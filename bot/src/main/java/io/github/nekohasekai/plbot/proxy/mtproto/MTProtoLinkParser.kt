package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.plbot.parser.LinkParser
import io.github.nekohasekai.plbot.proxy.Proxy
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.URI

object MTProtoLinkParser : LinkParser {

    override val name = "mtproto"

    private val telegramDomains = mutableSetOf(
            "t.me",
            "telegram.me",
            "telegram.dog"
    )

    fun addTelegramDomain(domain: String) = telegramDomains.add(domain)

    override fun parseProxy(link: String): Proxy {

        val scheme = URI.create(link).scheme

        val httpUrl = if (scheme == "tg" || scheme.startsWith("tg:")) {

            link.replace(URI.create(link).scheme + "://","https://").toHttpUrl()

        } else if (scheme == "http" || scheme == "https") {

            link.toHttpUrl().also {

                if (it.host !in telegramDomains) {

                    error("not a telegram domain")

                }

                if (it.encodedPath != "/proxy") {

                    error("path not equal to /proxy")

                }

            }

        } else {

            error("not a mtproto proxy link")

        }

        if (httpUrl.queryParameter("secret") == null) error("no secret in query parameter")

        return MTProtoProxy().apply {

            server = httpUrl.queryParameter("server")!!
            port = httpUrl.queryParameter("port")!!.toInt()
            secret = httpUrl.queryParameter("secret")!!

        }

    }

}