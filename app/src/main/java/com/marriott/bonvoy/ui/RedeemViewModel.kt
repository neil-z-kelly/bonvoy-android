package com.marriott.bonvoy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marriott.bonvoy.data.BonvoyApi
import com.marriott.bonvoy.data.DemoData
import com.marriott.bonvoy.data.Hotel
import com.marriott.bonvoy.data.RedemptionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RedeemState {
    data object Idle : RedeemState
    data object Loading : RedeemState
    data class Success(val result: RedemptionResult) : RedeemState
    data class Failure(val error: Throwable) : RedeemState
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
                RedeemState.Failure(t)
            }
        }
    }

    fun reset() {
        _state.value = RedeemState.Idle
    }
}
