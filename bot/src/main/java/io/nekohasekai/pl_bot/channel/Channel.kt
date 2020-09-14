package io.nekohasekai.pl_bot.channel

import io.nekohasekai.td.proxy.impl.Proxy

interface Channel {

    val name: String
    val async: Boolean

    fun fetchProxies(): Collection<Proxy>

}