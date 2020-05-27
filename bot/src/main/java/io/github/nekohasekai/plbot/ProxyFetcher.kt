package io.github.nekohasekai.plbot

import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.raw.getChatHistory
import io.github.nekohasekai.nekolib.core.raw.searchPublicChat
import io.github.nekohasekai.nekolib.core.utils.text
import kotlinx.coroutines.*
import td.TdApi
import kotlin.system.exitProcess

object ProxyFetcher : TdClient() {

    val channels = arrayListOf(
            "socks5list",
            "onessr",
            "MTProtoShare",
            "cnhumanright99",
            "prossh",
            "MTProxyT",
            "MTProxyStar",
            "ProxyMTProto"
    )

    init {

        options databaseDirectory "data/fetcher"

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        TdLoader.tryLoad()

        waitForLogin()

        val proxyArray = proxyList.toMutableSet()

        channels.forEach { channel ->

            val chat = searchPublicChat(channel)

            val start = proxyArray.size

            var history: TdApi.Messages
            var from = 0L
            var size = 0

            do {

                try {

                    history = getChatHistory(chat.id, from, 0, 100, false)

                } catch (ex: TdException) {

                    println("FAILED: ${ex.message}")

                    break

                }

                if (history.messages.isNotEmpty()) {

                    from = history.messages[history.messages.size - 1].id

                }

                history.messages.forEach {

                    message ->

                    message.searchMTProxies().forEach {

                        proxyArray.add(it)

                        size++

                    }

                }

            } while (history.messages.isNotEmpty())

            println("CHANNEL ${chat.title} FETCHED ${proxyArray.size - start} / $size PROXIES")

        }

        val deferreds = mutableListOf<Deferred<Unit>>()

        fun fetchHttpChannel(name: String, channel: () -> List<String>) {

            deferreds.add(GlobalScope.async(Dispatchers.IO) {

                repeat(3) {

                    runCatching {

                        val start: Int

                        val size: Int

                        proxyArray.addAll(channel().also {
                            start = proxyArray.size
                            size = it.size
                        })

                        println("CHANNEL $name: FETCHED ${proxyArray.size - start} / $size PROXIES")

                        return@async

                    }

                }

            })

        }

        fetchHttpChannel("HiGram") { HttpFetcher.fetchHiGram() }
        fetchHttpChannel("Nitrogram") { HttpFetcher.fetchNitrogram() }
        fetchHttpChannel("ChatGera") { HttpFetcher.fetchChatGera() }
        fetchHttpChannel("ChatGera2") { HttpFetcher.fetchChatGera2() }
        fetchHttpChannel("fetchVGram") { HttpFetcher.fetchVGram() }
        fetchHttpChannel("Fungram") { HttpFetcher.fetchFungram() }
        fetchHttpChannel("ElGram") { HttpFetcher.fetchElGram() }
        fetchHttpChannel("ElGram2") { HttpFetcher.fetchElGram2() }
        fetchHttpChannel("Topmessager") { HttpFetcher.fetchTopmessager() }
        fetchHttpChannel("RozGram") { HttpFetcher.fetchRozGram() }
        fetchHttpChannel("NitroPlus") { HttpFetcher.fetchNitroPlus() }
        fetchHttpChannel("JetGram") { HttpFetcher.fetchJetGram() }
        fetchHttpChannel("Limogram") { HttpFetcher.fetchLimogram() }
        fetchHttpChannel("TeleVpn") { HttpFetcher.fetchTeleVpn() }
        fetchHttpChannel("MTProx") { HttpFetcher.fetchMTProx() }

        deferreds.awaitAll()

        proxyList = proxyArray.toList()

        waitForClose()

        exitProcess(0)

    }

    fun TdApi.Message.searchMTProxies(): HashSet<String> {

        val list = hashSetOf<String>()

        text?.also {

            it.split("\n").forEach {

                it.split(" ").forEach {

                    if (it.contains("proxy?")) {

                        list.add(it.parseLnk())

                    }

                }

            }

        }

        val buttons = replyMarkup

        if (buttons is TdApi.ReplyMarkupInlineKeyboard) {

            buttons.rows.forEach { row ->

                row.forEach {

                    val type = it.type

                    if (type is TdApi.InlineKeyboardButtonTypeUrl) {

                        if (type.url.contains("proxy?")) {

                            list.add(type.url.parseLnk())

                        }

                    }

                }

            }

        }

        return list

    }

    fun String.parseLnk() = "https://t.me/proxy?${substringAfter("proxy?")}"

}