package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class TargetNotFoundException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.TARGET_NOT_FOUND, moreMessage, cause)
