package io.github.nekohasekai.plbot.channel.impl

import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.proxy.Proxy
import io.github.nekohasekai.plbot.channel.Channel

object ChannelFlameProxy : Channel {

    override val name = "FlameProxy"

    override fun fetchProxies(): Collection<Proxy> {

        Backendless.initApp("2F2545E8-225A-4F55-FFAD-5F9842558900", "3177B0F3-2AE2-40B9-BE60-49E9DE318A76")

        val all = hashSetOf<String>()

        var hasNext = true
        var offset = 0

        while (hasNext) {

            val result = Backendless.Data.of("severs").find(DataQueryBuilder.create().setOffset(offset)).map {

                println(it)

                it["lipk"] as String

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