package com.yeqiu.board

import android.content.Context
import android.util.Log
import android.widget.Toast


const val tag = "awesomeAndroid"
const val space = "       "

fun log(msg: String) {

    val taskName = getTaskName()
    Log.i(tag, "═════════$taskName═════════")
    Log.i(tag, "$space$msg")
    Log.i(tag, space)
}



fun Context.toast(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}


private fun getTaskName(): String {

    val index = 4
    val stackTrace = Thread.currentThread().stackTrace
    var name = "unknown"
    var lineNumber = 0
    if (stackTrace.size >= 5) {
        name = stackTrace[index].fileName
        lineNumber = stackTrace[index].lineNumber
    }
    return "($name:$lineNumber)"

}