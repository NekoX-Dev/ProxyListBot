package io.nekohasekai.pl_bot.channel.impl

import cn.hutool.json.JSONObject
import io.nekohasekai.ktlib.core.getValue
import io.nekohasekai.ktlib.core.setValue
import io.nekohasekai.pl_bot.Fetcher
import io.nekohasekai.pl_bot.channel.HttpChannel
import io.nekohasekai.td.proxy.parser.JSONParser
import io.nekohasekai.td.proxy.impl.Proxy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object ChannelMyProxy : HttpChannel() {

    override val name = "MyProxy"

    var finished by AtomicBoolean()
    var currentPage = AtomicInteger()

    override fun buildRequest(): Request {

        val nextPage = currentPage.incrementAndGet()

        if (nextPage % 10 == 0) {

            println("fetching page $nextPage")

        }

        return getRequest("https://mytele.vidiato.net/panel/getProxies.php?page=$nextPage")

    }

    override fun fetchProxies() = runBlocking {

        finished = false
        currentPage.set(0)

        val pool = Executors.newFixedThreadPool(9)

        val proxies = HashSet<Proxy>()

        fun nextRequest() {

            if (finished) return

            pool.execute {

                val response = Fetcher.okHttpClient.newCall(buildRequest()).execute()

                val json = JSONObject(response.body!!.string())

                if (!json.getBool("success", false)) {

                    println("page $currentPage, finished (${json.getStr("message")}).")

                    finished = true

                }

                val thisPage = JSONParser.parseProxies(json).toMutableSet().apply {

                    iterator().apply {

                        forEach {

                            if (Fetcher.exists.contains(it.toString())) {

                                remove()

                            }

                        }

                    }

                }

                if (thisPage.isEmpty()) {

                    println("page $currentPage, finished (exists).")

                    finished = true

                } else {

                    proxies.addAll(thisPage)

                }

                if (finished) return@execute

                try {

                    nextRequest()

                } catch (e: Exception) {

                    e.printStackTrace()

                    finished = true

                }

            }

        }

        repeat(6) {

            nextRequest()

        }

        while (!finished) delay(1000L)

        proxies

    }

}