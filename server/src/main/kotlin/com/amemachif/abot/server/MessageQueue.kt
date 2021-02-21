package com.amemachif.abot.server

import com.amemachif.abot.proto.MessageEvent
import net.mamoe.mirai.event.events.BotEvent
import java.util.concurrent.ConcurrentLinkedDeque

class MessageQueue : ConcurrentLinkedDeque<BotEvent>() {
    var messageSize = 4096

    suspend fun fetch(size: Int = 10, latest: Boolean = false): List<MessageEvent.EventInfo> {
        var count = size

        val ret = ArrayList<MessageEvent.EventInfo>(count)
        while (!this.isEmpty() && count > 0) {
            val event = if (latest) removeLast() else pop()
            event.toEventInfo()?.also {
                ret.add(it)
                count--
            }
        }
        return ret
    }

    suspend fun peek(size: Int = 10, latest: Boolean = false): List<MessageEvent.EventInfo> {
        var count = size
        val ret = ArrayList<MessageEvent.EventInfo>(count)

        val iterator: Iterator<BotEvent> = (if (latest) reversed() else this).iterator()

        while (iterator.hasNext() && count > 0) {
            iterator.next().toEventInfo()?.also {
                ret.add(it)
                count--
            }
        }

        return ret
    }

    override fun add(element: BotEvent?): Boolean {
        while (size >= messageSize) removeFirst()
        return super.add(element)
    }
}
