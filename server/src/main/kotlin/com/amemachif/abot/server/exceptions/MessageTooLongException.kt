package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class MessageTooLongException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.MESSAGE_TOO_LONG, moreMessage, cause)
