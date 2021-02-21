package com.amemachif.abot.server

import com.amemachif.abot.proto.BotEvents
import com.amemachif.abot.proto.Group
import com.amemachif.abot.proto.MessageEvent
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.MiraiExperimentalApi

@OptIn(MiraiExperimentalApi::class)
suspend fun BotEvent.toEventInfo(): MessageEvent.EventInfo? {
    return MessageEvent.EventInfo.newBuilder().apply {
        when (this@toEventInfo) {
            is FriendMessageEvent -> friendMessageBuilder.apply {
                messageChainBuilder.addAllChain(this@toEventInfo.message.toMessageChainList())
                senderBuilder.applyBy(this@toEventInfo.sender)
            }
            is GroupMessageEvent -> groupMessageBuilder.apply {
                messageChainBuilder.addAllChain(this@toEventInfo.message.toMessageChainList())
                senderBuilder.applyBy(this@toEventInfo.sender)
            }
            is GroupTempMessageEvent -> tempMessageBuilder.apply {
                messageChainBuilder.addAllChain(this@toEventInfo.message.toMessageChainList())
                senderBuilder.applyBy(this@toEventInfo.sender)
            }
            is BotOnlineEvent -> botOnlineEventBuilder.apply {
                qq = this@toEventInfo.bot.id
            }
            is BotOfflineEvent.Active -> botOfflineEventActiveBuilder.apply {
                qq = this@toEventInfo.bot.id
            }
            is BotOfflineEvent.Force -> botOfflineEventForceBuilder.apply {
                qq = this@toEventInfo.bot.id
            }
            is BotOfflineEvent.Dropped -> botOfflineEventDroppedBuilder.apply {
                qq = this@toEventInfo.bot.id
            }
            is BotReloginEvent -> botReLoginEventBuilder.apply {
                qq = this@toEventInfo.bot.id
            }
            is BotGroupPermissionChangeEvent -> botGroupPermissionChangeEventBuilder.apply {
                origin = Group.GroupPermission.forNumber(this@toEventInfo.origin.level)
                current = Group.GroupPermission.forNumber(this@toEventInfo.new.level)
                groupBuilder.applyBy(this@toEventInfo.group)
            }
            is BotMuteEvent -> botMuteEventBuilder.apply {
                durationBuilder.seconds = this@toEventInfo.durationSeconds.toLong()
                operatorBuilder.applyBy(this@toEventInfo.operator)
            }
            is BotUnmuteEvent -> botUnmuteEventBuilder.apply {
                operatorBuilder.applyBy(this@toEventInfo.operator)
            }
            is BotJoinGroupEvent -> botJoinGroupEventBuilder.apply {
                groupBuilder.applyBy(this@toEventInfo.group)
            }
            is BotLeaveEvent.Active -> botLeaveEventActiveBuilder.apply {
                groupBuilder.applyBy(this@toEventInfo.group)
            }
            is BotLeaveEvent.Kick -> botLeaveEventKickBuilder.apply {
                groupBuilder.applyBy(this@toEventInfo.group)
            }
            is MessageRecallEvent.GroupRecall -> groupRecallEventBuilder.apply {
                authorId = this@toEventInfo.authorId
                messageId = this@toEventInfo.messageIds.firstOrNull() ?: 0
                timeBuilder.seconds = this@toEventInfo.messageTime.toLong()
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is MessageRecallEvent.FriendRecall -> friendRecallEventBuilder.apply {
                authorId = this@toEventInfo.authorId
                messageId = this@toEventInfo.messageIds.firstOrNull() ?: 0
                timeBuilder.seconds = this@toEventInfo.messageTime.toLong()
            }
            is GroupNameChangeEvent -> groupNameChangeEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is GroupEntranceAnnouncementChangeEvent -> groupEntranceAnnouncementChangeEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is GroupMuteAllEvent -> groupMuteAllEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is GroupAllowAnonymousChatEvent -> groupAllowAnonymousChatEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is GroupAllowConfessTalkEvent -> groupAllowConfessTalkEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                isByBot = this@toEventInfo.isByBot
            }
            is GroupAllowMemberInviteEvent -> groupAllowMemberInviteEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                groupBuilder.applyBy(this@toEventInfo.group)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is MemberJoinEvent -> memberJoinEventBuilder.apply {
                memberBuilder.applyBy(this@toEventInfo.member)
            }
            is MemberLeaveEvent.Kick -> memberLeaveEventKickBuilder.apply {
                memberBuilder.applyBy(this@toEventInfo.member)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is MemberLeaveEvent.Quit -> memberLeaveEventQuitBuilder.apply {
                memberBuilder.applyBy(this@toEventInfo.member)
            }
            is MemberCardChangeEvent -> memberCardChangeEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                memberBuilder.applyBy(this@toEventInfo.member)
            }
            is MemberSpecialTitleChangeEvent -> memberSpecialTitleChangeEventBuilder.apply {
                origin = this@toEventInfo.origin
                current = this@toEventInfo.new
                memberBuilder.applyBy(this@toEventInfo.member)
            }
            is MemberPermissionChangeEvent -> memberPermissionChangeEventBuilder.apply {
                origin = Group.GroupPermission.forNumber(this@toEventInfo.origin.level)
                current = Group.GroupPermission.forNumber(this@toEventInfo.new.level)
                memberBuilder.applyBy(this@toEventInfo.member)
            }
            is MemberMuteEvent -> memberMuteEventBuilder.apply {
                durationBuilder.seconds = this@toEventInfo.durationSeconds.toLong()
                memberBuilder.applyBy(this@toEventInfo.member)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is MemberUnmuteEvent -> memberUnmuteEventBuilder.apply {
                memberBuilder.applyBy(this@toEventInfo.member)
                this@toEventInfo.operator?.let { operatorBuilder.applyBy(it) }
            }
            is NewFriendRequestEvent -> newFriendRequestEventBuilder.apply {
                eventId = this@toEventInfo.eventId
                fromId = this@toEventInfo.fromId
                groupId = this@toEventInfo.fromGroupId
                nick = this@toEventInfo.fromNick
                message = this@toEventInfo.message
            }
            is MemberJoinRequestEvent -> memberJoinRequestEventBuilder.apply {
                eventId = this@toEventInfo.eventId
                fromId = this@toEventInfo.fromId
                groupId = this@toEventInfo.groupId
                groupName = this@toEventInfo.groupName
                nick = this@toEventInfo.fromNick
                message = this@toEventInfo.message
            }
            is BotInvitedJoinGroupRequestEvent -> botInvitedJoinGroupRequestEventBuilder.apply {
                eventId = this@toEventInfo.eventId
                fromId = this@toEventInfo.invitorId
                groupId = this@toEventInfo.groupId
                nick = this@toEventInfo.groupName
            }
        }
    }.build()
}
