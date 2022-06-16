package com.snapmint.checkoutsdk.bridge

interface CheckoutResponse {
    fun handlePaymentResponse(code: String?, message: String?)
}