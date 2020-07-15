package io.github.nekohasekai.pl_client

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.client.TdException
import io.github.nekohasekai.nekolib.core.raw.getChats
import io.github.nekohasekai.nekolib.core.raw.getFile
import io.github.nekohasekai.nekolib.core.utils.*
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoImpl
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoTester
import io.github.nekohasekai.nekolib.proxy.parser.StringParser
import io.github.nekohasekai.nekolib.proxy.tester.ProxyTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.yaml.snakeyaml.Yaml
import td.TdApi
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

object Launcher : TdCli() {

    override val loginType = LoginType.ALL

    val config = Yaml().loadAs(getFile("config.yml").bufferedReader(), HashMap::class.java)
    var logChannel = config["log_channel"].toString().toLong()
    var exchangeChannel = config["exchange_channel"].toString().toLong()

    @JvmStatic
    fun main(args: Array<String>) {

        TdLoader.tryLoad()

        start()

    }

    override fun onLoad() {

        options databaseDirectory "data/client"

        MTProtoImpl.init()

        MTProtoTester.onLoad(this)

        connectionManager.autoProxy = true

    }

    suspend fun log(str: String): TdApi.Message {

        defaultLog.info(str)

        return sudo make str disNtf true syncTo logChannel

    }

    suspend fun finish(str: String) {

        defaultLog.error(str)

        waitForClose()

        exitProcess(1)

    }

    override suspend fun onLogin() {

        super.onLogin()

        if (!isBot) getChats(TdApi.ChatListMain(), 0, 0, 114514)

        if (logChannel == -1L) {

            if (isBot) finish("Please specify log_channel")

            logChannel = me.chatId

        }

        if (exchangeChannel == -1L) {

            if (isBot) finish("Please specify exchange_channel")

            exchangeChannel = me.chatId

        }

        defaultLog.info("初始化完成")

    }

    override suspend fun onLogout() {

        super.onLogout()

        timer.cancel()

        finish("账号被登出")

    }

    override suspend fun onNewMessage(userId: Int, chatId: Long, message: TdApi.Message) {

        super.onNewMessage(userId, chatId, message)

        if (message.content !is TdApi.MessageDocument) return

        val document = message.content as TdApi.MessageDocument

        if (document.caption.text != "!check_proxy") return

        GlobalScope.launch(Dispatchers.IO) {

            val file = download(getFile(document.document.document.id))

            val proxyIds = hashMapOf<Proxy, Long>()

            val proxies = JSONArray(file.readText()).toList(JSONObject::class.java).map { obj ->

                StringParser.parseProxies(obj.getStr("link"))[0].also {

                    proxyIds[it] = obj.getLong("id")

                }

            }.toMutableLinkedList()

            val totalCount = proxies.size

            val retest = totalCount > 500

            val status = log("待检查: ${proxies.size}")

            val threads = 16

            val exec = Executors.newFixedThreadPool(threads)

            val index = AtomicInteger()

            val result = JSONArray()

            repeat(threads) {

                exec.execute {

                    runBlocking {

                        while (proxies.isNotEmpty()) {

                            val entity = synchronized(proxies) { proxies.remove() }

                            var i = -1

                            try {

                                val ping = ProxyTester.testProxy(entity, if (retest) 1 else 2)

                                i = index.incrementAndGet()

                                println("[${i}/$totalCount] ${entity}: 可用, ${ping}ms.")

                                if (i % 10 == 0) {

                                    sudo make "[${i}/$totalCount] ${entity}: 可用, ${ping}ms." syncEditTo status

                                }

                                result.add(JSONObject().apply {

                                    set("id", proxyIds[entity])
                                    set("status", 2)
                                    set("message", "$ping")

                                })

                            } catch (e: TdException) {

                                i = index.incrementAndGet()

                                println("[${i}/$totalCount] ${entity}: ${e.message}.")

                                if (i % 10 == 0) {

                                    sudo make "[${i}/$totalCount] ${entity}: ${e.message}." syncEditTo status

                                }

                                result.add(JSONObject().apply {

                                    set("id", proxyIds[entity])
                                    set("status", 1)
                                    set("message", e.message)

                                })

                            }

                            if (i == totalCount) {

                                val cacheFile = getFile("cache/${System.currentTimeMillis()}")

                                cacheFile.parentFile.mkdirs()

                                cacheFile.writeText(result.toString())

                                sudo make "检查完成" editTo status

                                sudo makeFile cacheFile captionText "!check_result" syncTo exchangeChannel

                                println("检查完成")

                            }

                        }

                    }

                }

            }

        }

    }

}