package io.github.ulysseszh.displaytoggle.service

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import io.github.ulysseszh.displaytoggle.IShizukuUserService
import io.github.ulysseszh.displaytoggle.util.Display
import kotlin.system.exitProcess

class ShizukuUserService : IShizukuUserService.Stub {
	companion object {
		private val TAG = ShizukuUserService::class.simpleName!!
	}

	constructor() {
		Log.i(TAG, "UserService created")
	}

	// Only available from Shizuku v13
	@Keep // prevent proguard from removing it
	constructor(context: Context) {
		Log.i("UserService", "constructor with Context: context=$context")
	}

	override fun destroy() {
		Log.i(TAG, "UserService destroyed")
		exitProcess(0)
	}

	override fun setDisplayPowerMode(mode: Int) {
		Display.setPowerMode(mode)
	}
}
