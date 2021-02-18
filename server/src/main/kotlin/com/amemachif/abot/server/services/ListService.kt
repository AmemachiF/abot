package com.amemachif.abot.server.services

import com.amemachif.abot.proto.Group
import com.amemachif.abot.proto.ListServiceGrpcKt
import com.amemachif.abot.proto.ServiceFriendGroupList
import com.amemachif.abot.server.SessionManager
import com.amemachif.abot.server.botCatching
import com.amemachif.abot.server.exceptions.TargetNotFoundException
import net.mamoe.mirai.contact.nameCardOrNick

class ListService : ListServiceGrpcKt.ListServiceCoroutineImplBase() {
    override suspend fun friendList(request: ServiceFriendGroupList.FriendGroupListRequest): ServiceFriendGroupList.FriendListResponse {
        return botCatching {
            val session = SessionManager.checkSession(request.sessionKey)
            val bot = session.ensuredBind().bot

            ServiceFriendGroupList.FriendListResponse.newBuilder()
                .apply {
                    bot.friends.forEach {
                        addDataBuilder().apply {
                            id = it.id
                            nickname = it.nick
                            remark = it.remark
                        }
                    }
                }
        }
    }

    override suspend fun groupList(request: ServiceFriendGroupList.FriendGroupListRequest): ServiceFriendGroupList.GroupListResponse {
        return botCatching {
            val bot = SessionManager.checkSession(request.sessionKey).ensuredBind().bot
            ServiceFriendGroupList.GroupListResponse.newBuilder()
                .apply {
                    bot.groups.forEach {
                        addDataBuilder().apply {
                            id = it.id
                            name = it.name
                            permission = Group.GroupPermission.forNumber(it.botPermission.level)
                        }
                    }
                }
        }
    }

    override suspend fun memberList(request: ServiceFriendGroupList.MemberListRequest): ServiceFriendGroupList.MemberListResponse {
        return botCatching {
            val bot = SessionManager.checkSession(request.sessionKey).ensuredBind().bot
            ServiceFriendGroupList.MemberListResponse.newBuilder()
                .apply {
                    val group = bot.getGroup(request.target) ?: throw TargetNotFoundException()
                    val groupInfo = Group.GroupInfo.newBuilder()
                        .apply {
                            id = group.id
                            name = group.name
                            permission = Group.GroupPermission.forNumber(group.botPermission.level)
                        }
                    group.members.forEach {
                        addDataBuilder().apply {
                            id = it.id
                            nick = it.nick
                            nameCard = it.nameCard
                            permission = Group.GroupPermission.forNumber(it.permission.level)
                            setGroup(groupInfo)
                        }
                    }
                }
        }
    }
}
