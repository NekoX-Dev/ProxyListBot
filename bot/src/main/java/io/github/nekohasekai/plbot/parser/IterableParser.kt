package io.github.nekohasekai.plbot.parser

import io.github.nekohasekai.plbot.proxy.Proxy

object IterableParser : Parser<Iterable<*>> {

    override fun parseProxies(value: Iterable<*>): Collection<Proxy> = mutableSetOf<Proxy>().apply {

        value.forEach {

            if (it == null) return@forEach

            addAll(Parser.parseProxies(it))

        }

    }

}