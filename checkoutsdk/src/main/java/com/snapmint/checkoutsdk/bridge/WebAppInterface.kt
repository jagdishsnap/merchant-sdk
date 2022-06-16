package com.snapmint.checkoutsdk.bridge

import android.content.Context
import android.webkit.JavascriptInterface

class WebAppInterface(context: Context) {
    private var checkoutResponse:CheckoutResponse = context as CheckoutResponse
    private var context: Context = context

    @JavascriptInterface
    fun handlePaymentResponse(code: String?, message: String?) {
        checkoutResponse.handlePaymentResponse(code,message)
    }

}