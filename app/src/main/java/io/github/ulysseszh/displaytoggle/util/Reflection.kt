package io.github.ulysseszh.displaytoggle.util

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Method

object Reflection {
	private const val TAG = "Reflection"

	private val getDeclaredMethodMethod = Class::class.java.getDeclaredMethod(
		"getDeclaredMethod",
		String::class.java,
		arrayOf<Class<*>>()::class.java
	)
	private val loadLibrary0Method = getDeclaredMethod(Runtime::class.java, "loadLibrary0", Class::class.java, String::class.java)
	private var servicesClassLoader = getServicesClassLoader()
	private val classCache = mutableMapOf<String, Class<*>>()

	fun getServicesClassLoader(): ClassLoader {
		return getDeclaredMethod(
			@SuppressLint("PrivateApi") Class.forName("com.android.internal.os.ClassLoaderFactory"),
			"createClassLoader",
			String::class.java,
			String::class.java,
			String::class.java,
			ClassLoader::class.java,
			Int::class.javaPrimitiveType!!,
			Boolean::class.javaPrimitiveType!!,
			String::class.java
		).invoke(
			null,
			"/system/framework/services.jar",
			null,
			null,
			ClassLoader.getSystemClassLoader(),
			0,
			true,
			null
		) as ClassLoader
	}

	fun getDeclaredMethod(
		className: String,
		methodName: String,
		vararg parameterTypes: Class<*>
	): Method {
		val clazz: Class<*> = if (classCache.containsKey(className)) {
			classCache[className]!!
		} else {
			try {
				Class.forName(className)
			} catch (e: ClassNotFoundException) {
				Log.d(TAG, "Class $className not found in system class loader, trying services class loader")
				val result = servicesClassLoader.loadClass(className)
				loadLibrary0Method.invoke(Runtime.getRuntime(), result, "android_servers")
				result
			}
		}
		classCache[className] = clazz
		return getDeclaredMethod(clazz, methodName, *parameterTypes)
	}

	fun getDeclaredMethod(
		clazz: Class<*>,
		methodName: String,
		vararg parameterTypes: Class<*>
	): Method {
		val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			HiddenApiBypass.getDeclaredMethod(clazz, methodName, *parameterTypes)
		} else {
			val result = getDeclaredMethodMethod.invoke(clazz, methodName, parameterTypes) as Method
			result.isAccessible = true
			result
		}
		Log.d(TAG, "Found method: $result")
		return result
	}
}
