package io.nekohasekai.pl_bot.channel

import cn.hutool.json.JSONUtil
import io.nekohasekai.pl_bot.Fetcher
import io.nekohasekai.td.proxy.parser.JSONParser
import io.nekohasekai.td.proxy.parser.StringParser
import io.nekohasekai.td.proxy.impl.Proxy
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

abstract class HttpChannel(val debug: Boolean = false) : Channel {

    override val async = true

    companion object {

        fun create(name: String, link: String,parser: ((Response) -> Collection<Proxy>)?= null, debug: Boolean = false): HttpChannel {

            return create(name, { Request.Builder().url(link).build() },parser, debug)

        }

        fun create(name: String, request: () -> Request,parser: ((Response) -> Collection<Proxy>)? = null, debug: Boolean = false) = object : HttpChannel(debug) {

            override val name = name

            override fun buildRequest(): Request {

                return request()

            }

            override fun parseResponse(response: Response): Collection<Proxy> {

                return parser?.invoke(response) ?: super.parseResponse(response)

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

                val response = Fetcher.okHttpClient.newCall(buildRequest()).execute()

                return parseResponse(response)

            }.onFailure {

                if (index == 3) throw it

            }

        }

        error("频道无法拉取")

    }

}