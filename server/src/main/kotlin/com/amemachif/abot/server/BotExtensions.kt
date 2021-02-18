package com.amemachif.abot.server

import com.amemachif.abot.proto.Error
import com.amemachif.abot.server.exceptions.BotException
import com.amemachif.abot.server.exceptions.ErrorException
import com.google.protobuf.GeneratedMessageV3
import kotlin.reflect.full.functions
import kotlin.reflect.full.staticFunctions

object BotExtensions {
    inline fun <reified R : GeneratedMessageV3, T : GeneratedMessageV3.Builder<*>> botCatching(block: () -> T): R {
        return try {
            try {
                val result = block()
                    .apply {
                        setCode(this, ErrorCode.OK.code, ErrorCode.OK.message)
                        val getCodeBuilder = this::class.functions.find { it.name == "getCodeBuilder" }
                        val codeBuilder = getCodeBuilder?.call(this)
                        if (codeBuilder is Error.ErrorMessage.Builder) {
                            codeBuilder.apply {
                                code = Error.Code.OK
                                num = Error.Code.OK_VALUE
                                message = ErrorCode.OK.message
                            }
                        }
                    }.build()
                if (result is R) {
                    result
                } else {
                    throw NoSuchMethodException()
                }
            } catch (e: BotException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                throw ErrorException(e.message, e)
            }
        } catch (e: BotException) {
            e.printStackTrace()
            val newBuilder = R::class.staticFunctions.find { it.name == "newBuilder" && it.parameters.count() == 0 }
                ?: throw NoSuchMethodException()
            val builder = newBuilder.call() ?: throw NoSuchMethodException()
            val build = builder::class.functions.find { it.name == "build" } ?: throw NoSuchMethodException()
            val getCodeBuilder = builder::class.functions.find { it.name == "getCodeBuilder" }
            val codeBuilder = getCodeBuilder?.call(builder)
            if (codeBuilder is Error.ErrorMessage.Builder) {
                codeBuilder.apply {
                    code = Error.Code.forNumber(e.error.code)
                    num = e.error.code
                    message = e.message
                }
            }
            val result = build.call(builder)
            return if (result is R) {
                result
            } else {
                throw NoSuchMethodException()
            }
        }
    }

    fun setCode(builder: Any, code: Int, message: String) {
        val getCodeBuilder = builder::class.functions.find { it.name == "getCodeBuilder" }
        val codeBuilder = getCodeBuilder?.call(builder)
        if (codeBuilder is Error.ErrorMessage.Builder) {
            codeBuilder.apply {
                this.code = Error.Code.forNumber(code)
                this.num = code
                this.message = message
            }
        }
    }
}
