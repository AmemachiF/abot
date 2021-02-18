package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class SessionNotAuthException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.SESSION_NOT_AUTH, moreMessage, cause)
