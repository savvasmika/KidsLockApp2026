package com.example.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

class NsdHelper(private val context: Context) {
    private val tag = "KidLockNSD"
    private val serviceType = "_kidlock._tcp."
    private val nsdManager: NsdManager? =
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val _discoveredServices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredServices: StateFlow<List<DiscoveredDevice>> = _discoveredServices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    data class DiscoveredDevice(
        val serviceName: String,
        val host: InetAddress?,
        val port: Int,
        val deviceId: String = "",
        val deviceType: String = "Tablet"
    )

    fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("KidLockMulticast")?.apply {
                    setReferenceCounted(true)
                    acquire()
                }
            } else if (multicastLock?.isHeld == false) {
                multicastLock?.acquire()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to acquire multicast lock", e)
        }
    }

    fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to release multicast lock", e)
        }
    }

    /**
     * Registers a local service on the Child device.
     */
    fun registerService(serviceName: String, port: Int, deviceId: String) {
        acquireMulticastLock()
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = this@NsdHelper.serviceType
            this.port = port
            setAttribute("id", deviceId)
            setAttribute("type", "Tablet")
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d(tag, "Service registered successfully: ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Service registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d(tag, "Service unregistered: ${info.serviceName}")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Service unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(tag, "Error registering NSD service", e)
        }
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (e: Exception) {
                Log.e(tag, "Error unregistering NSD service", e)
            }
            registrationListener = null
        }
        releaseMulticastLock()
    }

    /**
     * Starts discovering nearby KIDLOCK services for Parent device.
     */
    fun startDiscovery() {
        stopDiscovery()
        acquireMulticastLock()
        _discoveredServices.value = emptyList()
        _isScanning.value = true

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(tag, "Service discovery started")
                _isScanning.value = true
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(tag, "Service found: ${service.serviceName}")
                if (service.serviceType.contains("kidlock")) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(tag, "Service lost: ${service.serviceName}")
                _discoveredServices.value = _discoveredServices.value.filterNot {
                    it.serviceName == service.serviceName
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(tag, "Discovery stopped: $serviceType")
                _isScanning.value = false
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Start discovery failed: $errorCode")
                _isScanning.value = false
                try {
                    nsdManager?.stopServiceDiscovery(this)
                } catch (_: Exception) {}
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Stop discovery failed: $errorCode")
                _isScanning.value = false
            }
        }

        try {
            nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(tag, "Error starting discovery", e)
            _isScanning.value = false
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Resolve failed: $errorCode for ${serviceInfo.serviceName}")
            }

            override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                Log.d(tag, "Service resolved: ${resolvedInfo.serviceName}, host: ${resolvedInfo.host}:${resolvedInfo.port}")
                val deviceId = try {
                    resolvedInfo.attributes["id"]?.let { String(it) } ?: ""
                } catch (_: Exception) { "" }

                val discovered = DiscoveredDevice(
                    serviceName = resolvedInfo.serviceName,
                    host = resolvedInfo.host,
                    port = resolvedInfo.port,
                    deviceId = deviceId
                )

                val current = _discoveredServices.value.toMutableList()
                val existingIndex = current.indexOfFirst { it.serviceName == discovered.serviceName }
                if (existingIndex >= 0) {
                    current[existingIndex] = discovered
                } else {
                    current.add(discovered)
                }
                _discoveredServices.value = current
            }
        }

        try {
            nsdManager?.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            Log.e(tag, "Error resolving service", e)
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager?.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e(tag, "Error stopping discovery", e)
            }
            discoveryListener = null
        }
        _isScanning.value = false
        releaseMulticastLock()
    }
}
