package io.github.ulysseszh.displaytoggle.service

import android.service.quicksettings.TileService
import io.github.ulysseszh.displaytoggle.ShizukuHelper
import io.github.ulysseszh.displaytoggle.util.Display.PowerMode

abstract class TileSetDisplayPowerModeService: TileService() {
	abstract val powerMode: PowerMode

	private val shizukuHelper = ShizukuHelper(this)

	override fun onClick() {
		super.onClick()
		shizukuHelper.addServiceConnectedListener {
			shizukuHelper.setDisplayPowerMode(powerMode)
			shizukuHelper.cleanUp()
		}
		shizukuHelper.setUp()
	}
}
