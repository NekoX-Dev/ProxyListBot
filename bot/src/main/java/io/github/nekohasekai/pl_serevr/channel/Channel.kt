package io.github.nekohasekai.pl_serevr.channel

import io.github.nekohasekai.nekolib.proxy.impl.Proxy

interface Channel {

    val name: String
    val async: Boolean

    fun fetchProxies(): Collection<Proxy>

}