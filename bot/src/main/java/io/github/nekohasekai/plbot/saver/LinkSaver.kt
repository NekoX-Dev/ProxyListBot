package io.github.nekohasekai.plbot.saver

import io.github.nekohasekai.plbot.proxy.Proxy

interface LinkSaver<Type : Proxy> {

    fun toLink(proxy: Type): String
    fun toLink(protocol: String, proxy: Type): String

    companion object : LinkSaver<Proxy> {

        private val implements = hashMapOf<Class<out Proxy>, LinkSaver<out Proxy>>()

        fun <T : Proxy> addSaver(clazz: Class<T>, saver: LinkSaver<T>) = implements.put(clazz, saver)

        inline fun <reified T : Proxy> addSaver(saver: LinkSaver<T>) = addSaver(T::class.java, saver)

        override fun toLink(proxy: Proxy): String {

            fun <T : Proxy> LinkSaver<T>.toLinkInternal(proxy: Proxy) = @Suppress("UNCHECKED_CAST") toLink(proxy as T)

            val proxySaver = (implements[proxy::class.java] ?: error("no saver for ${proxy.javaClass.simpleName} found"))

            return proxySaver.toLinkInternal(proxy)

        }

        override fun toLink(protocol: String, proxy: Proxy): String {

            fun <T : Proxy> LinkSaver<T>.toLinkInternal(protocol: String, proxy: Proxy) = @Suppress("UNCHECKED_CAST") toLink(protocol, proxy as T)

            val proxySaver = (implements[proxy::class.java] ?: error("no saver for ${proxy.javaClass.simpleName} found"))

            return proxySaver.toLinkInternal(protocol, proxy)

        }

    }

}