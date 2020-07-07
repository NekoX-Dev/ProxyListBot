package io.github.nekohasekai.pl_serevr

import cn.hutool.core.codec.Base64
import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.RuntimeUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.cli.TdCli
import io.github.nekohasekai.nekolib.cli.TdLoader
import io.github.nekohasekai.nekolib.core.raw.getChats
import io.github.nekohasekai.nekolib.core.raw.getFile
import io.github.nekohasekai.nekolib.core.utils.*
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
import io.github.nekohasekai.nekolib.proxy.impl.mtproto.MTProtoProxy
import io.github.nekohasekai.nekolib.proxy.saver.LinkSaver
import io.github.nekohasekai.nekolib.utils.GetIdCommand
import io.github.nekohasekai.pl_serevr.channel.Channel
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelFlameProxy
import io.github.nekohasekai.pl_serevr.channel.impl.ChannelTeleVpn
import io.github.nekohasekai.pl_serevr.channel.impl.createHttpChannels
import io.github.nekohasekai.pl_serevr.channel.impl.createTelegramChannels
import io.github.nekohasekai.pl_serevr.database.ProxyDatabase
import io.github.nekohasekai.pl_serevr.database.ProxyEntity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.dizitart.no2.objects.filters.ObjectFilters
import org.yaml.snakeyaml.Yaml
import td.TdApi
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

object Launcher : TdCli() {

    override val loginType = LoginType.USER

    val config = Yaml().loadAs(getFile("config.yml").bufferedReader(), HashMap::class.java)
    var logChannel = config["log_channel"].toString().toLong()
    var exchangeChannel = config["exchange_channel"].toString().toLong()

