package io.github.nekohasekai.plbot.channel

import io.github.nekohasekai.plbot.proxy.Proxy

interface Channel {

    val name: String

    fun fetchProxies(): Collection<Proxy>

}