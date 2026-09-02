package com.marriott.bonvoy.data

import com.marriott.bonvoy.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendException(
    val statusCode: Int,
    val error: String?,
    val backendMessage: String?,
    val code: String?,
    val requestId: String?,
    val rawBody: String,
    url: String,
) : IOException("HTTP $statusCode from $url") {
    companion object {
        fun fromResponse(statusCode: Int, body: String, url: String): BackendException {
            return try {
                val json = JSONObject(body)
                fun optionalValue(key: String): String? =
                    json.optString(key, "")
                        .takeUnless { it.isEmpty() || json.isNull(key) }

                BackendException(
                    statusCode = statusCode,
                    error = optionalValue("error"),
                    backendMessage = optionalValue("message"),
                    code = optionalValue("code"),
                    requestId = optionalValue("requestId"),
                    rawBody = body,
                    url = url,
                )
            } catch (_: JSONException) {
                BackendException(
                    statusCode = statusCode,
                    error = null,
                    backendMessage = null,
                    code = null,
                    requestId = null,
                    rawBody = body,
                    url = url,
                )
            }
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
                throw BackendException.fromResponse(response.code, text, url)
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
