package io.github.nekohasekai.pl_serevr.database

import io.github.nekohasekai.nekolib.core.utils.kryo
import io.github.nekohasekai.nekolib.core.utils.kryoAny
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoProxy
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object ProxyEntities : LongIdTable("proxies") {

    const val UNCHECKED = 0
    const val INVALID = 1
    const val AVAILABLE = 2

    val proxy = kryo<MTProtoProxy>("proxy")
    val status = integer("status")
    val failedCount = integer("failed_count")
    val message = text("message").nullable()

}