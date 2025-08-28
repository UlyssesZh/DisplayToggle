package io.github.ulysseszh.displaytoggle.service

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import io.github.ulysseszh.displaytoggle.IUserService
import io.github.ulysseszh.displaytoggle.util.Display


class UserService : IUserService.Stub {
	companion object {
		private const val TAG = "UserService"
	}

	constructor() {
		Log.i(TAG, "UserService created")
	}

	// Only available from Shizuku v13
	@Keep // prevent proguard from removing it
	constructor(context: Context) {
		Log.i("UserService", "constructor with Context: context=" + context.toString())
	}

	override fun destroy() {
		Log.i(TAG, "UserService destroyed")
		System.exit(0)
	}

	override fun setDisplayOn() {
		Display.turnOn()
	}

	override fun setDisplayOff() {
		Display.turnOff()
	}

}