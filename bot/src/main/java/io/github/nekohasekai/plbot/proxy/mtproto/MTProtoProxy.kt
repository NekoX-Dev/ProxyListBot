package io.github.nekohasekai.plbot.proxy.mtproto

import io.github.nekohasekai.plbot.proxy.Proxy
import org.dizitart.no2.Document
import org.dizitart.no2.mapper.NitriteMapper

class MTProtoProxy : Proxy {

    lateinit var server: String
    var port: Int = 0
    lateinit var secret: String

    override fun toString() = "$server:$port"

    override fun strictKey(): String {

        var s = server

        while (s.count { it == '.' } > 2) {

            s = s.substringAfter('.')

        }

        return s

    }

    override fun compareTo(other: Proxy): Int {

        if (this == other) return 0

        return -1

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MTProtoProxy

        if (server != other.server) return false
        if (port != other.port) return false
        if (secret != other.secret) return false

        return true
    }

    override fun hashCode(): Int {
        var result = server.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + "$port".hashCode()
        return result
    }

    override fun write(mapper: NitriteMapper) = Document().also {

        it["server"] = server
        it["port"] = port
        it["secret"] = secret

    }

    override fun read(mapper: NitriteMapper, document: Document) {

        runCatching {

            server = document["server"] as String
            port = (document["port"] as Integer) as Int
            secret = document["secret"] as String

        }.onFailure {

            throw it

        }

    }

}