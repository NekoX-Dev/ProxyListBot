package io.github.nekohasekai.plbot

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import java.io.File

// 所有代理列表
var proxyList: List<String>
    get() = File("proxy_list.json")
            .takeIf { it.isFile }
            ?.readText()
            ?.let { JSONArray(it) }
            ?.toList(String::class.java)
            ?: listOf()
    set(value) = File("proxy_list.json")
            .writeText(JSONArray(value).toStringPretty())

// 无效代理记录 链接: 原因
var invalidList: MutableMap<String,String>
    get() = File("proxy_list_invalid.json")
            .takeIf { it.isFile }
            ?.readText()
            ?.let {
                @Suppress("UNCHECKED_CAST")
                JSONObject(it) as MutableMap<String,String>
            }
            ?: hashMapOf()
    set(value) = File("proxy_list_invalid.json")
            .writeText(JSONObject(value).toStringPretty())

// 用于部署的代理列表
// 这个是NekoX 的历史遗留格式 懒得改了
var outputList: List<String>
    get() = File("proxy_list_output.json")
            .takeIf { it.isFile }
            ?.readText()
            ?.let { JSONArray(it) }
            ?.toList(JSONObject::class.java)
            ?.map { it.getStr("proxy") }
            ?: listOf()
    set(value) = File("proxy_list_output.json")
            .writeText(JSONArray(value.map { JSONObject(mapOf("proxy" to it, "desc" to "")) }).toString())