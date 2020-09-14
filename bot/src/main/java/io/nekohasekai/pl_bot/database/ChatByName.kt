package io.nekohasekai.pl_bot.database

import org.jetbrains.exposed.sql.Table

object ChatByName : Table("chat_by_name") {

    val username = text("username").uniqueIndex()
    val chatId = long("chatId").uniqueIndex()

}