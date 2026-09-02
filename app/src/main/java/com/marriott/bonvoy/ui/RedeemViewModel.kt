package com.marriott.bonvoy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marriott.bonvoy.data.BonvoyApi
import com.marriott.bonvoy.data.BackendException
import com.marriott.bonvoy.data.DemoData
import com.marriott.bonvoy.data.Hotel
import com.marriott.bonvoy.data.RedemptionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

sealed interface RedeemState {
    data object Idle : RedeemState
    data object Loading : RedeemState
    data class Success(val result: RedemptionResult) : RedeemState
    data class Failure(
        val title: String,
        val body: String,
        val supportDetails: List<Pair<String, String>>,
    ) : RedeemState
}

internal fun Throwable.toFailureState(): RedeemState.Failure {
    val backendException = this as? BackendException
    val title: String
    val body: String
    val supportDetails: List<Pair<String, String>>

    if (backendException != null) {
        if (backendException.statusCode >= 500 ||
            backendException.code == "POINTS_LEDGER_UNAVAILABLE"
        ) {
            title = "Points redemption is temporarily unavailable"
            body = "We couldn't complete your redemption right now. Your points haven't been touched — please try again in a few minutes or continue browsing hotels."
        } else if (backendException.statusCode in 400..499) {
            title = "We couldn't complete this redemption"
            body = backendException.backendMessage ?: "Please check your details and try again."
        } else {
            title = "Something went wrong"
            body = "Please try again."
        }
        supportDetails = listOfNotNull(
            backendException.error?.let { "Error" to it },
            backendException.backendMessage?.let { "Message" to it },
            backendException.code?.let { "Code" to it },
            backendException.requestId?.let { "Request ID" to it },
            "Status" to "HTTP ${backendException.statusCode}",
        )
    } else if (this is IOException) {
        title = "We can't reach Marriott Bonvoy right now"
        body = "Check your connection and try again."
        supportDetails = listOfNotNull(
            "Error" to this::class.java.simpleName,
            message?.let { "Message" to it },
        )
    } else {
        title = "Something went wrong"
        body = "Please try again."
        supportDetails = listOfNotNull(
            "Error" to this::class.java.simpleName,
            message?.let { "Message" to it },
        )
    }

    return RedeemState.Failure(title, body, supportDetails)
}

class RedeemViewModel(private val api: BonvoyApi = BonvoyApi()) : ViewModel() {

    private val _state = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val state: StateFlow<RedeemState> = _state

    fun redeem(hotel: Hotel, nights: Int) {
        _state.value = RedeemState.Loading
        viewModelScope.launch {
            _state.value = try {
                val result = withContext(Dispatchers.IO) {
                    api.redeemPoints(DemoData.member.memberNumber, hotel, nights)
                }
                RedeemState.Success(result)
            } catch (t: Throwable) {
                t.toFailureState()
            }
        }
    }

    fun reset() {
        _state.value = RedeemState.Idle
    }
}
