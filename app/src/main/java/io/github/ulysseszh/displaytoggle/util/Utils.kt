package io.github.ulysseszh.displaytoggle.util

object Utils {
	fun format(template: String, arguments: Map<String, String>): String {
		var result = template
		for ((key, value) in arguments) {
			result = result.replace("%{$key}", value)
		}
		return result
	}
}
