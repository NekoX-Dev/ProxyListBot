package io.github.nekohasekai.plbot.parser

import io.github.nekohasekai.plbot.proxy.Proxy

object ArrayParser : Parser<Array<*>> {

    override fun parseProxies(value: Array<*>): Collection<Proxy> = mutableSetOf<Proxy>().apply {

        value.forEach {

            if (it == null) return@forEach

            addAll(Parser.parseProxies(it))

        }

    }

}