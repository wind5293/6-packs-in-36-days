package com.example.fitflow.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

data class NetworkState(
    val isConnected: Boolean,
    val isWifi: Boolean,
    val isCellular: Boolean
)

object NetworkStateHelper {
    fun getNetworkState(context: Context): NetworkState {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return NetworkState(
                isConnected = false,
                isWifi = false,
                isCellular = false
            )
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkState(
                isConnected = false,
                isWifi = false,
                isCellular = false
            )

            val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            NetworkState(
                isConnected = hasInternet,
                isWifi = isWifi,
                isCellular = isCellular
            )
        } catch (_: SecurityException) {
            NetworkState(
                isConnected = false,
                isWifi = false,
                isCellular = false
            )
        }
    }
}
