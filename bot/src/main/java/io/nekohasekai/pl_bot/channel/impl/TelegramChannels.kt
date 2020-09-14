package io.nekohasekai.pl_bot.channel.impl

import io.nekohasekai.pl_bot.channel.TdChannel

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