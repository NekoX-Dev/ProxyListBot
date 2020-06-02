package io.github.nekohasekai.plbot.channel.impl

import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import io.github.nekohasekai.plbot.channel.Channel
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.proxy.Proxy

object ChannelTeleVpn : Channel {

    override val name = "TeleVPN"

    override fun fetchProxies(): Collection<Proxy> {

        Backendless.initApp("CDC94056-BE5D-DBE9-FF22-02E52B5F3F00", "EEBF6A02-C5B4-4187-9DD1-EAAA607D1CD3")

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

        return Parser.parseProxies(all)

    }
}