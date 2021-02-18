package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode

class FileNotFoundException(moreMessage: String? = null, cause: Throwable? = null) :
    BotException(ErrorCode.FILE_NOT_FOUND, moreMessage, cause)
