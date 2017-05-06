package com.unixtrong.designation.utils

import android.util.Log

/** Created by danyun on 2017/5/6 */

const val TAG = "DES"

fun debug(msg: String) {
    val ste = Throwable().stackTrace[1]
    val packageArray = ste.className.split(".")
    Log.d(TAG, "${packageArray[packageArray.size - 1]} ${ste.methodName}, $msg")
}