package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class BotMutedException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.BOT_MUTED, moreMessage, cause)
