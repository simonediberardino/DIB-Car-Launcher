package com.mini.infotainment.errors

import android.os.Environment
import android.util.Log
import java.io.*
import java.lang.Thread.UncaughtExceptionHandler
import java.text.SimpleDateFormat
import java.util.*


class ExceptionHandler : UncaughtExceptionHandler {
    private val defaultUEH: UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    override fun uncaughtException(t: Thread, e: Throwable) {
        val stringBuffSync: Writer = StringWriter()
        val printWriter = PrintWriter(stringBuffSync)

        e.printStackTrace(printWriter)
        val stacktrace = stringBuffSync.toString()
        printWriter.close()

        writeToFile(stacktrace)

        defaultUEH.uncaughtException(t, e)
    }

    private fun writeToFile(currentStacktrace: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
            val fileName = dateFormat.format(Date()) + ".txt"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(path, fileName)

            val stream = FileOutputStream(file)

            stream.use { stream ->
                stream.write(currentStacktrace.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("ExceptionHandler", e.message.toString())
        }
    }
}
