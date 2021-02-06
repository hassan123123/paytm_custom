package com.paytm.allinonesdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONObject

/** AllInOneSdkPlugin */
public class AllInOneSdkPlugin : FlutterPlugin, ActivityResultListener, MethodCallHandler, ActivityAware {
    private val REQ_CODE = 100
    private lateinit var channel: MethodChannel
    private lateinit var result: Result
    private var activity: Activity? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "allinonesdk")
        channel.setMethodCallHandler(this)
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "allinonesdk")
            val plugin = AllInOneSdkPlugin()
            channel.setMethodCallHandler(plugin)
            registrar.addActivityResultListener(plugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "startTransaction") {
            startTransaction(call)
            this.result = result
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun startTransaction(call: MethodCall) {
        val arg = call.arguments as Map<*, *>?
        if (arg != null) {
            val mid = arg["mid"] as String?
            val orderId = arg["orderId"] as String?
            val amount = arg["amount"] as String?
            val txnToken = arg["txnToken"] as String?
            val callbackUrl = arg["callbackUrl"] as String?
            val isStaging = arg["isStaging"] as Boolean
            val restrictAppInvoke = arg["restrictAppInvoke"] as Boolean
            if (mid == null || orderId == null || amount == null || mid.isEmpty() || orderId.isEmpty() || amount.isEmpty()) {
                showToast("Please enter all field")
                return
            }
            if (txnToken == null || txnToken.isEmpty()) {
                showToast("Token error")
                return
            }
            initiateTransaction(mid, orderId, amount, txnToken, callbackUrl, isStaging, restrictAppInvoke)
        } else {
            showToast("Please send arguments")
        }
    }

    private fun initiateTransaction(mid: String, orderId: String, amount: String, txnToken: String, callbackUrl: String?, isStaging: Boolean, restrictAppInvoke: Boolean) {
        var host = "https://securegw.paytm.in/"
        if (isStaging) {
            host = "https://securegw-stage.paytm.in/"
        }
        val callback = if (callbackUrl == null || callbackUrl.trim().isEmpty()) {
            host + "theia/paytmCallback?ORDER_ID=" + orderId
        } else {
            callbackUrl
        }
        val paytmOrder = PaytmOrder(orderId, mid, txnToken, amount, callback)
        val transactionManager = TransactionManager(paytmOrder, object : PaytmPaymentTransactionCallback {
            override fun onTransactionResponse(bundle: Bundle?) {
                val result = HashMap<String, String?>()
                if(bundle != null) {
                    for (key: String in bundle.keySet()) {
                        result[key] = bundle.getString(key)
                    }
                    if (result["STATUS"] == "TXN_SUCCESS") {
                        setResult(result)
                    } else {
                        setResult(result["RESPMSG"], result)
                    }
                }else{
                    setResult("Error")
                }
            }

            override fun networkNotAvailable() {
                setResult("Network Not Available")
            }

            override fun onErrorProceed(s: String) {
                setResult(s)
            }

            override fun clientAuthenticationFailed(s: String) {
                setResult(s)
            }

            override fun someUIErrorOccurred(s: String) {
                setResult(s)
            }

            override fun onErrorLoadingWebPage(iniErrorCode: Int, inErrorMessage: String, inFailingUrl: String) {
                setResult(inErrorMessage)
            }

            override fun onBackPressedCancelTransaction() {
                setResult("Back Pressed")
            }

            override fun onTransactionCancel(s: String, bundle: Bundle) {
                val result = HashMap<String, String?>()
                for (key: String in bundle.keySet()) {
                    result[key] = bundle.getString(key)
                }
                setResult("Transaction Cancel", result)
            }
        })
        transactionManager.setCallingBridge("Flutter")
        if(restrictAppInvoke){
            transactionManager.setAppInvokeEnabled(false)
        }
        transactionManager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage")
        transactionManager.startTransaction(activity!!, REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
        if (requestCode == REQ_CODE && data != null) {
            val message = data.getStringExtra("nativeSdkForMerchantMessage")
            val response = data.getStringExtra("response")
            if (response != null && response.isNotEmpty()) {
                try{
                    val jsonOb = JSONObject(response)
                    val result = HashMap<String, String?>()
                    for (key: String in jsonOb.keys()) {
                        result[key] = jsonOb.getString(key)
                    }
                    if (result["STATUS"] == "TXN_SUCCESS") {
                        setResult(result)
                    } else {
                        setResult(result["RESPMSG"], result)
                    }
                }catch(e:Exception){
                    setResult(e.message)
                }
            } else {
                setResult(message)
            }
        }
        return true
    }

    private fun setResult(message: String?, value: HashMap<String, String?>? = null) {
        result.error("0", message ?: "Unknown error", value)
    }

    private fun setResult(value: HashMap<String, String?>) {
        result.success(value)
    }

    private fun showToast(message: String) {
        Toast.makeText(activity!!, message, Toast.LENGTH_LONG).show()
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
