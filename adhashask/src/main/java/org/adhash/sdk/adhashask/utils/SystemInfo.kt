package org.adhash.sdk.adhashask.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import java.util.*
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.WindowManager
import org.adhash.sdk.adhashask.constants.LibConstants
import kotlin.math.sqrt
import android.text.TextUtils
import android.net.NetworkCapabilities
import org.adhash.sdk.adhashask.constants.ConnectionType


private val TAG = LibConstants.SDK_TAG + SystemInfo::class.java.name

class SystemInfo(private val context: Context) {

    fun getTimeZone(): Int {
        var timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
        timeZone = timeZone.substring(3, 6)
        return timeZone.toInt()
    }

    fun getScreenHeight(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display?.getSize(size)

        return size.y
    }

    fun getScreenWidth(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display?.getSize(size)

        return size.x
    }

    fun getPhoneType(): String {
        val metrics = context.resources.displayMetrics
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = sqrt((xInches * xInches + yInches * yInches).toDouble())
        return if (diagonalInches >= 6.5) {
            LibConstants.tablet
        } else {
            LibConstants.mobile
        }
    }

    fun getConnectionType(): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            capabilities?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.TRANSPORT_WIFI
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.TRANSPORT_CELULLAR
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.TRANSPORT_ETHERNET
                    hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.TRANSPORT_BLUETOOTH
                    else -> LibConstants.CONNECTION_UNKNOWN
                }
            } ?: ConnectionType.CONNECTION_UNKNOWN
        } else {
            connectivityManager.activeNetworkInfo?.let { networkInfo ->
                when {
                    networkInfo.type == ConnectivityManager.TYPE_WIFI -> ConnectionType.TRANSPORT_WIFI
                    networkInfo.type == ConnectivityManager.TYPE_MOBILE -> when (networkInfo.subtype) {
                        TelephonyManager.NETWORK_TYPE_1xRTT -> ConnectionType.CONNECTION_1xRTT
                        TelephonyManager.NETWORK_TYPE_CDMA -> ConnectionType.CONNECTION_CDMA
                        TelephonyManager.NETWORK_TYPE_EDGE -> ConnectionType.CONNECTION_EDGE
                        TelephonyManager.NETWORK_TYPE_GPRS -> ConnectionType.CONNECTION_GPRS
                        TelephonyManager.NETWORK_TYPE_IDEN -> ConnectionType.CONNECTION_IDEN
                        TelephonyManager.NETWORK_TYPE_GSM -> ConnectionType.CONNECTION_GSM
                        TelephonyManager.NETWORK_TYPE_EVDO_0 -> ConnectionType.CONNECTION_EVDO_0
                        TelephonyManager.NETWORK_TYPE_EVDO_A -> ConnectionType.CONNECTION_EVDO_A
                        TelephonyManager.NETWORK_TYPE_EVDO_B -> ConnectionType.CONNECTION_EVDO_B
                        TelephonyManager.NETWORK_TYPE_HSDPA -> ConnectionType.CONNECTION_HSDPA
                        TelephonyManager.NETWORK_TYPE_HSPA -> ConnectionType.CONNECTION_HSPA
                        TelephonyManager.NETWORK_TYPE_HSPAP -> ConnectionType.CONNECTION_HSPAP
                        TelephonyManager.NETWORK_TYPE_HSUPA -> ConnectionType.CONNECTION_HSUPA
                        TelephonyManager.NETWORK_TYPE_UMTS -> ConnectionType.CONNECTION_UMTS
                        TelephonyManager.NETWORK_TYPE_EHRPD -> ConnectionType.CONNECTION_EHRPD
                        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> ConnectionType.CONNECTION_TD_SCDMA
                        TelephonyManager.NETWORK_TYPE_LTE -> ConnectionType.CONNECTION_LTE
                        TelephonyManager.NETWORK_TYPE_IWLAN -> ConnectionType.CONNECTION_IWLAN
                        TelephonyManager.NETWORK_TYPE_NR -> ConnectionType.CONNECTION_NR
                        else -> ConnectionType.CONNECTION_UNKNOWN
                    }
                    else -> ConnectionType.CONNECTION_UNKNOWN
                }
            } ?: ConnectionType.CONNECTION_UNKNOWN
        }
    }

    fun getOrientationScreen(): String {
        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> return LibConstants.orientation_landscape
            Configuration.ORIENTATION_PORTRAIT -> return LibConstants.orientation_portrait
        }
        return LibConstants.CONNECTION_UNKNOWN
    }

    fun getTimeInUnix(): Long {
        val cal = Calendar.getInstance()
        val timeZone = cal.timeZone
        val calDate = Calendar.getInstance(TimeZone.getDefault()).time
        var milliseconds = calDate.time
        milliseconds += timeZone.getOffset(milliseconds)
        return milliseconds / 1000L
    }

    fun getPublishedLocation() =
        "https://play.google.com/store/apps/details?id=${context.packageName}" //todo pull parent package name

    fun getPlatform() = "Android ${getVersionName()} API ${getVersionCode()}"

    fun getUserAgent() = System.getProperty("http.agent") ?: ""

    fun getLanguage(): String = Locale.getDefault().displayLanguage

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.startsWith(manufacturer))
            capitalizeDeviceModel(model)
        else
            capitalizeDeviceModel(manufacturer) + " " + model
    }

    fun getCarrierId() = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
        .run { networkOperatorName } ?: ""

    private fun capitalizeDeviceModel(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }

    private fun getVersionName() = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun getVersionCode() = try {
        context.packageManager.getPackageInfo(context.packageName, 0).run {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                versionCode.toString()
            } else {
                longVersionCode.toString()
            }
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

}