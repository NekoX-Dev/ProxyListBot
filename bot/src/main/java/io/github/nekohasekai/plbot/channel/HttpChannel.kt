package io.github.nekohasekai.plbot.channel

import cn.hutool.json.JSONUtil
import io.github.nekohasekai.plbot.Fetcher
import io.github.nekohasekai.plbot.parser.JSONParser
import io.github.nekohasekai.plbot.parser.StringParser
import io.github.nekohasekai.plbot.proxy.Proxy
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response

abstract class HttpChannel(val debug: Boolean = false) : Channel {

    companion object {

        fun create(name: String, link: String, debug: Boolean = false): HttpChannel {

            return create(name, link.toHttpUrl(), debug)

        }

        fun create(name: String, link: HttpUrl, debug: Boolean = false): HttpChannel {

            return create(name, Request.Builder()
                    .url(link)
                    .build(), debug)

        }

        fun create(name: String, request: Request, debug: Boolean = false) = object : HttpChannel(debug) {

            override val name = name

            override fun buildRequest(): Request {

                return request

            }

        }

    }

    abstract fun buildRequest(): Request

    fun getRequest(url: String) = getRequest(url.toHttpUrl())
    fun getRequest(url: HttpUrl) = Request.Builder().url(url).build()

    open fun parseResponse(response: Response): Collection<Proxy> {

        val body = response.body!!

        if (debug) println(body)

        val string = body.string()

        if (debug) println(string)

        try {

            body.contentType()?.also {

                if (it.subtype == "json") {

                    return JSONParser.parseProxies(JSONUtil.parse(string))

                }

            }

            return StringParser.parseProxies(string)

        } catch (e: Exception) {

            e.printStackTrace()

            throw e

        }

    }

    override fun fetchProxies(): Collection<Proxy> {

        repeat(4) { index ->

            if (index > 0) println("重试第 $index 次.")

            runCatching {

                return parseResponse(Fetcher.okHttpClient.newCall(buildRequest()).execute())

            }.onFailure {

                if (index == 3) throw it

            }

        }

        error("频道无法拉取")

    }

}