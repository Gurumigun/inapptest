package com.gurumi.inapptest

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.vending.billing.IInAppBillingService
import com.droidcba.kedditbysteps.commons.recyclerView.adapter.ViewType
import com.google.gson.Gson
import com.gurumi.inapptest.payment.IabHelper
import com.gurumi.inapptest.payment.IabHelper.BILLING_RESPONSE_RESULT_OK
import com.gurumi.inapptest.recyclerView.adapter.AdapterConstants
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*

data class SponsorVo(val title: String,
                     val description: String,
                     val price: String,
                     val type: String,
                     val price_amount_micros: Long,
                     val price_currency_code: String,
                     val productId: String) : ViewType {
    override fun getViewType(): Int = AdapterConstants.ROW_SPONSOR
}

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        testString = when (v) {
            purchased -> "android.test.purchased"
            canceled -> "android.test.canceled"
            refunded -> "android.test.refunded"
            unavailable -> "android.test.item_unavailable"
            else -> null
        }

        productId.text = testString
    }

    val SKU_KRW_1000 = "krw_1000"
    val SKU_KRW_2000 = "krw_2000"
    val SKU_KRW_5000 = "krw_5000"
    val SKU_KRW_8000 = "krw_8000"
    val SKU_KRW_10000 = "krw_10000"

    private var testString: String? = null

    var mService: IInAppBillingService? = null

    private lateinit var sponsorAdapter: DevSponsorAdapter

    private var mIabHelper: IabHelper? = null
    val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0ibQoODU//" +
            "y+yHVMf7nwDoKWvJz73NdRmPIevmiBrjdxNSZNZzzyVrO/bi9XKh0bpRQIxHDvhbOk" +
            "/aMAHnLk4u+nn3+vsQ5f48UDAA2uh42lrtMSvkHkac76bV3H5tI4mEA8g0huDArac4vHa/" +
            "vXNcp4UDlBt8COgfYX8szFsQEu/5dBy4Wu4J62yZF5CJxo1cUO4CveCZgU7tkwzrdCXmnxla" +
            "NunXiiYWi5dHbt6KMroBw2Pp+XGnTyKBIts6Zjwq1RT1EnODaBOKL+nseCsUObC1kzKxn+kO6s" +
            "WBqTwLjNdEQFClu2ZqQPgLqUt0a5GFU9ZLaBAylct0Am2CXEFwIDAQAB" // 구글에서 발급받은 바이너리키를 입력

    // Provides purchase notification while this app is running
    var mServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mService = IInAppBillingService.Stub.asInterface(service)
        }
    }

    var mGotInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        // mHelper가 소거되었다면 종료
        if (mIabHelper == null) return@QueryInventoryFinishedListener

        // getPurchases()가 실패하였다launchPurchaseFlow면 종료
        if (result.isFailure) {
            Log.e("KIY", "Failed to query inventory: $result")
            return@QueryInventoryFinishedListener
        }

        launch {
            val skuList = ArrayList<String>()
            skuList.add(SKU_KRW_1000)
            skuList.add(SKU_KRW_2000)
            skuList.add(SKU_KRW_5000)
            skuList.add(SKU_KRW_8000)
            skuList.add(SKU_KRW_10000)
            val querySkus = Bundle()
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList)
            val skuDetails = mService?.getSkuDetails(3, packageName, "inapp", querySkus)
            val response = skuDetails?.getInt("RESPONSE_CODE") ?: -1
            if (response == BILLING_RESPONSE_RESULT_OK) {
                val responseList = skuDetails?.getStringArrayList("DETAILS_LIST")
                Log.d("KIY", "Response List : $responseList")
                val gson = Gson()
                val rows = ArrayList<SponsorVo>(responseList?.size ?: 0)

                responseList?.forEach {
                    rows.add(gson.fromJson(it, SponsorVo::class.java))
                }

                rows.sortBy { it.price_amount_micros }

                launch(UI) {
                    sponsorAdapter.setItem(rows)
                }
            }
        }

        Log.d("KIY", "Initial inventory query finished; enabling main UI.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sponsorAdapter = DevSponsorAdapter {
            buyItems(it)
        }

        with(recyclerView) {
            this.adapter = sponsorAdapter
            this.layoutManager = LinearLayoutManager(this@MainActivity)
        }

        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return true
            }
        })

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE)

        mIabHelper = IabHelper(applicationContext, base64EncodedPublicKey)
        mIabHelper?.enableDebugLogging(true)
        mIabHelper?.startSetup {
            if (it.isSuccess === false) {
            }

            mIabHelper?.queryInventoryAsync(mGotInventoryListener)
        }

        purchased.setOnClickListener(this)
        canceled.setOnClickListener(this)
        refunded.setOnClickListener(this)
        unavailable.setOnClickListener(this)
        reset.setOnClickListener(this)
    }

    private fun buyItems(item: SponsorVo) {
        try {
            mIabHelper?.flagEndAsync()
            // 리스너 방식
            mIabHelper?.launchPurchaseFlow(this, testString ?: item.productId, 1000) { result, info ->
                //            mIabHelper.launchPurchaseFlow(this, "android.test.purchased", 1000, { result, info ->
                if (result.response == IabHelper.BILLING_RESPONSE_RESULT_OK
                        && info != null) {
                    val originalJson = info.originalJson
                    val dataSignature = info.signature

                    Log.d("KIY", "$originalJson , $dataSignature")
                    // 위의 사항들 체크 후 아이템 추가
                    try {
                        val jo = JSONObject(originalJson)
                        val sku = jo.getString("productId")

                        deleteAlreadyPurchaseItems()
                    } catch (e: JSONException) {
                        this@MainActivity.showAlert("결제에 성공하였습니다")
                    }
                } else if (result.response == IabHelper.IABHELPER_UNKNOWN_ERROR) {
                    this@MainActivity.showAlert("결제에 실패하였습니다.")
                    return@launchPurchaseFlow
                } else {
                    if (info == null) {
                        return@launchPurchaseFlow
                    }
                }

            }
            val bundle = mService!!.getBuyIntent(3, packageName, testString ?: item.productId, item.type, packageName)
//            val bundle = mService!!.getBuyIntent(3, packageName, testString ?: item.productId , item.type, packageName)
            val pendingIntent = bundle?.getParcelable<PendingIntent>("BUY_INTENT")
            if (pendingIntent != null) {
                // onActivityResult방식
//                startIntentSenderForResult(pendingIntent.intentSender,
//                        1000,
//                        Intent(),
//                        Integer.valueOf(0),
//                        Integer.valueOf(0),
//                        Integer.valueOf(0))
            } else {
//                 결제가 안되는 상황
            }
        } catch (e: Exception) {
        }
    }

    private fun deleteAlreadyPurchaseItems() {
        try {
            val bundle = mService!!.getPurchases(3, packageName, "inapp", null)
            val response = bundle.getInt("RESPONSE_CODE")
            if (response == 0) {
                val list = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST")
                val tokens = arrayOfNulls<String>(list!!.size)
                for (index in list.indices) {
                    val purchaseData = list[index]
                    val jsonObject = JSONObject(purchaseData)
                    tokens[index] = jsonObject.getString("purchaseToken")

                    mService!!.consumePurchase(3, packageName, tokens[index])
                }
            }
        } catch (e: Exception) {
            Log.e("KIY", "DeleteAlreadyPurchaseItems Error : ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                if (mIabHelper!!.handleActivityResult(requestCode, resultCode, data) === false) {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }

        //- 기존 방식
//        if (requestCode == 1000) {
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
//            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
//
//            if (resultCode == RESULT_OK) {
//                try {
//                    JSONObject jo = new JSONObject(purchaseData);
//                    String sku = jo.getString("productId");
//                    alert("You have bought the " + sku + ". Excellent choice,
//                            adventurer!");
//                }
//                catch (JSONException e) {
//                    alert("Failed to parse purchase data.");
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        mIabHelper.apply {
            mIabHelper?.dispose()
            mIabHelper = null
        }

        mServiceConn.apply { unbindService(this) }
    }
}
