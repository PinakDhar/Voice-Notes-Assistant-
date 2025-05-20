package com.example.voicenotes.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkUtils private constructor(context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkLiveData = MutableLiveData<Boolean>()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkLiveData.postValue(true)
        }
        
        override fun onLost(network: Network) {
            networkLiveData.postValue(false)
        }
        
        override fun onUnavailable() {
            networkLiveData.postValue(false)
        }
    }
    
    init {
        // Register network callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Set initial network state
        updateNetworkState()
    }
    
    fun getNetworkStatus(): LiveData<Boolean> = networkLiveData
    
    fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
    
    private fun updateNetworkState() {
        networkLiveData.postValue(isConnected())
    }
    
    companion object {
        @Volatile
        private var instance: NetworkUtils? = null
        
        fun getInstance(context: Context): NetworkUtils {
            return instance ?: synchronized(this) {
                instance ?: NetworkUtils(context.applicationContext).also { instance = it }
            }
        }
    }
}
