package com.snapmint.checkoutsdk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.snapmint.checkoutsdk.databinding.ActivityMainBinding
import com.snapmint.checkoutsdk.model.CheckoutModel
import com.snapmint.checkoutsdk.utils.ApiConstants
import com.snapmint.checkoutsdk.utils.Helper1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isProd: Boolean = false
    private var baseUrl: String = ""
    private var finalData: String = ""
    private var sucUrl: String = ""
    private var failUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActions()
        //setupData()

    }

    private fun setupData() = binding.apply{
        baseUrl = if (isProd){
            "https://api.snapmint.com/v1/public/online_checkout"
        }else{
            "https://qaapi.snapmint.com/v1/public/online_checkout"
        }

        val checkoutModel = CheckoutModel(
            baseUrl,
            "sdk",
            "+Q4pfCpQ44AevJjakhcBovSNkO6A1Y6jbepCtamlTnhq9OhG+ZYvyyQPXENNVoNYVZIyYuHDXg5Ovd8kQFO3eQ==",
            "new_user",
            userMobile?.text.toString(),
            merchantId?.text.toString(),
            "1",
            "1",
            orderValue?.text.toString(),
            "http://www.vijaysales.com/success",
            "http://www.vijaysales.com/failed",
            "GIRIDHAR Crawley",
            userEmail?.text.toString(),
            "GIRIDHAR Crawley",
            "GIRIDHAR EVENT ORGANRING",
            "Mumbai",
            "400076",
            "GIRIDHAR Crawley",
            "GIRIDHAR EVENT ORGANRING",
            "Mumbai",
            "400076",
            "abdx123",
            "1000",
            "5",
            "pS7C8Pw8",
            "wb4egsnI",
            "",

            )

        sucUrl = checkoutModel.merchant_confirmation_url
        failUrl = checkoutModel.merchant_failure_url

        try {
            //checksum format merchant_key|order_id|order_value|full_name|email|token
            val checkSumStr: String =
                Helper1.generateCheckSum("${checkoutModel.merchant_key}|${checkoutModel.order_id}|${checkoutModel.order_value}|${checkoutModel.full_name}|${checkoutModel.email}|${checkoutModel.merchant_token}")
            checkoutModel.checksum_hash = checkSumStr
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ChecksumError", e.message.toString())
        }

        val data1 =
            "utf8=?&co_source=${checkoutModel.co_source}&authenticity_token=${checkoutModel.authenticity_token}&source=&user_type=${checkoutModel.user_type}&mobile="

        var data2 = ""
        data2 = "&merchant_id=${checkoutModel.merchant_id}"
        data2 += "&store_id=${checkoutModel.store_id}"
        data2 += "&order_id=${checkoutModel.order_id}"
        data2 += "&order_value=${checkoutModel.order_value}"
        data2 += "&merchant_confirmation_url=${checkoutModel.merchant_confirmation_url}"
        data2 += "&merchant_failure_url=${checkoutModel.merchant_failure_url}"
        data2 += "&full_name=${checkoutModel.full_name}"
        data2 += "&email=${checkoutModel.email}"
        data2 += "&billing_full_name=${checkoutModel.billing_full_name}"
        data2 += "&billing_address_line1=${checkoutModel.billing_address_line1}"
        data2 += "&billing_city=${checkoutModel.billing_city}"
        data2 += "&billing_zip=${checkoutModel.billing_zip}"
        data2 += "&shipping_full_name=${checkoutModel.shipping_full_name}"
        data2 += "&shipping_address_line1=${checkoutModel.shipping_address_line1}"
        data2 += "&shipping_city=${checkoutModel.shipping_city}"
        data2 += "&shipping_zip=${checkoutModel.shipping_zip}"
        data2 += "&products[][sku]=${checkoutModel.products_sku}"
        data2 += "&products[][unit_price]=${checkoutModel.products_unit_price}"
        data2 += "&products[][quantity]=${checkoutModel.products_quantity}"
        data2 += "&checksum_hash=${checkoutModel.checksum_hash}"

        finalData = "$data1${checkoutModel.mobile}$data2"
        Log.d("finalData",finalData)

    }

    private fun initActions() = binding.apply{
        binding.rgTypes.setOnCheckedChangeListener { group, checkedId ->
            isProd = when (checkedId) {
                R.id.rbProd -> {
                    true
                }
                R.id.rbTest -> {
                    false
                }
                else -> {
                    false
                }
            }
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null){
                    val code = data.getIntExtra(ApiConstants.STATUS_CODE,0)
                    val message = data.getStringExtra(ApiConstants.STATUS_MSG)
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "$code\n$message"
                    binding.tvStatus.setTextColor(Color.parseColor("#ffffff"))
                }
            }
        }

        binding.btnSubmit.setOnClickListener {

            if (userName.text.isBlank()) {
                Toast.makeText(this@MainActivity, "Please enter name", Toast.LENGTH_SHORT).show()
            } else if (userMobile.text.isBlank()) {
                Toast.makeText(this@MainActivity, "Please enter mobile number", Toast.LENGTH_SHORT).show()
            } else if (userEmail.text.isBlank()) {
                Toast.makeText(this@MainActivity, "Please enter email", Toast.LENGTH_SHORT).show()
            } else if (merchantId.text.isBlank()) {
                Toast.makeText(this@MainActivity, "Please enter merchant id", Toast.LENGTH_SHORT).show()
            } else if (orderValue.text.isBlank()) {
                Toast.makeText(this@MainActivity, "Please enter order value", Toast.LENGTH_SHORT).show()
            } else {
                setupData()
                val intent = Intent(this@MainActivity, CheckoutActivity::class.java)
                intent.putExtra(ApiConstants.FINAL_DATA, finalData)
                intent.putExtra(ApiConstants.BASE_URL, baseUrl)
                intent.putExtra(ApiConstants.OPTION, "check_out")
                intent.putExtra(ApiConstants.SUC_URL, sucUrl)
                intent.putExtra(ApiConstants.FAIL_URL, failUrl)
                resultLauncher.launch(intent)
            }
        }
    }
}