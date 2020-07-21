package io.github.nekohasekai.pl_serevr.channel.impl

import io.github.nekohasekai.pl_serevr.channel.TdChannel

// 频道来源
val channels = mutableSetOf(
        "socks5list",
        "onessr",
        "MTProtoShare",
        "cnhumanright99",
        "chinagrassroot",
        "mtpclub",
        "prossh",
        "googlessrr",
        "Rocketcool"
)

fun createTelegramChannels() = channels.map { TdChannel.create(it) }