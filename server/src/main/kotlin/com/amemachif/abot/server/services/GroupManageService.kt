package com.amemachif.abot.server.services

import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.GroupManageServiceGrpcKt
import com.amemachif.abot.proto.ServiceGroupManage
import com.amemachif.abot.server.*
import net.mamoe.mirai.utils.MiraiExperimentalApi

class GroupManageService : GroupManageServiceGrpcKt.GroupManageServiceCoroutineImplBase() {
    override suspend fun mute(request: ServiceGroupManage.MuteRequest): Common.CommonEmptyResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.target)
                .ensureMember(request.memberId)
            member.mute(request.time.seconds.toInt())
            commonResponseBuilder()
        }
    }

    override suspend fun unmute(request: ServiceGroupManage.UnmuteRequest): Common.CommonEmptyResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.target)
                .ensureMember(request.memberId)
            member.unmute()
            commonResponseBuilder()
        }
    }

    override suspend fun kick(request: ServiceGroupManage.KickRequest): Common.CommonEmptyResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.target)
                .ensureMember(request.memberId)
            member.kick(request.msg)
            commonResponseBuilder()
        }
    }

    override suspend fun quit(request: ServiceGroupManage.QuitRequest): Common.CommonEmptyResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(request.target)
            group.quit()
            commonResponseBuilder()
        }
    }

    override suspend fun muteAll(request: ServiceGroupManage.MuteUnmuteAllRequest): Common.CommonEmptyResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(request.target)
            group.settings.isMuteAll = true
            commonResponseBuilder()
        }
    }

    override suspend fun unmuteAll(request: ServiceGroupManage.MuteUnmuteAllRequest): Common.CommonEmptyResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(request.target)
            group.settings.isMuteAll = false
            commonResponseBuilder()
        }
    }

    @MiraiExperimentalApi
    override suspend fun getGroupConfig(request: ServiceGroupManage.GetGroupConfigRequest): ServiceGroupManage.GetGroupConfigResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(request.target)
            ServiceGroupManage.GetGroupConfigResponse.newBuilder()
                .apply {
                    dataBuilder.apply {
                        nameBuilder.value = group.name
                        announcementBuilder.value = group.settings.entranceAnnouncement
                        allowMemberInviteBuilder.value = group.settings.isAllowMemberInvite
                        autoApproveBuilder.value = group.settings.isAutoApproveEnabled
                        anonymousChatBuilder.value = group.settings.isAnonymousChatEnabled
                    }
                }
        }
    }

    override suspend fun setGroupConfig(request: ServiceGroupManage.SetGroupConfigRequest): Common.CommonEmptyResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(request.target)
            with(request.data) {
                if (hasName()) group.name = name.value
                if (hasAnnouncement()) group.settings.entranceAnnouncement = announcement.value
                if (hasAllowMemberInvite()) group.settings.isAllowMemberInvite = allowMemberInvite.value
            }
            commonResponseBuilder()
        }
    }

    override suspend fun getMemberInfo(request: ServiceGroupManage.GetMemberInfoRequest): ServiceGroupManage.GetMemberInfoResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.memberId)
                .ensureMember(request.memberId)
            ServiceGroupManage.GetMemberInfoResponse.newBuilder()
                .apply {
                    dataBuilder.apply {
                        nickBuilder.value = member.nick
                        nameCardBuilder.value = member.nameCard
                        specialTitleBuilder.value = member.specialTitle
                    }
                }
        }
    }

    override suspend fun setMemberInfo(request: ServiceGroupManage.SetMemberInfoRequest): Common.CommonEmptyResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.memberId)
                .ensureMember(request.memberId)
            with(request.data) {
                if (hasNameCard()) member.nameCard = nameCard.value
                if (hasSpecialTitle()) member.specialTitle = specialTitle.value
            }
            commonResponseBuilder()
        }
    }
}
