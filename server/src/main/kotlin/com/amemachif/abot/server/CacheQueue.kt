package com.amemachif.abot.server

import com.amemachif.abot.server.exceptions.TargetNotFoundException
import net.mamoe.mirai.message.data.OnlineMessageSource

class CacheQueue : LinkedHashMap<Int, OnlineMessageSource>() {
    var cacheSize = 4096

    override fun get(key: Int): OnlineMessageSource = super.get(key) ?: throw TargetNotFoundException()

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, OnlineMessageSource>?): Boolean = size > cacheSize

    fun add(source: OnlineMessageSource) {
        put(source.ids.firstOrNull() ?: 0, source)
    }
}
