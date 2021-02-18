package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class WrongAuthKeyException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.WRONG_AUTH_KEY, moreMessage, cause)
