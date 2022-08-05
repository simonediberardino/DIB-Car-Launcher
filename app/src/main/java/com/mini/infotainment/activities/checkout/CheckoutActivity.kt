package com.mini.infotainment.activities.checkout

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.mini.infotainment.R
import com.mini.infotainment.UI.CustomToast
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.support.ActivityExtended
import com.mini.infotainment.utility.Utility
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class CheckoutActivity : ActivityExtended() {
    lateinit var dialog: Dialog
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var paymentIntentClientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_checkout)
        initializePaymentSheet()
        showDialog()
        
        super.pageLoaded()
    }

    private fun showDialog(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_upgrade_premium)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val confirmBtn = dialog.findViewById<View>(R.id.dialog_premium_okDialog)

        dialog.setOnDismissListener { onBackPressed() }
        confirmBtn.setOnClickListener {
            presentPaymentSheet()
        }

        Utility.ridimensionamento(this, dialog.findViewById(R.id.parent))

        dialog.show()
    }

    private fun initializePaymentSheet(){
        val endPoint = "http://snrservers.vpsgh.it:3000/payment-sheet"
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        endPoint.httpPost().responseJson { _, _, result ->
            when(result){
                is Result.Success -> {
                    val responseJson = result.get().obj()

                    paymentIntentClientSecret = responseJson.getString("paymentIntent")

                    customerConfig = PaymentSheet.CustomerConfiguration(
                        responseJson.getString("customer"),
                        responseJson.getString("ephemeralKey")
                    )

                    val publishableKey = responseJson.getString("publishableKey")
                    PaymentConfiguration.init(this, publishableKey)
                }
                is Result.Failure -> this.onError()
            }
        }
    }

    private fun presentPaymentSheet() {
        dialog.setOnDismissListener {  }
        dialog.dismiss()

        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = this.getString(R.string.app_name),
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {}
            is PaymentSheetResult.Failed -> {
                this.onError()
            }
            is PaymentSheetResult.Completed -> {
                this.onSuccess()
            }
        }
    }

    private fun onSuccess(){
        FirebaseClass.promoteToPremium(30)
        CustomToast(this.getString(R.string.premium_success), this).show()
    }

    private fun onError(){
        CustomToast(this.getString(R.string.premium_error), this).show()
    }

}