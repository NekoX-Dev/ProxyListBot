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

abstract class HttpChannel : Channel {

    companion object {

        fun create(name: String, link: String): HttpChannel {

            return create(name, link.toHttpUrl())

        }

        fun create(name: String, link: HttpUrl): HttpChannel {

            return create(name, Request.Builder()
                    .url(link)
                    .build())

        }

        fun create(name: String, request: Request) = object : HttpChannel() {

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

        body.contentType()?.also {

            if (it.subtype == "json") {

                return JSONParser.parseProxies(JSONUtil.parse(body.string()))

            }

        }

        val responseStr = response.body!!.string()

        return StringParser.parseProxies(responseStr)

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