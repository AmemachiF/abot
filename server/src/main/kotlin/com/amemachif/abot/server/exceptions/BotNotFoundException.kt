package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class BotNotFoundException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.BOT_NOT_FOUND, moreMessage, cause)
