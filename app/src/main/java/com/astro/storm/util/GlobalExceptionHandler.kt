package com.astro.storm.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import com.astro.storm.ui.screen.DebugActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val application: Application,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    private var lastActivity: Activity? = null
    private var activityCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    if (isDebugActivity(activity)) return
                    lastActivity = activity
                }

                override fun onActivityStarted(activity: Activity) {
                    if (isDebugActivity(activity)) return
                    activityCount++
                    lastActivity = activity
                }

                override fun onActivityResumed(activity: Activity) {
                    if (isDebugActivity(activity)) return
                    lastActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {
                    if (isDebugActivity(activity)) return
                    if (activityCount > 0) {
                        activityCount--
                    }
                    if (activityCount == 0) {
                        lastActivity = null
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {}
            })
    }

    private fun isDebugActivity(activity: Activity?): Boolean {
        return activity is DebugActivity
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        lastActivity?.let {
            val stringWriter = StringWriter()
            exception.printStackTrace(PrintWriter(stringWriter))
            val stackTrace = stringWriter.toString()

            val intent = Intent(it, DebugActivity::class.java).apply {
                putExtra(DebugActivity.EXTRA_STACK_TRACE, stackTrace)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            it.startActivity(intent)
            Process.killProcess(Process.myPid())
            exitProcess(-1)
        } ?: defaultHandler?.uncaughtException(thread, exception)
    }

    companion object {
        fun initialize(application: Application) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is GlobalExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    GlobalExceptionHandler(
                        application,
                        defaultHandler
                    )
                )
            }
        }
    }
}
