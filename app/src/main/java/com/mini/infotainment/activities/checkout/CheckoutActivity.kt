package com.mini.infotainment.activities.checkout

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.mini.infotainment.R
import com.mini.infotainment.data.FirebaseClass
import com.mini.infotainment.support.SActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class CheckoutActivity : SActivity() {
    private lateinit var confirmBtn: View
    private var plans: MutableList<Plan> = mutableListOf()
    private var confirmedPlan: Plan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeLayout()
        super.pageLoaded()
    }

    override fun initializeLayout(){
        setContentView(R.layout.activity_checkout)

        confirmBtn = findViewById(R.id.checkout_confirm)
        confirmBtn.setOnClickListener {
            confirmedPlan = plans.first { it.isSelected }.also {
                it.payment.presentPaymentSheet()
            }
        }

        plans.add(Plan(
                findViewById(R.id.checkout_plan_1),
                2.toString(),
               1
            )
        )

        plans.add(Plan(
                findViewById(R.id.checkout_plan_2),
                7.toString(),
                6
            )
        )

        plans.add(Plan(
                findViewById(R.id.checkout_plan_3),
                12.toString(),
                12
            )
        )

        plans[1].isSelected = true
    }

    inner class Plan(val view: ViewGroup, val price: String, val months: Int){
        var payment: StripePayment = StripePayment().also { it.initializePaymentSheet(price) }

        var isSelected = false
            set(value) {
                val bgResource = if(value) R.drawable.round_blue_gradient else R.drawable.square_light_grey_round
                view.setBackgroundResource(bgResource)
                field = value
            }

        init {
            view.setOnClickListener {
                for (plan in plans) plan.isSelected = plan.view == it
            }

            view.findViewById<TextView>(R.id.plan_months).text =
                getString(R.string.n_months).replace("{n}", months.toString())

            view.findViewById<TextView>(R.id.plan_price).text = price.toString()
            isSelected = false
        }
    }

    inner class StripePayment{
        private var paymentSheet: PaymentSheet? = null
        private var paymentIntentClientSecret: String? = null
        private var customerConfiguration: PaymentSheet.CustomerConfiguration? = null

        fun initializePaymentSheet(price: String){
            val endPoint = "http://snrservers.vpsgh.it:3000/payment-sheet/$price"
            paymentSheet = PaymentSheet(this@CheckoutActivity, ::onPaymentSheetResult)

            endPoint.httpPost().responseJson { _, _, result ->
                when(result){
                    is Result.Success -> {
                        val responseJson = result.get().obj()

                        paymentIntentClientSecret = responseJson.getString("paymentIntent")

                        customerConfiguration = PaymentSheet.CustomerConfiguration(
                            responseJson.getString("customer"),
                            responseJson.getString("ephemeralKey")
                        )

                        val publishableKey = responseJson.getString("publishableKey")
                        PaymentConfiguration.init(this@CheckoutActivity, publishableKey)
                    }
                    is Result.Failure -> onError()
                }
            }
        }

        fun presentPaymentSheet() {
            if(paymentIntentClientSecret == null) {
                Toast.makeText(
                    this@CheckoutActivity,
                    getString(R.string.payment_not_ready),
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            paymentSheet?.presentWithPaymentIntent(
                paymentIntentClientSecret!!,
                PaymentSheet.Configuration(
                    merchantDisplayName = getString(R.string.app_name),
                    customer = customerConfiguration,
                    allowsDelayedPaymentMethods = true
                )
            )
        }

        private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
            when (paymentSheetResult) {
                is PaymentSheetResult.Canceled -> {}
                is PaymentSheetResult.Failed -> {
                    onError()
                }
                is PaymentSheetResult.Completed -> {
                    onSuccess()
                    finish()
                }
            }
        }

        private fun onSuccess(){
            FirebaseClass.promoteToPremium(confirmedPlan!!.months.toLong()){
                Toast.makeText(
                    this@CheckoutActivity,
                    getString(R.string.premium_success).replace("{n_months}", confirmedPlan!!.months.toString()),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        private fun onError(){
            Toast.makeText(
                this@CheckoutActivity,
                getString(R.string.premium_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}