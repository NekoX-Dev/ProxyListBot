package io.nekohasekai.pl_bot.database

import io.nekohasekai.ktlib.db.kryo
import io.nekohasekai.td.proxy.impl.mtproto.MTProtoProxy
import org.jetbrains.exposed.dao.id.LongIdTable

object ProxyEntities : LongIdTable("proxies") {

    const val UNCHECKED = 0
    const val INVALID = 1
    const val AVAILABLE = 2

    val proxy = kryo<MTProtoProxy>("proxy")
    val status = integer("status")
    val failedCount = integer("failed_count")
    val message = text("message").nullable()

}