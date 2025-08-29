package io.github.ulysseszh.displaytoggle;

// All functions that need ADB permission to implement should be defined here.
interface IShizukuUserService {
	void destroy() = 16777114;
	void setDisplayPowerMode(int mode) = 1108;
}
