package io.github.ulysseszh.displaytoggle.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.google.android.material.color.DynamicColors
import io.github.ulysseszh.displaytoggle.BuildConfig
import io.github.ulysseszh.displaytoggle.R
import io.github.ulysseszh.displaytoggle.ShizukuHelper
import io.github.ulysseszh.displaytoggle.util.Display
import io.github.ulysseszh.displaytoggle.util.Utils

class MainActivity : AppCompatActivity() {
	companion object {
		private val TAG = MainActivity::class.simpleName!!
	}

	private val shizukuHelper = ShizukuHelper(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "Activity created; SDK_INT=${Build.VERSION.SDK_INT}")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		DynamicColors.applyToActivityIfAvailable(this)
		setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

		shizukuHelper.setUp()
		findViewById<Button>(R.id.button_on).setOnClickListener {
			shizukuHelper.setDisplayPowerMode(Display.PowerMode.NORMAL)
		}
		findViewById<Button>(R.id.button_off).setOnClickListener {
			shizukuHelper.setDisplayPowerMode(Display.PowerMode.OFF)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		shizukuHelper.cleanUp()
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
					"homepage_url" to getString(R.string.homepage_url),
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
}
