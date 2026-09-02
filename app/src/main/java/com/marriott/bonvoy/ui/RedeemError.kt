package com.marriott.bonvoy.ui

import com.marriott.bonvoy.data.BackendException
import java.io.IOException

data class SupportDetail(val label: String, val value: String)

data class RedeemErrorUi(
    val title: String,
    val body: String,
    val details: List<SupportDetail>,
)

fun Throwable.toRedeemErrorUi(): RedeemErrorUi = when (this) {
    is BackendException -> {
        val details = if (errorName == null && serverMessage == null) {
            listOf(SupportDetail("Status", "HTTP $statusCode"))
        } else {
            buildList {
                add(SupportDetail("Error", errorName ?: "HTTP $statusCode"))
                serverMessage?.let { add(SupportDetail("Message", it)) }
                requestId?.let { add(SupportDetail("Request ID", it)) }
            }
        }
        RedeemErrorUi(
            title = "Points redemption is temporarily unavailable",
            body = "We couldn't complete your redemption right now. Your points haven't been touched — please try again in a few minutes, or keep exploring hotels.",
            details = details,
        )
    }

    is IOException -> RedeemErrorUi(
        title = "We couldn't reach Marriott Bonvoy",
        body = "Check your connection and try again.",
        details = listOf(SupportDetail("Error", simpleClassName())),
    )

    else -> RedeemErrorUi(
        title = "Points redemption is temporarily unavailable",
        body = "Something went wrong on our side. Please try again.",
        details = buildList {
            add(SupportDetail("Error", simpleClassName()))
            message?.takeIf { it.isNotBlank() }?.let { add(SupportDetail("Message", it)) }
        },
    )
}

private fun Throwable.simpleClassName(): String = javaClass.simpleName
