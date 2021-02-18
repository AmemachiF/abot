package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class NoPermissionException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.NO_PERMISSION, moreMessage, cause)
