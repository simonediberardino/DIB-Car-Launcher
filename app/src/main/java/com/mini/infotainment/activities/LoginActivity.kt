package com.mini.infotainment.activities

/*

class LoginActivity : ActivityExtended() {
    private lateinit var enteredTextET: EditText
    private lateinit var loginBtn: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()
    }

    private fun initializeLayout(){
        setContentView(R.layout.activity_login)

        loginBtn = findViewById(R.id.login_login_button)
        enteredTextET = findViewById(R.id.login_password_et)

        loginBtn.setOnClickListener {
            val enteredText = enteredTextET.text.toString()
            if(enteredText != Car.currentCar.password){
                loginError()
            }else{
                doLogin()
            }
        }
    }

    private fun loginError(){
        enteredTextET.setText(String())
        Utility.showToast(this, getString(R.string.password_errata))
    }

    private fun doLogin(){
        ApplicationData.setLastLogin(System.currentTimeMillis())
        Utility.navigateTo(this, HomeActivity::class.java)
    }
}*/
