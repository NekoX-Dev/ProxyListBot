package com.v2ray.ang.util

import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.v2ray.ang.V2RayConfig
import com.v2ray.ang.dto.AngConfig.VmessBean
import com.v2ray.ang.dto.V2rayConfig

object V2rayConfigUtil {
    private val requestObj: JsonObject by lazy {
        Gson().fromJson("""{"version":"1.1","method":"GET","path":["/"],"headers":{"User-Agent":["Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36","Mozilla/5.0 (iPhone; CPU iPhone OS 10_0_2 like Mac OS X) AppleWebKit/601.1 (KHTML, like Gecko) CriOS/53.0.2785.109 Mobile/14A456 Safari/601.1.46"],"Accept-Encoding":["gzip, deflate"],"Connection":["keep-alive"],"Pragma":"no-cache"}}""", JsonObject::class.java)
    }

//    private val responseObj: JSONObject by lazy {
//        JSONObject("""{"version":"1.1","status":"200","reason":"OK","headers":{"Content-Type":["application/octet-stream","video/mpeg"],"Transfer-Encoding":["chunked"],"Connection":["keep-alive"],"Pragma":"no-cache"}}""")
//    }

    data class Result(var status: Boolean, var content: String)

    @JvmStatic
    var currDomain: String = ""

    /**
     * 生成v2ray的客户端配置文件
     */
    @JvmStatic
    fun getV2rayConfig(vmess: VmessBean, port: Int): Result {
        val result = Result(false, "")
        try {
            //转成Json
            val v2rayConfig = Gson().fromJson("""
{
  "dns": {
    "servers": [
      "https+local://mozilla.cloudflare-dns.com/dns-query",
      "https+local://dns.google/dns-query",
      "https+local://dns.adguard.com/dns-query"
    ]
  },
  "inbounds": [{
    "tag": "socks-in",
    "listen": "127.0.0.1",
    "port": 114514,
    "protocol": "socks",
    "settings": {
      "auth": "noauth",
      "udp": true,
      "userLevel": 8
    },
    "sniffing": {
      "destOverride": [
        "http",
        "tls"
      ],
      "enabled": true
    }
  }
  ],
  "outbounds": [{
    "tag": "proxy",
    "protocol": "vmess",
    "settings": {
      "vnext": [{
        "address": "v2ray.cool",
        "port": 443,
        "users": [{
          "alterId": 64,
          "id": "73670f86-1145-4ffd-b468-6cd73cea1f29",
          "level": 8,
          "security": "none"
        }]
      }]
    },
    "streamSettings": {
      "network": "ws",
      "security": "tls",
      "tlssettings": {
        "allowInsecure": true,
        "serverName": "baidu.com"
      },
      "wssettings": {
        "connectionReuse": true,
        "headers": {
          "Host": "baidu.com"
        },
        "path": "/search"
      }
    }
  }],
  "routing": {
    "domainStrategy": "IPIfNonMatch",
    "rules": [{
      "inboundTag": [
        "socks-in"
      ],
      "outboundTag": "proxy",
      "type": "field"
    }]
  }
}
""", V2rayConfig::class.java) ?: return result
//            if (v2rayConfig == null) {
//                return result
//            }

            inbounds(vmess, v2rayConfig, port)

            outbounds(vmess, v2rayConfig)

            routing(vmess, v2rayConfig)

            val finalConfig = GsonBuilder().setPrettyPrinting().create().toJson(v2rayConfig)

            result.status = true
            result.content = finalConfig
            return result

        } catch (e: Exception) {
            e.printStackTrace()
            return result
        }
    }

