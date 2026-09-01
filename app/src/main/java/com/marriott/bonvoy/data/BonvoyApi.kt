package com.marriott.bonvoy.data

import com.marriott.bonvoy.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendException(val statusCode: Int, val rawBody: String, url: String) :
    IOException("HTTP $statusCode from $url\n$rawBody")

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
            .put("devinOrgId", BuildConfig.DEVIN_ORG_ID)
            .toString()
            .toRequestBody(json)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Accept", "application/json")
            .header("X-Bonvoy-Client", "android/${BuildConfig.VERSION_NAME}")
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw BackendException(response.code, text, url)
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
