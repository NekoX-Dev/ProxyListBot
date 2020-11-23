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
        "Rocketcool",
        "Gramip",
        "mianfeidaili",
        "FreeProxyfortelegran",
        "ProxyFinder",
        "MTP_roto",
        "ProxyMTProto",
        "MTProtoProxies",
        "iMTProto",
        "DLProxy",
        "MTProtoTG",
        "TrueProxy",
        "MTProxies",
        "ParisProxy",
        "proxys_telegram",
        "hideproxi",
        "telproxy2019",
        "TelMTProto",
        "hotspotproxy",
        "Get_MTProto",
        "Mtproxeiss",
        "version2_mt",
        "GlypeX"
)

fun createTelegramChannels() = channels.map { TdChannel.create(it) }