package io.nekohasekai.pl_bot.channel.impl

import cn.hutool.json.JSONObject
import io.nekohasekai.pl_bot.channel.HttpChannel
import io.nekohasekai.pl_bot.channel.HttpChannel.Companion.create
import io.nekohasekai.td.proxy.impl.Proxy
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoMapParser
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoProxy
import io.nekohasekai.td.proxy.parser.Parser
import okhttp3.*

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

    addChannel("Topmessager", "https://www.androidhapro.ir/Top/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("NitroPlus", "https://darkvstar.info/dark/v3/prxmgr.php")

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

    addChannel("BiChat", "https://www.dropbox.com/s/e4055xh1gmwno2p/robotsproxies.txt?dl=1")

    addChannel("BiChat2", "https://www.dropbox.com/s/ytl3ktllxw9s7ee/proxies.txt?dl=1")

    addChannel("IceGram", "https://www.dropbox.com/s/jgmrcwixe7ngtcz/robotsproxies.txt?dl=1")

    addChannel("IceGram2", "https://www.dropbox.com/s/ihg1wcm5jrswx5l/proxies.txt?dl=1")

    addChannel("IceGram3", "https://hydraplus.website/dark/prxmgr/prxmgr.php")

    addChannel("Graph plus","https://taaktook.website/telee/tel/prx/ProxyConfig.php", { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("GhodratGram", "https://pushepanel.com/dark/prxmgr/prxmgr.php")

    addChannel("FlashGram", "https://felashmobile.website/dark/prxmgr/prxmgr.php")

    addChannel("TeleBc", "http://141.136.35.91/tlp/?px&abcdefg=1356f07a-1c2e-49d7-b9e1-41f3f647f5bb")

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

    addChannel("MissGram", "https://www.dropbox.com/s/vz6ltoyca8b3t3q/robotsproxies.txt?dl=1")

    addChannel("MissGram2", "https://www.dropbox.com/s/yfe3o81bvt41a70/proxies.txt?dl=1")

    addChannel("SkyGram", "https://taaktook.website/telee/tel/prx/ProxyConfig.php" , { it.body!!.string().parseProxyConfigDotPhpResponse() })

    addChannel("NiceGram", "https://yarnarenji.website/dark/prxmgr/prxmgr.php")

    return httpChannels

}

fun String.parseProxyConfigDotPhpResponse(): Collection<Proxy> {

    return Parser.parseProxies(JSONObject(this)
            .getStr("custom_proxies")!!
            .split("!!!!")
            .mapNotNull { try {  JSONObject(it) } catch (e: Exception) { null } })

}
