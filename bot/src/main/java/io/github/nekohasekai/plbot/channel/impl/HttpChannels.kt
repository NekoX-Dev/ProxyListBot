package io.github.nekohasekai.plbot.channel.impl

import cn.hutool.core.codec.Base64
import cn.hutool.json.JSONUtil
import io.github.nekohasekai.plbot.channel.HttpChannel
import io.github.nekohasekai.plbot.channel.HttpChannel.Companion.create
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoMapParser
import io.github.nekohasekai.plbot.proxy.mtproto.MTProtoProxy
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
            "secretKey"
    ))

    create("HiGram", "https://masterproxy27.online/mtprt/getproxy.php").add()
    create("Nitrogram", "https://dl.dropboxusercontent.com/s/6c6qv3lfnbyezmh/server.json?dl=0").add()

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

    create("ChatGera2", "https://systemdb.info/Proxy/proxy2.php").add()

    create("Fungram", "https://dl.dropboxusercontent.com/s/vykl1c7cnpmoljx/data.json?dl=0").add()

    create("Elgram", "https://beanelps.online/newbase/acc2/getproxy.php").add()
    create("Elgram2", "https://elgramit.online/newbase/acc2/getproxy.php").add()

    object : HttpChannel() {

        override val name = "Topmessager"

        override fun buildRequest(): Request {

            return getRequest("https://www.androidhapro.ir/Top/ProxyConfig.php")

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            val responseStr = response.body!!.string()

            return Parser.parseProxies(JSONUtil.parseObj(responseStr).getStr("custom_proxies").split("|"))

        }

    }.add()

    create("RozGram", "https://jockertel.online/mtprt/getproxy.php").add()

    create("NitroPlus", "https://darkvstar.info/dark/v3/prxmgr.php").add()

    create("JetGram", "https://membergram.online/mtprt/getproxy.php").add()

    create("Limogram", "http://thextmind.website/tlp/lim.php?px").add()

    create("MTProx", "https://itrays.co/mtprox/json.php").add()

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

    create("GifProxy","http://95.216.137.116/api").add()

    return httpChannels

}
