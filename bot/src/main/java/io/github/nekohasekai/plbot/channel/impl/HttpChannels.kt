package io.github.nekohasekai.plbot.channel.impl

import cn.hutool.core.codec.Base64
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import io.github.nekohasekai.plbot.channel.HttpChannel
import io.github.nekohasekai.plbot.channel.HttpChannel.Companion.create
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoMapParser
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoProxy
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response

fun createHttpChannels(): Collection<HttpChannel> {

    val httpChannels = mutableListOf<HttpChannel>()

    fun HttpChannel.add() = httpChannels.add(this)

    MTProtoMapParser.keyServerAlias.addAll(arrayOf(
            "host", "address", "adress", "ip"
    ))

    MTProtoMapParser.keyPortAlias.addAll(arrayOf(
            "prt"
    ))

    MTProtoMapParser.keySecretAlias.addAll(arrayOf(
            "secretKey", "srt"
    ))

    fun addChannel(name: String, link: String, debug: Boolean = false) = create(name, link, debug).apply { add() }

    addChannel("HiGram", "https://masterproxy27.online/mtprt/getproxy.php")
    addChannel("Nitrogram", "https://dl.dropboxusercontent.com/s/6c6qv3lfnbyezmh/server.json?dl=0")

    object : HttpChannel() {

        override val name = "ChatGera"

        override fun buildRequest(): Request {

            return getRequest("https://systemdb.info/Proxy/proxy.php")

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return super.parseResponse(response).map {

                it.apply {

                    this as MTProtoProxy

                    server = Base64.decodeStr(server)
                    secret = Base64.decodeStr(secret)

                }

            }

        }

    }.add()

    addChannel("ChatGera2", "https://systemdb.info/Proxy/proxy2.php")

    addChannel("Fungram", "https://dl.dropboxusercontent.com/s/vykl1c7cnpmoljx/data.json?dl=0")

    addChannel("Elgram", "https://beanelps.online/newbase/acc2/getproxy.php")
    addChannel("Elgram2", "https://elgramit.online/newbase/acc2/getproxy.php")

    object : HttpChannel() {

        override val name = "Topmessager"

        override fun buildRequest(): Request {

            return getRequest("https://www.androidhapro.ir/Top/ProxyConfig.php")

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return response.body!!.string().parseProxyConfigDotPhpResponse()

        }

    }.add()

    addChannel("RozGram", "https://jockertel.online/mtprt/getproxy.php")

    addChannel("NitroPlus", "https://darkvstar.info/dark/v3/prxmgr.php")

    addChannel("JetGram", "https://membergram.online/mtprt/getproxy.php")

    addChannel("Limogram", "http://thextmind.website/tlp/lim.php?px")

    addChannel("MTProx", "https://itrays.co/mtprox/json.php")

    object : HttpChannel() {

        override val name = "FlyChat"

        override fun buildRequest(): Request {

            return Request.Builder()
                    .url("https://m.flychat.in/getmtp")
                    .header("User-Agent", "fuck FlyChat !!!")
                    .build()

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return super.parseResponse(response).map { proxy ->

                proxy.apply {

                    this as MTProtoProxy

                    secret = secret.takeIf { it.isNotBlank() && !it.contains(".") } ?: "eef69705c573ac28cc6b125ee5874e77dc636c6f7564666c6172652e636f6d"

                }

            }

        }

    }.add()

    addChannel("GifProxy", "http://95.216.137.116/api")

    object : HttpChannel() {

        override val name = "KingGram"

        override fun buildRequest(): Request {

            return Request.Builder()
                    .url("http://new.serpanel.website/KingGram/v1/ProxyConfig.php")
                    .post(FormBody.Builder()
                            .addEncoded("Authorization", "YjNKbkxtZHlZVzB1YTJsdVoyMWxjM05sYm1kbGNnPT0%3D")
                            .add("Authorization2", "org.gram.kingmessenger")
                            .add("VERSION_NAME", "5.11.0_K4")
                            .add("VERSION_CODE", "13685")
                            .add("ulp_sha256", "2d78db9ef036bca435c07aea2ecfe539e531c4fe")
                            .build())
                    .build()

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return response.body!!.string().parseProxyConfigDotPhpResponse()

        }

    }.add()

    object : HttpChannel() {

        override val name = "Firstgram"

        override fun buildRequest(): Request {

            return Request.Builder()
                    .url("http://just313.info/ProxyConfig.php")
                    .post(FormBody.Builder()
                            .addEncoded("Authorization", "WTI5dExtRnNiR2R5WVcwdWJXVnpjMlZ1WjJWeQ%3D%3D")
                            .add("RequestForLoginProcess", "false")
                            .add("Authorization2", "com.allgram.messenger")
                            .add("VERSION_NAME", "5.11.0_F3")
                            .add("VERSION_CODE", "15005")
                            .add("hasSupportedBlockChain", "false")
                            .add("SDK_INT", "28")
                            .add("ulp_sha256", "")
                            .build())
                    .build()

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return response.body!!.string().parseProxyConfigDotPhpResponse()

        }

    }.add()

    addChannel("golgram", "https://dl.dropboxusercontent.com/s/clef48ly7jf9ing/server.json?dl=0")

    return httpChannels

}

fun String.parseProxyConfigDotPhpResponse(): Collection<Proxy> {

    return Parser.parseProxies(JSONObject(this)
            .getStr("custom_proxies")!!
            .split("!!!!")
            .map { JSONObject(it) })

}
