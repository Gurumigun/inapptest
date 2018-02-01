package com.gurumi.inapptest

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast


/**
 *
 * Create : Kwon IkYoung
 * Date : 2018. 1. 2.
 */

var toast: Toast? = null

fun Context.showToast(msg: String) {
    if (toast == null) {
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    } else {
        toast?.setText(msg)
    }

    toast?.show()
}

fun View.startFlashAnimation() {
    val anim = AlphaAnimation(0.0f, 1.0f)
    anim.duration = 50 //You can manage the time of the blink with this parameter
    anim.startOffset = 20
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = 1
    startAnimation(anim)
}

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun <T> androidLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.toggleVisible() = if (this.isVisible()) this.visibility = View.GONE else this.visibility = View.VISIBLE

fun Context.showAlert(msg: String, leftBlock: (() -> String)? = null, rightBlock: (() -> String)? = null): AlertDialog {
    val alert = AlertDialog.Builder(this)
    alert.setMessage(msg)
    if (leftBlock != null) {
        alert.setNegativeButton("확인", { dialog, _ ->
            leftBlock()
        })
    }
    if (rightBlock == null) {
        alert.setPositiveButton("닫기", { dialog, which ->
        })
    } else {
        alert.setPositiveButton("닫기", { dialog, _ ->
            rightBlock()
        })
    }
    var dialog = alert.create()
    dialog.show()

    return dialog
}

fun String.convertInt(): Int = try {
    this.toInt()
} catch (e: NumberFormatException) {
    -1
}

fun String.convertLong(): Long = try {
    this.toLong()
} catch (e: NumberFormatException) {
    -1
}