    @JvmStatic
    fun main(args: Array<String>) {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))

        TdLoader.tryLoad()

        start()

    }

    override fun onLoad() {

        addHandler(GetIdCommand())

    }

    suspend fun log(str: String) {

        defaultLog.info(str)

        sudo make str disNtf true sendTo logChannel

    }

    suspend fun finish(str: String) {

        defaultLog.error(str)

        waitForClose()

        exitProcess(1)

    }

    override suspend fun onLogin() {

        super.onLogin()

        getChats(TdApi.ChatListMain(), 0, 0, 114514)

        if (logChannel == -1L) {

            if (isBot) finish("Please specify log_channel")

            logChannel = me.chatId

        }

        if (exchangeChannel == -1L) {

            if (isBot) finish("Please specify exchange_channel")

            exchangeChannel = me.chatId

        }

        timer.schedule(pullTask(), Date(nextHour()), 60 * 60 * 1000L)

        defaultLog.info("启动定时任务")

        File("exec").applyIf({ isFile }) {

            delete()

            pullTask().run()

        }


    }

    override suspend fun onLogout() {

        super.onLogout()

        timer.cancel()

        finish("账号被登出")

    }

    val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    val channels = arrayListOf<Channel>()

    init {

        channels.add(ChannelTeleVpn)
        channels.add(ChannelFlameProxy)
        // channels.add(ChannelMyProxy)
        channels.addAll(createHttpChannels())
        channels.addAll(createTelegramChannels().map { it.apply { onLoad(this@Launcher) } })

    }

    var count by AtomicInteger()

    fun pullTask() = timerTask {

        GlobalScope.launch(Dispatchers.IO) {

            log("开始")

            val retest = Date().hours == 0
            if (retest) count = 0

            fetch()

            check(retest)

        }

    }

    suspend fun fetch() {

        val exists = ProxyDatabase.table.find().map { it.proxy.toString() }.toMutableSet()

        val size = ProxyDatabase.table.find().totalCount()

        println("开始拉取, 已有: $size.")

        val proxies = hashSetOf<Proxy>()

        val deferreds = LinkedList<Deferred<Unit>>()

        channels.forEach { channel ->

            fun runFetch() {

                runCatching {

                    val before = proxies.size
                    val size: Int

                    proxies.addAll(channel.fetchProxies().also {

                        size = it.size

                    }.toMutableSet().apply {

                        iterator().apply {

                            forEach {

                                if (exists.contains(it.toString())) {

                                    remove()

                                }

                            }

                        }

                        forEach {

                            ProxyDatabase.table.insert(ProxyEntity().apply { proxy = it })

                        }

                    })

                    synchronized(this@Launcher) {

                        print("${channel.name}: ")

                        println("代理数量: $size, 不重复数量: ${proxies.size - before}.")

                    }

                }.onFailure {

                    it.printStackTrace()

                    println("出错")

                }

            }

            if (channel.async) {

                deferreds.add(GlobalScope.async(Dispatchers.IO) { runFetch() })

            } else {

                deferreds.awaitAll()

                runFetch()

            }

        }

        val new = ProxyDatabase.table.find().totalCount() - size

        log("拉取完成, 新增代理: $new.")

    }

    suspend fun check(all: Boolean) {

        val proxiesToCheck = if (all) ProxyDatabase.table.find() else ProxyDatabase.table.find(ObjectFilters.or(ObjectFilters.eq("status", ProxyEntity.UNCHECKED), ObjectFilters.eq("status", ProxyEntity.AVAILABLE)))

        val cacheFile = getFile("cache/${System.currentTimeMillis()}")

        cacheFile.parentFile.mkdirs()

        val array = JSONArray()

        proxiesToCheck.forEach {

            array.add(JSONObject().apply {

                set("id", it._id)
                set("link", LinkSaver.toLink(it.proxy))

            })

        }

        cacheFile.writeText(array.toStringPretty())

        sudo makeFile cacheFile captionText "!check_proxy" syncTo exchangeChannel

    }

    override suspend fun onNewMessage(userId: Int, chatId: Long, message: TdApi.Message) {

        super.onNewMessage(userId, chatId, message)

        if (message.content !is TdApi.MessageDocument) return

        val document = message.content as TdApi.MessageDocument

        if (document.caption.text == "!check_result") GlobalScope.launch(Dispatchers.IO) {

            val file = download(getFile(document.document.document.id))

            val result = JSONArray(file.readText())

            result.toList(JSONObject::class.java).forEach {

                val entity = ProxyDatabase.table[it.getLong("id")]!!

                entity.status = it.getInt("status")

                entity.message = it.getStr("message")

                ProxyDatabase.table.update(entity)

            }

            defaultLog.info("解析完成, 总处理: ${result.size}")

            export()
            publish()

        }

    }

    suspend fun export() {

        val available = ProxyDatabase.table.find(ObjectFilters.eq("status", ProxyEntity.AVAILABLE))

        val all = available.totalCount()

        val siMap = hashMapOf<String, ProxyEntity>()

        available.toList().forEach {

            siMap[it.proxy.strictKey()] = it

        }

        val node = siMap.values.map { ExportItem(it.proxy, LinkSaver.toLink(it.proxy), it.message!!.toInt()) }.let { TreeSet(it) }

        log("所有: $all, 可用: ${node.size}.")

        // 旧格式

        var mdText = "# ProxyList\n\n由 [ProxyListBot](https://github.com/NekoX-Dev/ProxyListBot) 自动采集, 自动获取完整列表请使用 [Nekogram X](https://github.com/NekoX-Dev/NekoX) :)\n"

        (if (node.size > 50) node.toList().subList(0, 50) else node).forEachIndexed { index, item ->

            mdText += "\n${index + 1}. [${item.proxy}](${item.link})  "
            val proxy = item.proxy
            if (proxy is MTProtoProxy) {
                mdText += "\n  **服务器**: `${proxy.server}`  "
                mdText += "\n  **端口**: `${proxy.port}`  "
                mdText += "\n  **密钥**: `${proxy.secret}`  "

            }
            mdText += "\n"

        }

        val time = DateUtil.formatChineseDate(Date(), false)

        mdText += "\n\n上次更新时间: $time"

        File("proxy_list_output.md").writeText(mdText)

        File("proxy_list_output").writeText(node.joinToString("\n") { it.link }.let { Base64.encode(it) })

    }

    class ExportItem(val proxy: Proxy, val link: String, val ping: Int) : Comparable<ExportItem> {

        override fun compareTo(other: ExportItem): Int {

            if (ping == other.ping) return link.compareTo(other.link)

            return ping - other.ping

        }

    }

    suspend fun publish() {

        val bash = when (TdLoader.NativeTarget.current()) {
            TdLoader.NativeTarget.Win32 -> "C:/Program Files (x86)/Git/bin/bash.exe"
            TdLoader.NativeTarget.Win64 -> "C:/Program Files/Git/bin/bash.exe"
            else -> "bash"
        }

        val process = RuntimeUtil.exec("$bash ${getPath("publish.sh")}")

        if (process.waitFor() != 0) {

            log("推送错误")

        } else {

            log("推送完成")

        }

        println(RuntimeUtil.getResult(process))

    }


}