package io.github.nekohasekai.pl_serevr.channel.impl

import cn.hutool.json.JSONObject
import io.github.nekohasekai.nekolib.core.utils.getValue
import io.github.nekohasekai.nekolib.core.utils.setValue
import io.github.nekohasekai.pl_serevr.Fetcher
import io.github.nekohasekai.pl_serevr.channel.HttpChannel
import io.github.nekohasekai.nekolib.proxy.parser.JSONParser
import io.github.nekohasekai.nekolib.proxy.impl.Proxy
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

//                    thisPage.forEach {
//
//                        runBlocking {
//
//                            try {
//
//                                it as MTProtoProxy
//
//                                println(it.secret)
//
//                                val ping = ProxyTester.testProxy(it, 1)
//
//                                println("$it: 可用, ${ping}ms.")
//
//                            } catch (e: Exception) {
//
//                                println("$it: ${e.message}.")
//
//                            }
//
//                        }
//
//                    }

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