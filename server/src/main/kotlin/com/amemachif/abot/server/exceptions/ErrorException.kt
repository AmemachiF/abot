package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class ErrorException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.ERROR, moreMessage, cause)
