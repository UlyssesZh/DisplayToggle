package io.github.ulysseszh.displaytoggle.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import io.github.ulysseszh.displaytoggle.Constants.ACTION_SET_DISPLAY_POWER_MODE
import io.github.ulysseszh.displaytoggle.ShizukuHelper
import io.github.ulysseszh.displaytoggle.util.Display.PowerMode

// One-off service to set display power mode
class SetDisplayPowerModeService : Service() {
	companion object {
		private val TAG = SetDisplayPowerModeService::class.simpleName!!
	}
	private var powerMode: PowerMode? = null
	private val shizukuHelper = ShizukuHelper(this)

	override fun onCreate() {
		super.onCreate()
		shizukuHelper.addServiceConnectedListener {
			if (powerMode != null) {
				shizukuHelper.setDisplayPowerMode(powerMode!!)
			}
			shizukuHelper.cleanUp()
			stopSelf()
		}
	}

	override fun onBind(intent: Intent): IBinder? {
		if (intent.action != ACTION_SET_DISPLAY_POWER_MODE) {
			Log.e(TAG, "Unsupported action: ${intent.action}")
			return null
		}
		val powerModeString = intent.getStringExtra("POWER_MODE")
		powerMode = PowerMode.fromString(powerModeString)
		if (powerMode == null) {
			Log.e(TAG, "Unsupported mode: $powerModeString")
			return null
		}
		shizukuHelper.setUp()
		return Binder()
	}
}
