package io.github.ulysseszh.displaytoggle.util

import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.collections.forEach

object Display {
	private val TAG = Display::class.simpleName!!

	// https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/android/view/SurfaceControl.java
	enum class PowerMode(val value: Int) {
		OFF(0),
		DOZE(1),
		NORMAL(2),
		DOZE_SUSPEND(3),
		ON_SUSPEND(4);

		companion object {
			fun fromString(mode: String?): PowerMode? {
				return entries.firstOrNull { it.name.equals(mode, ignoreCase = true) }
			}
		}
	}

	fun getBuiltInDisplay(): IBinder {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			Reflection.getDeclaredMethod("android.view.SurfaceControl", "getInternalDisplayToken")
				.invoke(null) as IBinder
		} else {
			Reflection.getDeclaredMethod("android.view.SurfaceControl", "getBuiltInDisplay", Int::class.javaPrimitiveType!!)
				.invoke(null, 0) as IBinder
		}
	}

	@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
	fun getPhysicalDisplays(): List<IBinder> {
		val displayIds = Reflection.getDeclaredMethod("com.android.server.display.DisplayControl", "getPhysicalDisplayIds")
			.invoke(null) as LongArray?
		if (displayIds == null || displayIds.isEmpty()) {
			Log.w(TAG, "No physical displays found")
			return emptyList()
		}
		val getPhysicalDisplayTokenMethod =
			Reflection.getDeclaredMethod("com.android.server.display.DisplayControl", "getPhysicalDisplayToken", Long::class.javaPrimitiveType!!)
		return displayIds.map { getPhysicalDisplayTokenMethod.invoke(null, it) as IBinder }
	}

	fun setPowerMode(displayToken: IBinder, mode: Int) {
		Reflection.getDeclaredMethod("android.view.SurfaceControl", "setDisplayPowerMode", IBinder::class.java, Int::class.javaPrimitiveType!!)
			.invoke(null, displayToken, mode)
	}

	fun setPowerMode(displayToken: IBinder, mode: PowerMode) {
		setPowerMode(displayToken, mode.value)
	}

	fun setPowerMode(mode: Int) {
		Log.i(TAG, "Setting display mode to $mode")
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			getPhysicalDisplays().forEach { setPowerMode(it, mode) }
		} else {
			setPowerMode(getBuiltInDisplay(), mode)
		}
	}

	fun setPowerMode(mode: PowerMode) {
		setPowerMode(mode.value)
	}
}
