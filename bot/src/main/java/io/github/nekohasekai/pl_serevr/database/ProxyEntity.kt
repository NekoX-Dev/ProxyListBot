package io.github.nekohasekai.pl_serevr.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProxyEntity(id: EntityID<Long>) : Entity<Long>(id) {

    var proxy by ProxyEntities.proxy
    var failedCount by ProxyEntities.failedCount
    var status by ProxyEntities.status
    var message by ProxyEntities.message

    companion object : EntityClass<Long, ProxyEntity>(ProxyEntities)

}