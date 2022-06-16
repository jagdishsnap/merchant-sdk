package com.snapmint.checkoutsdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.snapmint.checkoutsdk.bridge.CheckoutResponse
import com.snapmint.checkoutsdk.bridge.WebAppInterface
import com.snapmint.checkoutsdk.bridge.WebViewClientImpl
import com.snapmint.checkoutsdk.utils.ApiConstants
import java.io.File


open class CheckoutActivity : AppCompatActivity(), CheckoutResponse {
    private lateinit var webView: WebView
    private  var mUploadMessage: ValueCallback<Uri>? = null
    private  var uploadMessage: ValueCallback<Array<Uri>>? = null

    // Storage Permissions variables
    private val FILECHOOSER_RESULTCODE: Int = 1
    val REQUEST_SELECT_FILE = 100

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    private var imageUri: Uri? = null
    private var sucUrl: String = ""
    private var failUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        verifyStoragePermissions()
        initViews()
        initActions()
    }

    private fun initActions() {

         object : WebChromeClient() {
            @SuppressLint("SetJavaScriptEnabled")
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                    val newWebView = WebView(this@CheckoutActivity)

                    newWebView.settings.javaScriptEnabled = true
                    newWebView.settings.setSupportZoom(true)
                    newWebView.settings.builtInZoomControls = true
                    newWebView.settings.pluginState = WebSettings.PluginState.ON
                    newWebView.settings.setSupportMultipleWindows(true)
                    newWebView.settings.allowContentAccess = true
                    newWebView.settings.loadWithOverviewMode = true
                    view.addView(newWebView)

                    val transport = resultMsg.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()

                    newWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view1: WebView, url: String): Boolean {
                            view1.loadUrl(url)
                            when {
                                url.contains("token_recurring_status") -> {
                                    view.removeView(newWebView)
                                    Log.d("view","removed")
                                }
                                url.contains("success") -> {
                                    val returnIntent =  Intent()
                                    returnIntent.putExtra(ApiConstants.STATUS_CODE, 200)
                                    returnIntent.putExtra(ApiConstants.STATUS_MSG, "success")
                                    setResult(Activity.RESULT_OK, returnIntent)
                                    finish()
                                }
                                url.contains("failed") -> {
                                    val returnIntent =  Intent()
                                    returnIntent.putExtra(ApiConstants.STATUS_CODE, 300)
                                    returnIntent.putExtra(ApiConstants.STATUS_MSG, "failed")
                                    setResult(Activity.RESULT_OK, returnIntent)
                                    finish()
                                }
                                else -> {
                                    Log.d("view","not removed")
                                }
                            }
                            return true
                        }
                    }


                return true
            }


            // For Android 3.0+
            fun openFileChooser(uploadMsg: ValueCallback<Uri>?) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILECHOOSER_RESULTCODE
                )
            }

            // For Android 3.0+
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String?) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE
                )
            }

            //For Android 4.1
            fun openFileChooser(
                uploadMsg: ValueCallback<Uri>,
                acceptType: String?,
                capture: String?
            ) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILECHOOSER_RESULTCODE
                )
            }

            // For Lollipop 5.0+ Devices
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                mWebView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val imageStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyApp"
                )
                // Create the storage directory if it does not exist
                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs()
                }
                val file = File(
                    imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis()
                        .toString() + ".jpg"
                )
                imageUri = Uri.fromFile(file)
                val cameraIntents: MutableList<Intent> = ArrayList()
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val packageManager = packageManager
                val listCam = packageManager.queryIntentActivities(captureIntent, 0)
                for (res in listCam) {
                    val packageName = res.activityInfo.packageName
                    val i = Intent(captureIntent)
                    i.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                    i.setPackage(packageName)
                    i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    cameraIntents.add(i)
                }
                Log.v(this.javaClass.name, "REQUEST_SELECT_FILE in 5.0+$imageUri")
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, i)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    cameraIntents.toTypedArray()
                )
                try {
                    startActivityForResult(
                        chooserIntent,
                        REQUEST_SELECT_FILE
                    )
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(this@CheckoutActivity, "Cannot Open File Chooser", Toast.LENGTH_LONG)
                        .show()
                    return false
                }
                return true
            }
        }.also { webView.webChromeClient = it }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        val finalData = intent.getStringExtra(ApiConstants.FINAL_DATA)
        val option = intent.getStringExtra(ApiConstants.OPTION)
        val baseUrl = intent.getStringExtra(ApiConstants.BASE_URL)
        sucUrl = intent.getStringExtra(ApiConstants.SUC_URL).toString()
        failUrl = intent.getStringExtra(ApiConstants.FAIL_URL).toString()

        webView = WebView(this@CheckoutActivity)
        val webViewClient = WebViewClientImpl(this)
        webView.webViewClient = webViewClient
        webView.setInitialScale(1)

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportMultipleWindows(true)
        webSettings.pluginState = WebSettings.PluginState.ON

        webSettings.allowFileAccess = true
        webView.addJavascriptInterface(WebAppInterface(this@CheckoutActivity), "Android")

        if (!option.isNullOrEmpty()) {
            finalData?.toByteArray()?.let { webView.postUrl(baseUrl.toString(), it) }
        }
        setContentView(webView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view1: WebView, url: String): Boolean {
                view1.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Handler().postDelayed({
                    when (url) {
                        sucUrl -> {
                            val returnIntent =  Intent()
                            returnIntent.putExtra(ApiConstants.STATUS_CODE, 200)
                            returnIntent.putExtra(ApiConstants.STATUS_MSG, "success")
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                        failUrl -> {
                            val returnIntent =  Intent()
                            returnIntent.putExtra(ApiConstants.STATUS_CODE, 300)
                            returnIntent.putExtra(ApiConstants.STATUS_MSG, "failed")
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                        else -> {
                            Log.d("view","not removed")
                        }
                    }
                }, 2000)
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun handlePaymentResponse(code: String?, message: String?) {
        val returnIntent =  Intent()
        returnIntent.putExtra(ApiConstants.STATUS_CODE, code)
        returnIntent.putExtra(ApiConstants.STATUS_MSG, message)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun verifyStoragePermissions() {
        // Check if we have read or write permission
        val writePermission: Int =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission: Int =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val cameraPermission: Int =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        var intent = intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                Log.v(this.javaClass.name, "Image Uri : " + this.imageUri)
                val i = Intent()
                val result: Uri?
                if (resultCode != RESULT_OK) {
                    result = null
                } else {
                    if (intent == null) {
                        try {
                            i.data = this.imageUri
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    intent = intent ?: i // retrieve from the private variable if the intent is null
                }
                uploadMessage?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
                Log.v(this.javaClass.name, "Upload Image REQUEST_SELECT_FILE")
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result =
                if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
            Log.v(this.javaClass.name, "Upload Image FILECHOOSER_RESULTCODE")
        } else Toast.makeText(this@CheckoutActivity, "Failed to Upload Image", Toast.LENGTH_LONG).show()
    }
}
