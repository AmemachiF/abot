package com.amemachif.abot.server.exceptions

import com.amemachif.abot.server.ErrorCode
import java.lang.RuntimeException

abstract class BotException(
    val error: ErrorCode,
    val moreMessage: String? = null,
    cause: Throwable? = null
) : RuntimeException("${error.message}${if (moreMessage != null) ": $moreMessage" else ""}", cause)
