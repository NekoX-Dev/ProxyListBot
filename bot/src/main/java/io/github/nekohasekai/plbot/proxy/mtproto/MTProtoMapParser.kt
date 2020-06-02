package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.plbot.parser.MapParser
import io.github.nekohasekai.plbot.proxy.Proxy

object MTProtoMapParser : MapParser {

    override val name = "mtproto"

    val keyServerAlias = hashSetOf<String>()
    val keyPortAlias = hashSetOf<String>()
    val keySecretAlias = hashSetOf<String>()

    override fun parseProxy(value: Map<String, String>): Proxy {

        val server = value["server"] ?: run {

            keyServerAlias.forEach { alias ->

                value[alias]?.also { return@run it }

            }

            null

        } ?: error("no server field found")

        val port = (value["port"] ?: run {

            keyPortAlias.forEach { alias ->

                value[alias]?.also { return@run it }

            }

            null

        } ?: error("no port field found")).let { runCatching { it.toInt() }.getOrNull() } ?: error("invalid port")

        val secret = value["secret"] ?: run {

            keySecretAlias.forEach { alias ->

                value[alias]?.also { return@run it }

            }

            null

        } ?: error("no secret field found")

        return MTProtoProxy().also {

            it.server = server
            it.port = port
            it.secret = secret

        }

    }

}