package io.github.nekohasekai.plbot.channel.impl

import io.github.nekohasekai.plbot.channel.TdChannel

// 频道来源
val channels = mutableSetOf(
        "socks5list",
        "onessr",
        "MTProtoShare",
        "cnhumanright99",
        "chinagrassroot",
        "mtpclub",
        "prossh"
)

fun createTelegramChannels() = channels.map { TdChannel.create(it) }