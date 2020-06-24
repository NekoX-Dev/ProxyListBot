package io.github.nekohasekai.plbot.proxy

import org.dizitart.no2.mapper.Mappable

interface Proxy : Comparable<Proxy>,Mappable {

    fun strictKey(): String

}