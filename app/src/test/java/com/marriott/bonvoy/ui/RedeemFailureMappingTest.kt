package com.marriott.bonvoy.ui

import com.marriott.bonvoy.data.BackendException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class RedeemFailureMappingTest {
    private val url = "http://10.0.2.2:3000/api/bonvoy/points/redeem"

    @Test
    fun ledgerUnavailableIncludesStructuredSupportDetailsWithoutStackTrace() {
        val failure = BackendException(
            500,
            "PointsLedgerUnavailable",
            "Loyalty points ledger is unavailable: shard bonvoy-ledger-amer for region AMER is not registered",
            "POINTS_LEDGER_UNAVAILABLE",
            "req-123",
            "raw",
            url,
        ).toFailureState()

        assertEquals("Points redemption is temporarily unavailable", failure.title)
        assertTrue(failure.supportDetails.contains("Request ID" to "req-123"))
        assertTrue(failure.supportDetails.contains("Error" to "PointsLedgerUnavailable"))
        assertFalse(failure.supportDetails.any { (_, value) -> value.contains("at com.") })
        assertFalse(failure.supportDetails.any { (_, value) -> value.contains("Exception:") })
    }

    @Test
    fun malformedBackendResponsePreservesRawBodyAndUsesUnavailableMapping() {
        val exception = BackendException.fromResponse(500, "<html>bad gateway</html>", url)
        val failure = exception.toFailureState()

        assertEquals(null, exception.error)
        assertEquals(null, exception.backendMessage)
        assertEquals(null, exception.code)
        assertEquals(null, exception.requestId)
        assertEquals("<html>bad gateway</html>", exception.rawBody)
        assertEquals("Points redemption is temporarily unavailable", failure.title)
        assertFalse(failure.supportDetails.isEmpty())
    }

    @Test
    fun clientErrorUsesBackendMessage() {
        val failure = BackendException(
            400,
            "NotEnoughPoints",
            "Not enough points available for this redemption.",
            "NOT_ENOUGH_POINTS",
            null,
            "raw",
            url,
        ).toFailureState()

        assertEquals(
            "Not enough points available for this redemption.",
            failure.body,
        )
    }

    @Test
    fun unknownHostUsesConnectionMessage() {
        val failure = UnknownHostException("x").toFailureState()

        assertEquals("We can't reach Marriott Bonvoy right now", failure.title)
        assertNotNull(failure.body)
    }
}
