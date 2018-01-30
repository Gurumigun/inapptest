package com.gurumi.inapptest

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        case1.setOnClickListener {
            startActivity(Intent(this@MainActivity, Case1Activity::class.java))
        }

        case2.setOnClickListener {
            startActivity(Intent(this@MainActivity, Case2Activity::class.java))
        }
    }
}
