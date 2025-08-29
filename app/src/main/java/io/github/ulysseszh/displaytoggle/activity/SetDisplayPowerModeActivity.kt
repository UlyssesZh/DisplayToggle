package io.github.ulysseszh.displaytoggle.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import io.github.ulysseszh.displaytoggle.Constants.ACTION_SET_DISPLAY_POWER_MODE
import io.github.ulysseszh.displaytoggle.ShizukuHelper
import io.github.ulysseszh.displaytoggle.util.Display.PowerMode

// One-off activity to set display power mode, needed because shortcuts cannot start services
class SetDisplayPowerModeActivity : Activity() {
	companion object {
		private val TAG = SetDisplayPowerModeActivity::class.simpleName!!
	}

	private val shizukuHelper = ShizukuHelper(this)
	private var powerMode: PowerMode? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (intent.action != ACTION_SET_DISPLAY_POWER_MODE) {
			Log.e(TAG, "Unsupported action: ${intent.action}")
			finish()
			return
		}
		val powerModeString = intent.getStringExtra("POWER_MODE")
		powerMode = PowerMode.fromString(powerModeString)
		if (powerMode == null) {
			Log.e(TAG, "Unsupported mode: $powerModeString")
		} else {
			shizukuHelper.addServiceConnectedListener {
				shizukuHelper.setDisplayPowerMode(powerMode!!)
				shizukuHelper.cleanUp()
			}
		}
		shizukuHelper.setUp()
		finish()
	}
}
