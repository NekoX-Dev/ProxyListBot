package io.github.nekohasekai.plbot.proxy.shadowsocks

import cn.hutool.core.codec.Base64
import com.github.shadowsocks.plugin.PluginConfiguration
import com.github.shadowsocks.plugin.PluginOptions
import io.github.nekohasekai.plbot.saver.LinkSaver
import okhttp3.HttpUrl
import java.rmi.UnexpectedException

object ShadowsocksLinkSaver : LinkSaver<ShadowsocksProxy> {

    override fun toLink(proxy: ShadowsocksProxy): String {

        var url = HttpUrl.Builder()
                .scheme("https")
                .encodedUsername(Base64.encodeUrlSafe("$proxy.method:$proxy.password"))
                .host(proxy.server)
                .port(proxy.port)

        if (proxy.name.isNotBlank()) url.fragment(proxy.name)

        if (proxy.plugin.isNotBlank()) url.addQueryParameter("plugin", PluginOptions(proxy.plugin,proxy.pluginOpts).toString(false))

        return url.build().toString().replace("https://","ss://")

    }

    override fun toLink(protocol: String, proxy: ShadowsocksProxy): String {

        if (protocol != "ss") error("unexpected protocol $protocol")

        return toLink(proxy)

    }

}