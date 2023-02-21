package com.example

import com.example.plugins.PillCount
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

fun main() {
    //TODO: This will ALWAYS be running! But when the pi regains wifi, restart this!

    //TODO: This is the only thing that needs to be restarted on wifi changes
    // if there is no wifi, run wifi-connect!
    val ipAddresses = getIpAddresses()

    // Create a JmDNS instance
    val jmdns = try {
        val jmdns = JmDNS.create(
            InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }!!.address)
        )

        // Register a service
        val serviceInfo = ServiceInfo.create(
            "_http._tcp.local.",
            "pillcounter",
            8080,
            "path=index.html"
        )

        jmdns.registerService(serviceInfo)
        jmdns
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    val server = embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                server.stop(1, 5, TimeUnit.SECONDS)
                jmdns?.unregisterAllServices()
            }
        )
    Thread.currentThread().join()
}

fun Application.module() {
    val pillInfoFile = File("pillInfo.json")
    try {
        if (!pillInfoFile.exists()) {
            pillInfoFile.createNewFile()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val initialInfo = try {
        Json.decodeFromString<PillWeights>(pillInfoFile.readText())
    } catch (e: Exception) {
        e.printStackTrace()
        PillWeights()
    }
    val fullWeight = MutableStateFlow(0)
    val pillWeights = MutableStateFlow(initialInfo)
    val pillCount = combine(
        fullWeight, pillWeights
    ) { f, p -> PillCount(calculatePillCount(f, p), p) }

    configureSockets(pillCount)
    configureSerialization()
    configureRouting(fullWeight, pillWeights, pillInfoFile)
    configurePiSetup(fullWeight)
    configureWifi()

    fullWeight
        .onEach { println("Full Weight: $it") }
        .launchIn(this)

    pillWeights
        .onEach { println("Pill Weight: $it") }
        .launchIn(this)

    pillCount
        .onEach { println("Pill Count: $it") }
        .launchIn(this)
}

@Serializable
data class PillWeights(
    val name: String = "",
    val bottleWeight: Double = 0.0,
    val pillWeight: Double = 0.0,
    val uuid: String = ""
)

fun calculatePillCount(weight: Int, pillWeight: PillWeights) =
    (weight - pillWeight.bottleWeight) / pillWeight.pillWeight

fun getIpAddresses(): List<IpAddressInfo> {
    val ip = mutableListOf<IpAddressInfo>()
    try {
        val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (enumNetworkInterfaces.hasMoreElements()) {
            val networkInterface = enumNetworkInterfaces.nextElement()
            val enumInetAddress = networkInterface.inetAddresses
            while (enumInetAddress.hasMoreElements()) {
                val inetAddress = enumInetAddress.nextElement()
                if (inetAddress.isLoopbackAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.Loopback)
                } else if (inetAddress.isSiteLocalAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.SiteLocal)
                } else if (inetAddress.isLinkLocalAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.LinkLocal)
                } else if (inetAddress.isMulticastAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.Multicast)
                }
                ip += IpAddressInfo(
                    inetAddress.hostAddress,
                    when {
                        inetAddress.isLoopbackAddress -> AddressType.Loopback
                        inetAddress.isSiteLocalAddress -> AddressType.SiteLocal
                        inetAddress.isLinkLocalAddress -> AddressType.LinkLocal
                        inetAddress.isMulticastAddress -> AddressType.Multicast
                        else -> AddressType.None
                    }
                )
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return ip
}

enum class AddressType {
    Loopback, SiteLocal, LinkLocal, Multicast, None
}

data class IpAddressInfo(
    val address: String,
    val addressType: AddressType
)