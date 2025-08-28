package io.github.ulysseszh.displaytoggle

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.google.android.material.color.DynamicColors
import io.github.ulysseszh.displaytoggle.service.UserService
import io.github.ulysseszh.displaytoggle.util.Utils
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "DisplayToggle"

		const val PERMISSION = 1108
	}

	private var shizukuBinderReceived = false
	private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
		shizukuBinderReceived = true
		Log.i(TAG, "Shizuku binder received")
		if (checkShizukuPermission(PERMISSION)) {
			startUserService()
		}
	}
	private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
		shizukuBinderReceived = false
		userService = null
		Log.w(TAG, "Shizuku binder dead")
	}

	var userService: IUserService? = null
	private val userServiceConnection: ServiceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			if (service == null || !service.pingBinder()) {
				Log.e(TAG, "UserService binder is null or dead")
				return
			}
			Log.i(TAG, "UserService connected")
			userService = IUserService.Stub.asInterface(service)
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.w(TAG, "UserService disconnected")
		}
	}

	private val userServiceArgs = Shizuku.UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, UserService::class.qualifiedName!!))
		.daemon(false)
		.processNameSuffix("service")
		.debuggable(BuildConfig.DEBUG)
		.version(BuildConfig.VERSION_CODE)

	private val shizukuRequestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
		if (grantResult != PackageManager.PERMISSION_GRANTED) {
			Log.w(TAG, "Shizuku permission not granted")
			Toast.makeText(applicationContext, getString(R.string.shizuku_permission_denied), Toast.LENGTH_SHORT).show()
			return@OnRequestPermissionResultListener
		}
		startUserService()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "Activity created; SDK_INT=${Build.VERSION.SDK_INT}")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		DynamicColors.applyToActivityIfAvailable(this)
		setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

		Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
		Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
		Shizuku.addRequestPermissionResultListener(shizukuRequestPermissionResultListener)

		findViewById<Button>(R.id.button_on).setOnClickListener { setDisplayOn() }
		findViewById<Button>(R.id.button_off).setOnClickListener { setDisplayOff() }
	}

	override fun onDestroy() {
		super.onDestroy()
		Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
		Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
		Shizuku.removeRequestPermissionResultListener(shizukuRequestPermissionResultListener)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_about -> AlertDialog.Builder(this)
				.setTitle(R.string.about)
				.setMessage(Utils.format(getString(R.string.about_contents), mapOf(
					"app_name" to getString(R.string.app_name),
					"version_name" to BuildConfig.VERSION_NAME,
					"home_url" to getString(R.string.homepage_url),
					"license" to getString(R.string.license),
				))).setPositiveButton(R.string.open_homepage) { _, _ ->
					val intent = Intent(Intent.ACTION_VIEW)
					intent.data = getString(R.string.homepage_url).toUri()
					startActivity(intent)
				}
				.show()

			else -> return super.onOptionsItemSelected(item)
		}
		return true
	}

	fun checkShizukuPermission(code: Int): Boolean {
		if (Shizuku.isPreV11()) {
			return false
		}
		if (!shizukuBinderReceived) {
			Toast.makeText(applicationContext, getString(R.string.shizuku_binder_not_received), Toast.LENGTH_SHORT).show()
			return false
		}
		if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
			return true
		} else if (Shizuku.shouldShowRequestPermissionRationale()) {
			Toast.makeText(applicationContext, getString(R.string.shizuku_permission_denied), Toast.LENGTH_SHORT).show()
			return false
		} else {
			Toast.makeText(applicationContext, getString(R.string.shizuku_request_permission), Toast.LENGTH_SHORT).show()
			Shizuku.requestPermission(code)
			return false
		}
	}

	fun setDisplayOn() {
		if (userService == null || !userService!!.asBinder().pingBinder()) {
			Log.e(TAG, "UserService is null or dead, starting UserService")
			Toast.makeText(applicationContext, getString(R.string.user_service_unavailable), Toast.LENGTH_SHORT).show()
			return
		}
		userService!!.setDisplayOn()
	}

	fun setDisplayOff() {
		if (userService == null || !userService!!.asBinder().pingBinder()) {
			Log.e(TAG, "UserService is null or dead")
			Toast.makeText(applicationContext, getString(R.string.user_service_unavailable), Toast.LENGTH_SHORT).show()
			return
		}
		userService!!.setDisplayOff()
	}

	fun startUserService() {
		if (Shizuku.getVersion() < 10) {
			Log.e(TAG, "Shizuku version ${Shizuku.getVersion()} < 10 is too low for UserService")
			Toast.makeText(applicationContext, getString(R.string.shizuku_version_too_low), Toast.LENGTH_SHORT).show()
			return
		}
		Shizuku.bindUserService(userServiceArgs, userServiceConnection)
	}

	fun stopUserService() {
		if (Shizuku.getVersion() < 10) {
			return
		}
		Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
	}



}
