package com.amemachif.abot.server

enum class ErrorCode(val code: Int, val message: String) {
    OK(0, "Ok"),
    WRONG_AUTH_KEY(1, "Wrong auth key"),
    BOT_NOT_FOUND(2, "Bot not found"),
    SESSION_INVALID(3, "Session invalid"),
    SESSION_NOT_AUTH(4, "Session not found"),
    TARGET_NOT_FOUND(5, "Target not found"),
    FILE_NOT_FOUND(6, "File not found"),
    NO_PERMISSION(10, "No permission"),
    BOT_MUTED(20, "Bot muted"),
    MESSAGE_TOO_LONG(30, "Message too long"),
    ERROR(400, "Error");
}
