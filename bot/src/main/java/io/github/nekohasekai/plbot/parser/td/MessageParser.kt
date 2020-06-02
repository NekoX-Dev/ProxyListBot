package io.github.nekohasekai.plbot.parser.td

import io.github.nekohasekai.nekolib.core.utils.text
import io.github.nekohasekai.plbot.parser.LinkParser
import io.github.nekohasekai.plbot.parser.Parser
import io.github.nekohasekai.plbot.parser.StringParser
import io.github.nekohasekai.plbot.proxy.Proxy
import td.TdApi

object MessageParser : Parser<TdApi.Message> {

    override fun parseProxies(value: TdApi.Message): Collection<Proxy> = mutableSetOf<Proxy>().apply {

        value.text?.also {

            addAll(StringParser.parseProxies(it))

        }

        val buttons = value.replyMarkup

        if (buttons is TdApi.ReplyMarkupInlineKeyboard) {

            buttons.rows.forEach { row ->

                row.forEach {

                    val type = it.type

                    if (type is TdApi.InlineKeyboardButtonTypeUrl) {

                        addAll(LinkParser.parseProxies(type.url))

                    }

                }

            }

        }

    }

}