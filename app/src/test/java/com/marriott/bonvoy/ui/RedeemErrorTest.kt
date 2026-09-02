package com.marriott.bonvoy.ui

import com.marriott.bonvoy.data.BackendException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RedeemErrorTest {

    @Test
    fun backendErrorBodyMapsToGracefulSupportDetails() {
        val message = "Loyalty points ledger is unavailable: shard bonvoy-ledger-amer for region AMER is not registered"
        val error = BackendException(
            500,
            """{"success":false,"error":"PointsLedgerUnavailable","message":"$message","code":"POINTS_LEDGER_UNAVAILABLE","requestId":"a134180b-d227-434f-b34e-3e8887299740"}""",
            "http://localhost/redeem",
        )

        val ui = error.toRedeemErrorUi()

        assertEquals("Points redemption is temporarily unavailable", ui.title)
        assertEquals(
            listOf(
                SupportDetail("Error", "PointsLedgerUnavailable"),
                SupportDetail("Message", message),
                SupportDetail("Request ID", "a134180b-d227-434f-b34e-3e8887299740"),
            ),
            ui.details,
        )
        assertFalse(ui.body.contains("Exception"))
        assertFalse(ui.body.contains("at com."))
    }

    @Test
    fun backendErrorWithoutRequestIdOmitsRequestIdDetail() {
        val error = BackendException(
            500,
            """{"success":false,"error":"PointsLedgerUnavailable","message":"ledger unavailable"}""",
            "http://localhost/redeem",
        )

        val ui = error.toRedeemErrorUi()

        assertEquals("PointsLedgerUnavailable", ui.details.first().value)
        assertFalse(ui.details.any { it.label == "Request ID" })
    }

    @Test
    fun nonJsonBackendBodyFallsBackToHttpStatus() {
        val error = BackendException(502, "<html>bad gateway</html>", "http://localhost/redeem")

        val ui = error.toRedeemErrorUi()

        assertEquals(listOf(SupportDetail("Status", "HTTP 502")), ui.details)
    }

    @Test
    fun socketTimeoutMapsToConnectionMessage() {
        val ui = SocketTimeoutException().toRedeemErrorUi()

        assertEquals("We couldn't reach Marriott Bonvoy", ui.title)
    }

    @Test
    fun unexpectedErrorIncludesMessageDetail() {
        val ui = IllegalStateException("boom").toRedeemErrorUi()

        assertEquals("Points redemption is temporarily unavailable", ui.title)
        assertEquals(SupportDetail("Message", "boom"), ui.details.last())
    }
}
