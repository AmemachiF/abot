package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class SessionInvalidException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.SESSION_INVALID, moreMessage, cause)
