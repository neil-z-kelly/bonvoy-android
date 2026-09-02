package com.marriott.bonvoy.ui

import com.marriott.bonvoy.data.BackendException
import java.io.IOException

/**
 * Member-facing presentation of a failed redemption. The friendly copy is what the
 * member reads; [supportDetails] are the technical fields a member can quote to
 * customer care and are rendered subdued.
 */
data class RedeemError(
    val title: String,
    val body: String,
    val supportDetails: List<Pair<String, String>>,
) {
    companion object {
        fun from(error: Throwable): RedeemError = when (error) {
            is BackendException -> RedeemError(
                title = if (error.isServerError) {
                    "Points redemption is temporarily unavailable"
                } else {
                    "We couldn't complete this redemption"
                },
                body = if (error.isServerError) {
                    "We're sorry — our loyalty points service is taking a short break. " +
                        "No points have been deducted from your account. Please try again in a few minutes, " +
                        "or explore other stays while we get things back on track."
                } else {
                    "Something about this redemption didn't go through. No points have been deducted. " +
                        "Please review the details and try again."
                },
                supportDetails = buildList {
                    add("Error" to error.error)
                    add("Message" to error.detail)
                    if (error.code.isNotBlank()) add("Code" to error.code)
                    add("Request ID" to (error.requestId ?: "unavailable"))
                    add("Status" to "HTTP ${error.statusCode}")
                },
            )
            is IOException -> RedeemError(
                title = "We couldn't reach Marriott Bonvoy",
                body = "Please check your connection and try again. No points have been deducted.",
                supportDetails = listOf("Error" to error::class.java.simpleName, "Message" to error.message.orEmpty()),
            )
            else -> RedeemError(
                title = "Something went wrong",
                body = "We couldn't complete your redemption. No points have been deducted. Please try again.",
                supportDetails = listOf("Error" to error::class.java.simpleName, "Message" to error.message.orEmpty()),
            )
        }
    }
}
