package com.amemachif.abot.server

import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.Error
import com.amemachif.abot.proto.Session
import com.amemachif.abot.proto.UserInfo
import com.amemachif.abot.server.exceptions.BotException
import com.amemachif.abot.server.exceptions.ErrorException
import com.amemachif.abot.server.exceptions.NoPermissionException
import com.amemachif.abot.server.exceptions.TargetNotFoundException
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Message
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.OnlineMessageSource
import kotlin.reflect.full.functions
import kotlin.reflect.full.staticFunctions

inline fun <T> botCatching(trying: () -> T, catching: (BotException) -> T): T {
    return try {
        try {
            trying()
        } catch (e: PermissionDeniedException) {
            throw NoPermissionException(e.message, e)
        } catch (e: BotException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw ErrorException(e.message, e)
        }
    } catch (e: BotException) {
        catching(e)
    }
}

inline fun <reified R : GeneratedMessageV3, T : GeneratedMessageV3.Builder<*>> botCatching(block: () -> T): R {
    return botCatching({
        val result = block()
            .apply {
                this.setCode(ErrorCode.OK.code, ErrorCode.OK.message)
            }.build()
        if (result is R) {
            result
        } else {
            throw NoSuchMethodException()
        }
    }, { e ->
        e.printStackTrace()
        val newBuilder = R::class.staticFunctions.find { it.name == "newBuilder" && it.parameters.count() == 0 }
            ?: throw NoSuchMethodException()
        val builder = newBuilder.call() ?: throw NoSuchMethodException()
        if (builder is Message.Builder) {
            val build = builder::class.functions.find { it.name == "build" } ?: throw NoSuchMethodException()
            builder.setCode(e.error.code, e.message)
            val result = build.call(builder)
            return if (result is R) {
                result
            } else {
                throw NoSuchMethodException()
            }
        } else {
            throw NoSuchMethodException()
        }
    })
}

fun <T : Message.Builder> T.setCode(code: Int, message: String?): T {
    val getCodeBuilder = this::class.functions.find { it.name == "getCodeBuilder" }
    val codeBuilder = getCodeBuilder?.call(this)
    if (codeBuilder is Error.ErrorMessage.Builder) {
        codeBuilder.apply {
            this.code = Error.Code.forNumber(code)
            this.message = message ?: "UNKNOWN"
            this.codeNum = code
        }
    }
    return this
}

fun Bot.ensureGroup(groupId: Long): Group {
    return this.getGroup(groupId) ?: throw TargetNotFoundException()
}

fun Group.ensureMember(memberId: Long): NormalMember {
    return this.getMember(memberId) ?: throw TargetNotFoundException()
}

fun Bot.ensureFriend(qq: Long): Friend {
    return this.getFriend(qq) ?: throw TargetNotFoundException()
}

fun commonResponseBuilder(): Common.CommonEmptyResponse.Builder {
    return Common.CommonEmptyResponse.newBuilder()
}

fun Session.SessionKey.ensureBot(): Bot {
    return SessionManager.checkSession(this).ensuredBind().bot
}

fun Session.SessionKey.ensureBind(): SessionManager.Session.Bind {
    return SessionManager.checkSession(this).ensuredBind()
}

fun Session.SessionKey.pushCache(receipt: MessageReceipt<*>) {
    SessionManager.checkSession(this).ensuredBind().cacheQueue.add(receipt.source)
}

fun Session.SessionKey.getCache(id: Int?): OnlineMessageSource? {
    return SessionManager.checkSession(this).ensuredBind().cacheQueue[id]
}

internal fun String.toHexArray(): ByteArray = ByteArray(length / 2) {
    ((Character.digit(this[it * 2], 16) shl 4) + Character.digit(this[it * 2 + 1], 16)).toByte()
}

fun com.amemachif.abot.proto.Group.GroupInfo.Builder.applyBy(group: Group) {
    id = group.id
    name = group.name
    permission = com.amemachif.abot.proto.Group.GroupPermission.forNumber(group.botPermission.level)
}

fun com.amemachif.abot.proto.Group.GroupMemberDetails.Builder.applyBy(member: Member) {
    id = member.id
    nick = member.nick
    nameCard = member.nameCard
    permission = com.amemachif.abot.proto.Group.GroupPermission.forNumber(member.permission.level)
    groupBuilder.applyBy(member.group)
}

fun UserInfo.FriendInfo.Builder.applyBy(user: User) {
    id = user.id
    nickname = user.nick
    remark = user.remark
}

inline fun <T> Array<out T>.allNot(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return false
    return true
}
