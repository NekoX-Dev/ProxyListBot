package io.github.nekohasekai.plbot.parser

import cn.hutool.json.JSONUtil
import io.github.nekohasekai.plbot.proxy.Proxy

object StringParser : Parser<String> {

    override fun parseProxies(value: String): Collection<Proxy> {

        if (JSONUtil.isJson(value)) {

            // 从 JSON 解析

            return JSONParser.parseProxies(JSONUtil.parse(value))

        }

        return mutableSetOf<Proxy>().apply {

            value.split('\n').forEach { line ->

                line.split(' ').forEach {

                    if (JSONUtil.isJson(value)) {

                        addAll(JSONParser.parseProxies(JSONUtil.parse(it)))

                    } else {

                        addAll(LinkParser.parseProxies(it))

                    }

                }

            }

        }

    }

}