package io.github.nekohasekai.plbot.parser

import io.github.nekohasekai.plbot.proxy.Proxy

interface LinkParser : Parser<String> {

    val name: String

    fun parseProxy(link: String): Proxy

    override fun parseProxies(value: String): Collection<Proxy> {

        runCatching {

            return listOf(parseProxy(value))

        }

        return listOf()

    }

    companion object : LinkParser {

        override val name = "<root>"

        private val implements = mutableSetOf<LinkParser>()

        fun addParser(parser: LinkParser) = implements.add(parser)

        override fun parseProxy(link: String): Proxy {

            val messages = hashMapOf<String,Throwable>()

            implements.forEach { parser ->

                runCatching {

                    return parser.parseProxy(link)

                }.onFailure {

                    messages[parser.name] = it

                }

            }

            error("unable to parse proxy: $link\n\n${messages.map { throwable -> "${throwable.key}: ${throwable.value.let { it.message ?: it.javaClass.simpleName }}" }.joinToString { "\n" }}")

        }

    }

}