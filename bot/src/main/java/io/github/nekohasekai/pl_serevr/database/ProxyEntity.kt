package io.github.nekohasekai.pl_serevr.database

import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import org.dizitart.no2.Document
import org.dizitart.no2.NitriteId
import org.dizitart.no2.mapper.Mappable
import org.dizitart.no2.mapper.NitriteMapper
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index

@Index("proxy")
open class ProxyEntity : Mappable,Comparable<ProxyEntity> {

    override fun compareTo(other: ProxyEntity): Int = proxy.compareTo(other.proxy)

    companion object {

        const val UNCHECKED = 0
        const val INVALID = 1
        const val AVAILABLE = 2

    }

    lateinit var proxy: Proxy

    var status = UNCHECKED
    var message: String? = null

    @Id @JvmField
    var _id: Long = 0L

    override fun toString() = proxy.toString()

    override fun write(mapper: NitriteMapper): Document {

        return Document().also {

            it["type"] = proxy::class.java.name
            it["proxy"] = proxy.write(mapper)

            it["status"] = status

            message?.apply { it["message"] = this }

            if (_id == 0L) {

                _id = NitriteId.newId().idValue

            }

            it["_id"] = _id

        }

    }

    override fun read(mapper: NitriteMapper, document: Document) {

        val type = Class.forName(document["type"] as String)
        proxy = mapper.asObject(document["proxy"] as Document, type) as Proxy

        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        status = ((document["status"] ?: UNCHECKED) as Integer) as Int

        message = document["message"] as String?

        _id = document.id.idValue

    }

}