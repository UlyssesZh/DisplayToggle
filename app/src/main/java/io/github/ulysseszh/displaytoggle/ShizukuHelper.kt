package io.github.ulysseszh.displaytoggle

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import io.github.ulysseszh.displaytoggle.service.ShizukuUserService
import io.github.ulysseszh.displaytoggle.util.Display
import rikka.shizuku.Shizuku

class ShizukuHelper(val context: Context? = null) {
	companion object {
		private val TAG = ShizukuHelper::class.simpleName!!
		const val PERMISSION = 1108
	}

	var binderReceived = false
		private set
	private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
		binderReceived = true
		Log.i(TAG, "Shizuku binder received")
		if (checkPermission(PERMISSION)) {
			startUserService()
		}
	}
	private val binderDeadListener = Shizuku.OnBinderDeadListener {
		binderReceived = false
		userService = null
		Log.w(TAG, "Shizuku binder dead")
	}

	var userService: IShizukuUserService? = null
		private set
	private val userServiceConnection: ServiceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			if (service == null || !service.pingBinder()) {
				Log.e(TAG, "UserService binder is null or dead")
				return
			}
			Log.i(TAG, "UserService connected")
			userService = IShizukuUserService.Stub.asInterface(service)
			serviceConnectedListeners.forEach { it() }
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.w(TAG, "UserService disconnected")
		}
	}
	private val serviceConnectedListeners = mutableListOf<() -> Unit>()
	fun addServiceConnectedListener(listener: () -> Unit) {
		serviceConnectedListeners.add(listener)
	}
	fun removeServiceConnectedListener(listener: () -> Unit) {
		serviceConnectedListeners.remove(listener)
	}

	private val userServiceArgs = Shizuku.UserServiceArgs(
		ComponentName(BuildConfig.APPLICATION_ID, ShizukuUserService::class.qualifiedName!!)
	).daemon(false).processNameSuffix("service").debuggable(BuildConfig.DEBUG).version(BuildConfig.VERSION_CODE)

	private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
		if (grantResult != PackageManager.PERMISSION_GRANTED) {
			Log.w(TAG, "Shizuku permission not granted")
			if (context != null) {
				Toast.makeText(
					context,
					context.getString(R.string.shizuku_permission_denied),
					Toast.LENGTH_SHORT
				).show()
			}
			return@OnRequestPermissionResultListener
		}
		startUserService()
	}

	private fun startUserService() {
		if (Shizuku.getVersion() < 10) {
			Log.e(TAG, "Shizuku version ${Shizuku.getVersion()} < 10 is too low for UserService")
			toast(R.string.shizuku_version_too_low)
			return
		}
		Shizuku.bindUserService(userServiceArgs, userServiceConnection)
	}

	private fun stopUserService() {
		if (Shizuku.getVersion() < 10) {
			return
		}
		Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
	}

	fun checkPermission(code: Int): Boolean {
		if (Shizuku.isPreV11()) {
			return false
		}
		if (!binderReceived) {
			toast(R.string.shizuku_binder_not_received)
			return false
		}
		if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
			return true
		} else if (Shizuku.shouldShowRequestPermissionRationale()) {
			toast(R.string.shizuku_permission_denied)
			return false
		} else {
			toast(R.string.shizuku_request_permission)
			Shizuku.requestPermission(code)
			return false
		}
	}

	private fun toast(stringId: Int) {
		if (context == null) return
		Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show()
	}

	fun setUp() {
		Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
		Shizuku.addBinderDeadListener(binderDeadListener)
		Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
	}

	fun cleanUp() {
		Shizuku.removeBinderReceivedListener(binderReceivedListener)
		Shizuku.removeBinderDeadListener(binderDeadListener)
		Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
		stopUserService()
	}

	fun setDisplayPowerMode(mode: Int) {
		if (userService == null || !userService!!.asBinder().pingBinder()) {
			Log.e(TAG, "UserService is null or dead")
			toast(R.string.user_service_unavailable)
			return
		}
		userService!!.setDisplayPowerMode(mode)
	}

	fun setDisplayPowerMode(mode: Display.PowerMode) {
		setDisplayPowerMode(mode.value)
	}
}
