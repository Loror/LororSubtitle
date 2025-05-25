package com.loror.lororSubtitle.example

import android.os.SystemClock
import kotlin.compareTo

class TimePlayer {

    private var time = SystemClock.uptimeMillis()

    fun current(): Int {
        return (SystemClock.uptimeMillis() - time).toInt()
    }

    fun end(): Boolean {
        return (SystemClock.uptimeMillis() - time) > 10 * 60 * 1000
    }
}
