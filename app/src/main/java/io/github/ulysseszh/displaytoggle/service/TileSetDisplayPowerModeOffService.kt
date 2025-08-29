package io.github.ulysseszh.displaytoggle.service

import io.github.ulysseszh.displaytoggle.util.Display.PowerMode

class TileSetDisplayPowerModeOffService: TileSetDisplayPowerModeService() {
	override val powerMode = PowerMode.OFF
}
