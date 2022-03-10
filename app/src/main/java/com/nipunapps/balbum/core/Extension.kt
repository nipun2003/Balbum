package com.nipunapps.balbum.core

fun Long.toTimeFormat(): String {
    var sec = this
    if (sec > 60) {
        var min = sec / 60
        sec %= 60
        if (min > 60) {
            val hour = min / 60
            min %= 60
            return "${hour.addZero()}:${min.addZero()}:${sec.addZero()}"
        }
        return "${min.addZero()}:${sec.addZero()}"
    }
    return "00:${sec.addZero()}"
}

fun Long.addZero(): String {
    return if (this < 10) return "0$this" else "$this"
}