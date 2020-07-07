package io.github.nekohasekai.pl_serevr.channel

import com.pengrad.telegrambot.model.ChatMember
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.*
import io.github.nekohasekai.nekolib.core.utils.*
import io.github.nekohasekai.nekolib.proxy.parser.td.MessageParser
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.pl_serevr.database.ChatByName
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import td.TdApi

abstract class TdChannel : TdHandler(), Channel {

    override val async = false

    companion object {

        val database = mkDatabase("channel_cache")
        val table = database.mkTable<ChatByName>()

        val log = mkLog("ChannelFetcher")

        fun create(chatUserName: String): TdChannel {

            return object : TdChannel() {

                override val name get() = "Telegram @$chatUserName"

                override fun fetchProxies(): Collection<Proxy> = runBlocking {

                    mutableSetOf<Proxy>().apply {

                        val chatId = table[chatUserName]?.chatId ?: searchPublicChat(chatUserName).id.also {

                            table.update(ChatByName(chatUserName, it), true)

                        }

                        if (getChatMemberOrNull(chatId, me.id)?.status?.isMember != true) {

                            joinChat(chatId)

                            do {

                                log.debug("waiting for join channel: @$chatUserName")

                                delay(1000L)

                            } while (getChatMemberOrNull(chatId, me.id)?.status?.isMember != true)

                            delay(5000L)

                        }

                        var history: TdApi.Messages
                        var from = 0L
                        var size = 0
                        var limit: Boolean

                        do {

                            limit = false

                            try {

                                // 读消息
                                history = getChatHistory(chatId, from, 0, 100, false)

                                size += history.totalCount

                            } catch (ex: TdException) {

                                if (ex.code == 429) {

                                    println("wait for 429 limit.")

                                    delay(1000L)

                                    limit = true

                                    history = TdApi.Messages()

                                    continue

                                }

                                break

                            }

                            //println("fetched messages $size")

                            if (history.messages.isNotEmpty()) {

                                from = history.messages[history.messages.size - 1].id

                            }

                            history.messages.forEach {

                                message ->

                                addAll(MessageParser.parseProxies(message))

                            }

                        } while (limit || history.messages.isNotEmpty())

                    }
                }

            }

        }

    }

}