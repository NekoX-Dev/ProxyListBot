package io.github.nekohasekai.plbot.channel.impl

import cn.hutool.core.codec.Base64
import cn.hutool.json.JSONObject
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

    fun addChannel(name: String, link: String, parser: ((Response) -> Collection<Proxy>)? = null, debug: Boolean = false) = create(name, link, parser, debug).apply { add() }

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

    addChannel("Topmessager", "https://www.androidhapro.ir/Top/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

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

    addChannel("NiceMobo", "https://locknet.website/dark/prxmgr/prxmgr.php")

    addChannel("BiChat", "https://www.dropbox.com/s/e4055xh1gmwno2p/robotsproxies.txt?dl=1")

    addChannel("BiChat2", "https://www.dropbox.com/s/ytl3ktllxw9s7ee/proxies.txt?dl=1")

    addChannel("LoxGram", "https://dl.dropboxusercontent.com/s/iogkcqbvrlxolei/data.json?dl=0")

    addChannel("IceGram", "https://www.dropbox.com/s/jgmrcwixe7ngtcz/robotsproxies.txt?dl=1")

    addChannel("IceGram2", "https://www.dropbox.com/s/ihg1wcm5jrswx5l/proxies.txt?dl=1")

    addChannel("IceGram3", "https://hydraplus.website/dark/prxmgr/prxmgr.php")

    addChannel("AreGram", "https://Ritmseda.online/mtprt/getproxy.php")

    addChannel("QGeram", "https://qbartar.com/dark/prx2/prxmgr.php")

    addChannel("Microgram", "https://dl.dropboxusercontent.com/s/vstucnq07jarua5/data.json?dl=0")

    addChannel("Graph plus","https://taaktook.website/telee/tel/prx/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("FilterPlus", "https://anfilrezil.website/dark/prxmgr/prxmgr.php")

    addChannel("RGram", "https://thextmind.website/RGram/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("GhodratGram", "https://pushepanel.com/dark/prxmgr/prxmgr.php")

    addChannel("FlashGram", "https://felashmobile.website/dark/prxmgr/prxmgr.php")

    addChannel("Robingram","https://redbreaste.website/ali/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    //addChannel("Robingram2", "https://redbreaste.website/proxy/getmt.php")

    addChannel("TeleBc", "http://141.136.35.91/tlp/?px&abcdefg=1356f07a-1c2e-49d7-b9e1-41f3f647f5bb")

    addChannel("GlodPlus", "https://delgrram.ir/delgram/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("FaraTel", "https://webgramapp.website/dark/prxmgr/prxmgr.php")

    addChannel("TeleHot", "https://telegramnews-a456a.firebaseio.com/proxy.json")

    object : HttpChannel() {

        override val name = "AzinGram"

        override fun buildRequest(): Request {

            return Request.Builder()
                    .url("http://app.serpanel.website/v1/ProxyConfig.php")
                    .post(FormBody.Builder()
                            .addEncoded("Authorization", "WTI5dExtRjZhVzVuY21GdExtSmxjM1E9")
                            .add("Authorization2", "com.azingram.best")
                            .add("VERSION_NAME", "5.11.0_A2")
                            .add("VERSION_CODE", "14445")
                            .add("ulp_sha256", "")
                            .build())
                    .build()

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            return response.body!!.string().parseProxyConfigDotPhpResponse()

        }

    }.add()

    addChannel("BlueGram", "https://shsra.club/tlp/?px&unused=eb82cee1-c882-4327-903e-7ba6f5907701")

    addChannel("ArsinGram", "https://Elgrami.site/mtprt/getproxy.php")

    addChannel("ChatPlus", "https://mememessenger.website/tele/proxy.php")

    addChannel("AlphaGeram", "https://dl.dropboxusercontent.com/s/vykl1c7cnpmoljx/data.json?dl=0")

    object : HttpChannel(true) {

        override val name = "CityPlus"

        override fun buildRequest(): Request {

            return Request.Builder()
                    .url("http://www.citytell.website/v1/ProxyConfig.php")
                    .post(FormBody.Builder()
                            .add("Authorization","WTI5dExtTnBkSGt1Y0d4MWMyVT0%3D")
                            .add("RequestForLoginProcess", "true")
                            .add("Authorization2", "com.city.pluse")
                            .add("VERSION_NAME", "5.11.0_CP2")
                            .add("VERSION_CODE", "23495")
                            .add("hasSupportedBlockChain", "false")
                            .add("SDK_INT", "28")
                            .add("ulp_sha256", "")
                            .build())
                    .build()

        }

        override fun parseResponse(response: Response): Collection<Proxy> {

            println(response)

            return response.body!!.string().also { println(it) }.parseProxyConfigDotPhpResponse()

        }

    }//.add()

    addChannel("PicoGram", "https://mycibuu.website/dark/v3/prxmgr.php")

    return httpChannels

}

fun String.parseProxyConfigDotPhpResponse(): Collection<Proxy> {

    return Parser.parseProxies(JSONObject(this)
            .getStr("custom_proxies")!!
            .split("!!!!")
            .mapNotNull { try {  JSONObject(it) } catch (e: Exception) { null } })

}
