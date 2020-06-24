package io.github.nekohasekai.plbot.proxy.shadowsocks

import com.github.shadowsocks.plugin.PluginConfiguration
import io.github.nekohasekai.plbot.proxy.Proxy
import org.dizitart.no2.Document
import org.dizitart.no2.mapper.NitriteMapper

class ShadowsocksProxy() : Proxy {

    lateinit var server: String
    var port: Int = -1
    lateinit var password: String
    lateinit var method: String

    var plugin: String = ""
    var pluginOpts: String = ""

    var name: String = ""

    constructor(host: String = "",
                remotePort: Int = 443,
                password: String = "",
                method: String = "aes-256-cfb",
                plugin: String = "",
                remarks: String? = null) : this() {

        this.server = host
        this.port = remotePort
        this.password = password
        this.name = remarks ?: name
        this.method = if (method == "plain") "none" else method

        if (plugin.isNotBlank()) {

            val pl = PluginConfiguration(plugin)

            if (pl.selected.contains("v2ray") && pl.selected != "v2ray") {

                pl.pluginsOptions["v2ray"] = pl.getOptions()
                pl.pluginsOptions.remove(pl.selected)
                pl.selected = "v2ray"

                // reslove v2ray plugin

            }

            this.plugin = pl.selected
            this.pluginOpts = pl.pluginsOptions[this.plugin]!!.toString()

        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShadowsocksProxy

        if (server != other.server) return false
        if (port != other.port) return false
        if (password != other.password) return false
        if (method != other.method) return false
        if (plugin != other.plugin) return false
        if (pluginOpts != other.pluginOpts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = server.hashCode()
        result = 31 * result + port
        result = 31 * result + password.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + plugin.hashCode()
        result = 31 * result + pluginOpts.hashCode()
        return result
    }

    override fun compareTo(other: Proxy): Int {

        if (this == other) return 0
        return -1

    }

    override fun toString(): String {

        return "[SS] $server:$port".let {

            if (name.isBlank()) it else "$it ($name)"

        }

    }

    override fun strictKey(): String {

        var s = server

        while (s.count { it == '.' } > 2) {

            s = s.substringAfter('.')

        }

        return s

    }

    override fun write(mapper: NitriteMapper): Document {

        return Document().also {

            it["server"] = server
            it["port"] = port
            it["password"] = password
            it["method"] = method

            if (plugin.isNotBlank()) {

                it["plugin"] = plugin
                it["pluginOpts"] = pluginOpts

            }

            if (name.isNotBlank()) {

                it["name"] = name

            }

        }

    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    override fun read(mapper: NitriteMapper, document: Document) {

        server = document["server"] as String
        port = (document["port"] as Integer) as Int
        password = document["password"] as String
        method = document["method"] as String

        document["plugin"]?.also {

            plugin = it as String
            pluginOpts = document["pluginOpts"] as String

        }

        document["name"]?.also {

            name = it as String

        }

    }

}