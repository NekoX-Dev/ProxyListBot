package io.github.nekohasekai.plbot.parser

import cn.hutool.json.JSON
import io.github.nekohasekai.plbot.proxy.Proxy

interface Parser<T : Any> {

    fun parseProxies(value: T): Collection<Proxy>

    companion object : Parser<Any> {

        override fun parseProxies(value: Any): Collection<Proxy> {

            @Suppress("UNCHECKED_CAST")
            return when (value) {

                is JSON -> JSONParser.parseProxies(value)

                is String -> StringParser.parseProxies(value)

                is Map<*,*> -> MapParser.parseMap(value)

                is Iterable<*> -> IterableParser.parseProxies(value as Iterable<Any>)

                else -> StringParser.parseProxies("$value")

            }

        }

    }

}