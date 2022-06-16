package com.snapmint.checkoutsdk.model

data class CheckoutModel(
    var base_url:String,
    var co_source:String,
    var authenticity_token:String,
    var user_type:String,
    var mobile:String,
    var merchant_id:String,
    var store_id:String,
    var order_id:String,
    var order_value:String,
    var merchant_confirmation_url:String,
    var merchant_failure_url:String,
    var full_name:String,
    var email:String,
    var billing_full_name:String,
    var billing_address_line1:String,
    var billing_city:String,
    var billing_zip:String,
    var shipping_full_name:String,
    var shipping_address_line1:String,
    var shipping_city:String,
    var shipping_zip:String,
    var products_sku:String,
    var products_unit_price:String,
    var products_quantity:String,
    var merchant_key:String,
    var merchant_token:String,
    var checksum_hash:String,

)