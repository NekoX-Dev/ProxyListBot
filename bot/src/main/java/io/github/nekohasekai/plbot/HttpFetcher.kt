package io.github.nekohasekai.plbot

import cn.hutool.core.codec.Base64
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import okhttp3.HttpUrl.Companion.toHttpUrl

object HttpFetcher {

    val timeout = 2333

    fun fetchHiGram(url: String = "https://masterproxy27.online/mtprt/getproxy.php") = JSONArray(HttpUtil.createGet(url).timeout(timeout).execute().body()).toList(JSONObject::class.java).map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "server", "port", "secret" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

    fun fetchNitrogram() = JSONObject(HttpUtil.get("https://dl.dropboxusercontent.com/s/6c6qv3lfnbyezmh/server.json?dl=0",timeout)).getJSONArray("result").toList(JSONObject::class.java).map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "address" -> url.addQueryParameter("server", "$v")
                "port", "secret" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

    fun fetchChatGera() = listOf(JSONObject(HttpUtil.get("https://systemdb.info/Proxy/proxy.php",timeout)).run {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "address" -> url.addQueryParameter("server", Base64.decodeStr("$v"))
                "secret" -> url.addQueryParameter(k, Base64.decodeStr("$v"))
                "port" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }).filterNotNull()

    fun fetchChatGera2() = JSONArray(HttpUtil.get("https://systemdb.info/Proxy/proxy2.php",timeout)).toList(JSONObject::class.java).map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "adress" -> url.addQueryParameter("server", "$v")
                "port", "secret" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

    fun fetchVGram(url: String = "https://3dword.xyz/v3/prxmgr.php") = JSONArray(HttpUtil.get(url,timeout)).toList(JSONObject::class.java).map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "ip" -> url.addQueryParameter("server", "$v")
                "prt" -> url.addQueryParameter("port", "$v")
                "secret" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

    fun fetchFungram() = JSONObject(HttpUtil.get("https://dl.dropboxusercontent.com/s/vykl1c7cnpmoljx/data.json?dl=0",timeout))
            .getJSONArray("login")
            .toList(String::class.java)

    fun fetchElGram() = fetchHiGram("https://beanelps.online/newbase/acc2/getproxy.php")
    fun fetchElGram2() = fetchHiGram("https://elgramit.online/newbase/acc2/getproxy.php")

    fun fetchTopmessager() = JSONObject(HttpUtil.get("https://www.androidhapro.ir/Top/ProxyConfig.php",timeout)).getStr("custom_proxies").split("|").map { JSONObject(it) }.map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "ip" -> url.addQueryParameter("server", "$v")
                "prt" -> url.addQueryParameter("port", "$v")
                "secretKey" -> url.addQueryParameter("secret", "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

    fun fetchRozGram() = fetchHiGram("https://jockertel.online/mtprt/getproxy.php")

    fun fetchNitroPlus() = fetchVGram("https://darkvstar.info/dark/v3/prxmgr.php")

    fun fetchJetGram() = fetchHiGram("https://membergram.online/mtprt/getproxy.php")

    fun fetchLimogram() = fetchHiGram("http://thextmind.website/tlp/lim.php?px")

    init {

        Backendless.initApp("CDC94056-BE5D-DBE9-FF22-02E52B5F3F00", "EEBF6A02-C5B4-4187-9DD1-EAAA607D1CD3")

    }

    fun fetchTeleVpn(): List<String> {

        val all = hashSetOf<String>()

        var hasNext = true
        var offset = 0

        while (hasNext) {

            val result = Backendless.Data.of("proxy").find(DataQueryBuilder.create().setOffset(offset)).map {

                it["link"] as String

            }

            if (result.isEmpty()) {

                hasNext = false

            } else {

                all.addAll(result)

                offset += result.size

            }

        }

        return all.toList()

    }

    fun fetchMTProx() = JSONObject(HttpUtil.get("https://itrays.co/mtprox/json.php",timeout)).getJSONArray("data").toList(JSONObject::class.java).map {

        val url = "https://t.me/proxy".toHttpUrl().newBuilder()

        it.forEach { k, v ->

            if (v == null) return@forEach

            when (k) {

                "host" -> url.addQueryParameter("server", "$v")
                "port", "secret" -> url.addQueryParameter(k, "$v")

            }


        }

        val urlFinal = url.build()

        if (urlFinal.queryParameter("secret") == null) null else urlFinal.toString()

    }.filterNotNull()

}