package io.github.nekohasekai.plbot.parser

import cn.hutool.json.JSON
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.github.nekohasekai.plbot.proxy.Proxy

object JSONParser : Parser<JSON> {

    override fun parseProxies(value: JSON): Collection<Proxy> = mutableSetOf<Proxy>().apply parse@{

        if (value is JSONArray) {

            value.forEach {

                addAll(Parser.parseProxies(it))

            }

        } else if (value is JSONObject) {

            var noSubEntity = true

            value.forEach { (key, value) ->

                if (value is JSONArray) {

                    addAll(parseProxies(value))

                    noSubEntity = false

                } else if (value is JSONObject) {

                    addAll(parseProxies(value))

                    noSubEntity = false

                } else if (value is String) {

                    addAll(StringParser.parseProxies(value).also {

                        if (it.isNotEmpty()) {

                            addAll(it)

                            noSubEntity = false

                        }

                    })

                }


            }

            if (!noSubEntity) return@parse

            addAll(MapParser.parseMap(value))

        }

    }

}