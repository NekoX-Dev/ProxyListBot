package io.github.nekohasekai.plbot.channel

import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.getChatHistory
import io.github.nekohasekai.nekolib.core.raw.searchPublicChat
import io.github.nekohasekai.plbot.parser.td.MessageParser
import io.github.nekohasekai.plbot.proxy.Proxy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import td.TdApi

abstract class TdChannel() : TdHandler(), Channel {

    companion object {

        fun create(chatUserName: String): TdChannel {

            return object : TdChannel() {

                override val name get() = "Telegram @$chatUserName"

                override fun fetchProxies(): Collection<Proxy> = runBlocking {

                    mutableSetOf<Proxy>().apply {

                        val chat = searchPublicChat(chatUserName)

                        var history: TdApi.Messages
                        var from = 0L
                        var size = 0
                        var limit: Boolean

                        do {

                            limit = false

                            try {

                                // 读消息
                                history = getChatHistory(chat.id, from, 0, 100, false)

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