package io.github.nekohasekai.plbot

import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdClient
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.raw.getChatHistory
import io.github.nekohasekai.nekolib.core.raw.searchPublicChat
import io.github.nekohasekai.nekolib.core.utils.text
import kotlinx.coroutines.*
import td.TdApi
import kotlin.system.exitProcess

/**
 * 代理搜索脚本
 *
 * 需要国际网络环境
 */
object ProxyFetcher : TdCli() {

    override val loginType = LoginType.USER

    // 频道来源
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

    // 后面三个频道消息数较多 没有缓存的情况读取较慢

    init {

        // 数据文件目录
        options databaseDirectory "data/fetcher"

        // 不要缓存聊天 否则 getChatHistory 一次之后只会返回缓存
        // 还没试过有没有用 建议每次都删除 *.sqlite
        // binlog 是认证信息 其他都是缓存
        options useChatInfoDatabase false

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        // 加载 TDLib
        TdLoader.tryLoad()

        // 登录程序
        waitForLogin()

        val proxyArray = proxyList.toMutableSet()

        val deferreds = mutableListOf<Deferred<Unit>>()

        // 其他来源 由于HTTP读入较慢 使用多线程
        fun fetchHttpChannel(name: String, retry: Boolean = true, channel: () -> Collection<String>) {

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

                        if (retry && proxyArray.size - start > 0) {

                            fetchHttpChannel(name, retry, channel)

                        }

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
        fetchHttpChannel("TeleVpn",false) { HttpFetcher.fetchTeleVpn() }
        fetchHttpChannel("MTProx",false) { HttpFetcher.fetchMTProx() }
        fetchHttpChannel("FlyChat",false) { HttpFetcher.fetchFlyChat() }
        fetchHttpChannel("GifProxy",false) { HttpFetcher.fetchGifProxy() }

        channels.forEach { channel ->

            val chat = searchPublicChat(channel)

            val start = proxyArray.size

            var history: TdApi.Messages
            var from = 0L
            var size = 0

            do {

                try {

                    // 读消息
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

                    // 从消息里搜索链接
                    message.searchMTProxies().forEach {

                        proxyArray.add(it)

                        size++

                    }

                }

            } while (history.messages.isNotEmpty())

            println("CHANNEL ${chat.title} FETCHED ${proxyArray.size - start} / $size PROXIES")

        }

        deferreds.awaitAll()

        proxyList = proxyArray.toList()

        waitForClose()

        exitProcess(0)

    }

    // 从消息文本和按钮中找链接
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

    // 全都换成标准格式
    fun String.parseLnk() = "https://t.me/proxy?${substringAfter("proxy?")}"

}