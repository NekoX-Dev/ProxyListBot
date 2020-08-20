package io.github.nekohasekai.pl_serevr.channel

import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.client.TdHandler
import io.github.nekohasekai.nekolib.core.raw.*
import io.github.nekohasekai.nekolib.core.utils.*
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.parser.td.MessageParser
import io.github.nekohasekai.pl_serevr.database.ChatByName
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import td.TdApi

abstract class TdChannel : TdHandler(), Channel {

    override val async = false

    companion object {

        val log = mkLog("ChannelFetcher")

        fun create(chatUserName: String): TdChannel {

            return object : TdChannel() {

                override val name get() = "Telegram @$chatUserName"

                override fun fetchProxies(): Collection<Proxy> = runBlocking {

                    mutableSetOf<Proxy>().apply {

                        val chatId = searchPublicChat(chatUserName).id

                        if (getChatMemberOrNull(chatId, me.id)?.status?.isMember != true) {

                            joinChat(chatId)

                            setChatNotificationSettings(chatId, TdApi.ChatNotificationSettings().apply {

                                muteFor = 2 * 7 * 24 * 60 * 60
                                useDefaultShowPreview = true
                                disableMentionNotifications = true
                                disablePinnedMessageNotifications = true

                            })

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

                                if (history.totalCount > 0) {

                                    if ((System.currentTimeMillis() / 1000) - history.messages[history.messages.size - 1].date > 3 * 30 * 24 * 60 * 1000L) {

                                        break

                                    }

                                }

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