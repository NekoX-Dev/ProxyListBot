package io.github.nekohasekai.pl_serevr.database

import org.dizitart.no2.Document
import org.dizitart.no2.mapper.Mappable
import org.dizitart.no2.mapper.NitriteMapper
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index

@Index("username")
class ChatByName() : Mappable {

    constructor(username: String, chatId: Long) {
        this.username = username
        this.chatId = chatId
    }

    @Id
    var username = ""
    var chatId = 0L


    override fun write(mapper: NitriteMapper): Document {

        return Document(mapOf(
                "username" to username,
                "chatId" to chatId
        ))

    }

    override fun read(mapper: NitriteMapper, document: Document) {

        username = document["username"] as String
        chatId = document["chatId"].toString().toLong()

    }

}