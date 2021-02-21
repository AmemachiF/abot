package com.amemachif.abot.server.services

import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.ServiceSessionConfig
import com.amemachif.abot.proto.SessionConfigGrpcKt
import com.amemachif.abot.server.botCatching
import com.amemachif.abot.server.ensureBind

class SessionConfigService : SessionConfigGrpcKt.SessionConfigCoroutineImplBase() {
    override suspend fun getConfig(request: ServiceSessionConfig.GetConfigRequest): ServiceSessionConfig.GetConfigResponse {
        return botCatching {
            val bind = request.sessionKey.ensureBind()
            ServiceSessionConfig.GetConfigResponse.newBuilder()
                .apply {
                    configBuilder.apply {
                        cacheSizeBuilder.value = bind.cacheSize

                    }
                }
        }
    }

    override suspend fun setConfig(request: ServiceSessionConfig.SetConfigRequest): Common.CommonEmptyResponse {
        return botCatching {
            request.sessionKey.ensureBind().let {
                if (request.hasConfig()) {
                    with(request.config) {
                        when {
                            hasCacheSize() -> it.cacheSize = this.cacheSize.value
                        }
                    }
                }
            }
            Common.CommonEmptyResponse.newBuilder()
        }
    }
}
