package com.marriott.bonvoy.ui

import com.marriott.bonvoy.data.BackendException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RedeemErrorTest {

    private val url = "http://10.0.2.2:3000/api/bonvoy/points/redeem"

    private val ledgerOutageBody = """
        {"success":false,"error":"PointsLedgerUnavailable",
         "message":"Loyalty points ledger is unavailable: shard bonvoy-ledger-amer for region AMER is not registered",
         "code":"POINTS_LEDGER_UNAVAILABLE","requestId":"c931e676-3f5e-4ba5-a468-03ae387f6ea9"}
    """.trimIndent()

    @Test
    fun `backend 500 envelope is parsed into structured fields`() {
        val e = BackendException.from(500, ledgerOutageBody, url)

        assertEquals(500, e.statusCode)
        assertTrue(e.isServerError)
        assertEquals("PointsLedgerUnavailable", e.error)
        assertEquals("POINTS_LEDGER_UNAVAILABLE", e.code)
        assertEquals("c931e676-3f5e-4ba5-a468-03ae387f6ea9", e.requestId)
        assertTrue(e.detail.startsWith("Loyalty points ledger is unavailable"))
    }

    @Test
    fun `ledger outage renders friendly copy with subdued support details`() {
        val presented = RedeemError.from(BackendException.from(500, ledgerOutageBody, url))

        assertEquals("Points redemption is temporarily unavailable", presented.title)
        assertTrue(presented.body.contains("No points have been deducted"))
        assertFalse(presented.body.contains("PointsLedgerUnavailable"))
        assertFalse(presented.body.contains("Exception"))

        val details = presented.supportDetails.toMap()
        assertEquals("PointsLedgerUnavailable", details["Error"])
        assertEquals("c931e676-3f5e-4ba5-a468-03ae387f6ea9", details["Request ID"])
        assertEquals("POINTS_LEDGER_UNAVAILABLE", details["Code"])
        assertTrue(details.getValue("Message").startsWith("Loyalty points ledger is unavailable"))
    }

    @Test
    fun `non-json body and missing fields degrade safely`() {
        val e = BackendException.from(502, "<html>Bad Gateway</html>", url)

        assertEquals("HTTP 502", e.error)
        assertEquals("<html>Bad Gateway</html>", e.detail)
        assertEquals("", e.code)
        assertNull(e.requestId)

        val details = RedeemError.from(e).supportDetails.toMap()
        assertEquals("unavailable", details["Request ID"])
        assertFalse(details.containsKey("Code"))
    }

    @Test
    fun `client 4xx uses non-outage copy`() {
        val body = """{"error":"InsufficientPoints","message":"Not enough points available for this redemption.","code":"INSUFFICIENT_POINTS"}"""
        val presented = RedeemError.from(BackendException.from(400, body, url))

        assertEquals("We couldn't complete this redemption", presented.title)
        assertEquals("InsufficientPoints", presented.supportDetails.toMap()["Error"])
    }

    @Test
    fun `network failure is presented as connectivity problem`() {
        val presented = RedeemError.from(IOException("timeout"))

        assertEquals("We couldn't reach Marriott Bonvoy", presented.title)
        assertEquals("IOException", presented.supportDetails.toMap()["Error"])
    }
}