    /**
     *
     */
    private fun inbounds(vmess: VmessBean, v2rayConfig: V2rayConfig, port: Int): Boolean {
        try {
            v2rayConfig.inbounds.forEach { curInbound ->
                curInbound.listen = "127.0.0.1"
            }
            v2rayConfig.inbounds[0].port = port
//            val socksPort = Utils.parseInt(app.defaultDPreference.getPrefString(SettingsActivity.PREF_SOCKS_PORT, "10808"))
//            val lanconnPort = Utils.parseInt(app.defaultDPreference.getPrefString(SettingsActivity.PREF_HTTP_PORT, ""))

//            if (socksPort > 0) {
//                v2rayConfig.inbounds[0].port = socksPort
//            }
//            if (lanconnPort > 0) {
//                val httpCopy = v2rayConfig.inbounds[0].copy()
//                httpCopy.port = lanconnPort
//                httpCopy.protocol = "http"
//                v2rayConfig.inbounds.add(httpCopy)
//            }
            v2rayConfig.inbounds[0].sniffing?.enabled = false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * vmess协议服务器配置
     */
    private fun outbounds(vmess: VmessBean, v2rayConfig: V2rayConfig): Boolean {
        try {
            val outbound = v2rayConfig.outbounds[0]

            when (vmess.configType) {
                V2RayConfig.EConfigType.Vmess -> {
                    outbound.settings?.servers = null

                    val vnext = v2rayConfig.outbounds[0].settings?.vnext?.get(0)
                    vnext?.address = vmess.address
                    vnext?.port = vmess.port
                    val user = vnext?.users?.get(0)
                    user?.id = vmess.id
                    user?.alterId = vmess.alterId
                    user?.security = vmess.security
                    user?.level = 8

                    //Mux
                    val muxEnabled = false//app.defaultDPreference.getPrefBoolean(SettingsActivity.PREF_MUX_ENABLED, false)
                    outbound.mux?.enabled = muxEnabled

                    //远程服务器底层传输配置
                    outbound.streamSettings = boundStreamSettings(vmess)

                    outbound.protocol = "vmess"
                }
                V2RayConfig.EConfigType.Shadowsocks -> {
                    outbound.settings?.vnext = null

                    val server = outbound.settings?.servers?.get(0)
                    server?.address = vmess.address
                    server?.method = vmess.security
                    server?.ota = false
                    server?.password = vmess.id
                    server?.port = vmess.port
                    server?.level = 8

                    //Mux
                    outbound.mux?.enabled = false

                    outbound.protocol = "shadowsocks"
                }
                else -> {
                }
            }

            var serverDomain: String
            if (Utils.isIpv6Address(vmess.address)) {
                serverDomain = String.format("[%s]:%s", vmess.address, vmess.port)
            } else {
                serverDomain = String.format("%s:%s", vmess.address, vmess.port)
            }
            currDomain = serverDomain
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 远程服务器底层传输配置
     */
    private fun boundStreamSettings(vmess: VmessBean): V2rayConfig.OutboundBean.StreamSettingsBean {
        val streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean("", "", null, null, null, null, null, null)
        try {
            //远程服务器底层传输配置
            streamSettings.network = vmess.network
            streamSettings.security = vmess.streamSecurity

            //streamSettings
            when (streamSettings.network) {
                "kcp" -> {
                    val kcpsettings = V2rayConfig.OutboundBean.StreamSettingsBean.KcpsettingsBean()
                    kcpsettings.mtu = 1350
                    kcpsettings.tti = 50
                    kcpsettings.uplinkCapacity = 12
                    kcpsettings.downlinkCapacity = 100
                    kcpsettings.congestion = false
                    kcpsettings.readBufferSize = 1
                    kcpsettings.writeBufferSize = 1
                    kcpsettings.header = V2rayConfig.OutboundBean.StreamSettingsBean.KcpsettingsBean.HeaderBean()
                    kcpsettings.header.type = vmess.headerType
                    streamSettings.kcpsettings = kcpsettings
                }
                "ws" -> {
                    val wssettings = V2rayConfig.OutboundBean.StreamSettingsBean.WssettingsBean()
                    wssettings.connectionReuse = true
                    val host = vmess.requestHost.trim()
                    val path = vmess.path.trim()

                    if (!StrUtil.isBlank(host)) {
                        wssettings.headers = V2rayConfig.OutboundBean.StreamSettingsBean.WssettingsBean.HeadersBean()
                        wssettings.headers.Host = host
                    }
                    if (!StrUtil.isBlank(path)) {
                        wssettings.path = path
                    }
                    streamSettings.wssettings = wssettings

                    val tlssettings = V2rayConfig.OutboundBean.StreamSettingsBean.TlssettingsBean()
                    tlssettings.allowInsecure = true
                    if (!StrUtil.isBlank(host)) {
                        tlssettings.serverName = host
                    }
                    streamSettings.tlssettings = tlssettings
                }
                "h2" -> {
                    val httpsettings = V2rayConfig.OutboundBean.StreamSettingsBean.HttpsettingsBean()
                    val host = vmess.requestHost.trim()
                    val path = vmess.path.trim()

                    if (!StrUtil.isBlank(host)) {
                        httpsettings.host = host.split(",").map { it.trim() }
                    }
                    httpsettings.path = path
                    streamSettings.httpsettings = httpsettings

                    val tlssettings = V2rayConfig.OutboundBean.StreamSettingsBean.TlssettingsBean()
                    tlssettings.allowInsecure = true
                    streamSettings.tlssettings = tlssettings
                }
                "quic" -> {
                    val quicsettings = V2rayConfig.OutboundBean.StreamSettingsBean.QuicsettingBean()
                    val host = vmess.requestHost.trim()
                    val path = vmess.path.trim()

                    quicsettings.security = host
                    quicsettings.key = path

                    quicsettings.header = V2rayConfig.OutboundBean.StreamSettingsBean.QuicsettingBean.HeaderBean()
                    quicsettings.header.type = vmess.headerType

                    streamSettings.quicsettings = quicsettings
                }
                else -> {
                    //tcp带http伪装
                    if (vmess.headerType == "http") {
                        val tcpSettings = V2rayConfig.OutboundBean.StreamSettingsBean.TcpsettingsBean()
                        tcpSettings.connectionReuse = true
                        tcpSettings.header = V2rayConfig.OutboundBean.StreamSettingsBean.TcpsettingsBean.HeaderBean()
                        tcpSettings.header.type = vmess.headerType

//                        if (requestObj.containsKey("headers")
//                                || requestObj.getJSONObject("headers").containsKey("Pragma")) {
//                            val arrHost = ArrayList<String>()
//                            vmess.requestHost
//                                    .split(",")
//                                    .forEach {
//                                        arrHost.add(it)
//                                    }
//                            requestObj.getJSONObject("headers")
//                                    .put("Host", arrHost)
//
//                        }
                        if (!StrUtil.isBlank(vmess.requestHost)) {
                            val arrHost = ArrayList<String>()
                            vmess.requestHost
                                    .split(",")
                                    .forEach {
                                        arrHost.add("\"$it\"")
                                    }
                            requestObj.getAsJsonObject("headers")
                                    .add("Host", Gson().fromJson(arrHost.toString(), JsonArray::class.java))
                        }
                        if (!StrUtil.isBlank(vmess.path)) {
                            val arrPath = ArrayList<String>()
                            vmess.path
                                    .split(",")
                                    .forEach {
                                        arrPath.add("\"$it\"")
                                    }
                            requestObj.add("path", Gson().fromJson(arrPath.toString(), JsonArray::class.java))
                        }
                        tcpSettings.header.request = requestObj
                        //tcpSettings.header.response = responseObj
                        streamSettings.tcpSettings = tcpSettings
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return streamSettings
        }
        return streamSettings
    }

    /**
     * routing
     */
    private fun routing(vmess: VmessBean, v2rayConfig: V2rayConfig) {

        v2rayConfig.routing.domainStrategy = "IPIfNonMatch"

    }

    /**
     * is valid config
     */
    fun isValidConfig(conf: String): Boolean {
        try {
            val jObj = JSONObject(conf)
            var hasBound = false
            //hasBound = (jObj.containsKey("outbounds") and jObj.containsKey("inbounds")) or (jObj.containsKey("outbound") and jObj.containsKey("inbound"))
            hasBound = (jObj.containsKey("outbounds")) or (jObj.containsKey("outbound"))
            return hasBound
        } catch (e: Exception) {
            return false
        }
    }

    private fun parseDomainName(jsonConfig: String): String {
        try {
            val jObj = JSONObject(jsonConfig)
            var domainName: String
            if (jObj.containsKey("outbound")) {
                domainName = parseDomainName(jObj.getJSONObject("outbound"))
                if (!StrUtil.isBlank(domainName)) {
                    return domainName
                }
            }
            if (jObj.containsKey("outbounds")) {
                for (i in 0 until jObj.getJSONArray("outbounds").size) {
                    domainName = parseDomainName(jObj.getJSONArray("outbounds").getJSONObject(i))
                    if (!StrUtil.isBlank(domainName)) {
                        return domainName
                    }
                }
            }
            if (jObj.containsKey("outboundDetour")) {
                for (i in 0 until jObj.getJSONArray("outboundDetour").size) {
                    domainName = parseDomainName(jObj.getJSONArray("outboundDetour").getJSONObject(i))
                    if (!StrUtil.isBlank(domainName)) {
                        return domainName
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun parseDomainName(outbound: JSONObject): String {
        try {
            if (outbound.containsKey("settings")) {
                var vnext: JSONArray?
                if (outbound.getJSONObject("settings").containsKey("vnext")) {
                    // vmess
                    vnext = outbound.getJSONObject("settings").getJSONArray("vnext")
                } else if (outbound.getJSONObject("settings").containsKey("servers")) {
                    // shadowsocks or socks
                    vnext = outbound.getJSONObject("settings").getJSONArray("servers")
                } else {
                    return ""
                }
                for (i in 0 until vnext!!.size) {
                    val item = vnext.getJSONObject(i)
                    val address = item.getStr("address")
                    val port = item.getStr("port")
                    if (Utils.isIpv6Address(address)) {
                        return String.format("[%s]:%s", address, port)
                    } else {
                        return String.format("%s:%s", address, port)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}