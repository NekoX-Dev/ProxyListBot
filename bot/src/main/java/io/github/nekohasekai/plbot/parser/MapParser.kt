package io.github.nekohasekai.plbot.parser

import io.github.nekohasekai.plbot.proxy.Proxy

interface MapParser : Parser<Map<String, String>> {

    val name: String

    fun parseProxy(value: Map<String, String>): Proxy

    override fun parseProxies(value: Map<String, String>) = listOf(parseProxy(value))

    companion object : MapParser {

        override val name = "<root>"

        private val implements = mutableSetOf<MapParser>()

        fun addParser(parser: MapParser) = implements.add(parser)

        fun parseMap(value: Map<*, *>): List<Proxy> {

            val stringMap = hashMapOf<String, String>()

            value.forEach { (key, value) ->

                if (key != null && value != null) {

                    stringMap["$key"] = "$value"

                }

            }

            return parseProxies(stringMap)

        }

        override fun parseProxy(value: Map<String, String>): Proxy {

            val messages = hashMapOf<String, Throwable>()

            implements.forEach { parser ->

                runCatching {

                    return parser.parseProxy(value)

                }.onFailure {

                    messages[parser.name] = it

                }

            }

            error("unable to parse proxy: $value\n\n${messages.map { throwable -> "${throwable.key}: ${throwable.value.let { it.message ?: it.javaClass.simpleName }}" }.joinToString("\n")}")

        }

    }

}