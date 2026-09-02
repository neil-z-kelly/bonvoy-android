package com.marriott.bonvoy.data

import com.marriott.bonvoy.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A non-2xx response from the Bonvoy backend, with the structured error envelope
 * (`error`, `message`, `code`, `requestId`) the API returns on failure.
 */
class BackendException(
    val statusCode: Int,
    val error: String,
    val detail: String,
    val code: String,
    val requestId: String?,
    url: String,
) : IOException("HTTP $statusCode from $url: $error — $detail") {

    val isServerError: Boolean get() = statusCode >= 500

    companion object {
        fun from(statusCode: Int, rawBody: String, url: String): BackendException {
            val obj = runCatching { JSONObject(rawBody) }.getOrNull()
            return BackendException(
                statusCode = statusCode,
                error = obj?.optString("error").orEmpty().ifBlank { "HTTP $statusCode" },
                detail = obj?.optString("message").orEmpty().ifBlank { rawBody.trim().take(200) },
                code = obj?.optString("code").orEmpty(),
                requestId = obj?.optString("requestId")?.takeIf { it.isNotBlank() },
                url = url,
            )
        }
    }
}

data class RedemptionResult(
    val confirmation: String,
    val pointsDebited: Int,
    val newBalance: Int,
)

class BonvoyApi(private val baseUrl: String = BuildConfig.BACKEND_BASE_URL) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = "application/json; charset=utf-8".toMediaType()

    fun redeemPoints(memberNumber: String, hotel: Hotel, nights: Int): RedemptionResult {
        val url = "${baseUrl.trimEnd('/')}/api/bonvoy/points/redeem"
        val body = JSONObject()
            .put("member_number", memberNumber.replace(" ", ""))
            .put("hotel", hotel.name)
            .put("nights", nights)
            .put("points", hotel.pointsPerNight * nights)
            .apply { if (BuildConfig.DEVIN_ORG_ID.isNotBlank()) put("devinOrgId", BuildConfig.DEVIN_ORG_ID) }
            .toString()
            .toRequestBody(json)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Accept", "application/json")
            .header("X-Bonvoy-Client", "android/${BuildConfig.VERSION_NAME}")
            .apply {
                if (BuildConfig.DEMO_TOKEN.isNotBlank()) {
                    header("X-Bonvoy-Demo-Token", BuildConfig.DEMO_TOKEN)
                }
            }
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw BackendException.from(response.code, text, url)
            }
            val obj = JSONObject(text)
            return RedemptionResult(
                confirmation = obj.getString("confirmationNumber"),
                pointsDebited = obj.getInt("pointsDebited"),
                newBalance = obj.getInt("newBalance"),
            )
        }
    }
}
