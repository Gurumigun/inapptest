package com.gurumi.inapptest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import com.gurumi.inapptest.payment.IabHelper
import kotlinx.coroutines.experimental.launch


class Case1Activity : AppCompatActivity() {
    var mService: IInAppBillingService? = null

    private lateinit var mIabHelper: IabHelper
    val base64EncodedPublicKey = "" // 구글에서 발급받은 바이너리키를 입력

    var mServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mService = IInAppBillingService.Stub.asInterface(service)
        }
    }

    var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        Log.d("KIY", "Query inventory finished.")
        // mHelper가 소거되었다면 종료
        if (mIabHelper == null) return@QueryInventoryFinishedListener

        // getPurchases()가 실패하였다launchPurchaseFlow면 종료
        if (result.isFailure) {
            Log.e("KIY", "Failed to query inventory: $result")
            return@QueryInventoryFinishedListener
        }

        Log.d("KIY", "Initial inventory query finished; enabling main UI.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case1)

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE)

        mIabHelper = IabHelper(applicationContext, base64EncodedPublicKey)
        mIabHelper.enableDebugLogging(true)
        mIabHelper.startSetup {
            if (it.isSuccess === false) {
            }

            mIabHelper.queryInventoryAsync(mGotInventoryListener)
        }

        launch {

        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            unbindService(mServiceConn)
        }
    }
}
