package com.amemachif.abot.server

import net.mamoe.mirai.message.data.Face
import kotlin.reflect.full.memberProperties

object FaceMap {
    private val id2Name = mutableMapOf<Int, String>()
    private val name2Id = mutableMapOf<String, Int>()

    init {
        Face.Companion::class.memberProperties.forEach {
            val n = it.name
            val i = with(it.get(Face.Companion)) {
                if (this is Int) this
                else return@forEach
            }
            id2Name[i] = n
            name2Id[n] = i
        }
    }

    operator fun get(id: Int) = id2Name[id] ?: "未知表情"
    operator fun get(name: String) = name2Id[name] ?: 0xff
}