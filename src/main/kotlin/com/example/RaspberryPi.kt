package com.example

import com.example.plugins.PillCount
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.stream.Collectors
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

fun Application.configurePiSetup(
    valueUpdate: MutableStateFlow<Int>,
    pillCount: Flow<PillCount>,
    version: String
) {
    /*pi4jAsync {
        digitalInput(12) {
            id("Top Button")
            piGpioProvider()
        }.onLow {
            println("Top Pressed!")
        }

        digitalInput(15) {
            id("Bottom Button")
            piGpioProvider()
        }.onLow {
            println("Bottom Pressed!")
        }
    }*/

    //TODO: On top button press, this will change to true for 10 minutes, then it will go back to false
    // OR on bottom button press, it will change to false
    val updateQuickly = MutableStateFlow(true)

    launch {
        delay(10000)
        updateQuickly.emit(false)
    }

    pillCount
        .combine(updateQuickly) { p, u -> p to u }
        .debounce { if (it.second) 1000 else 3.minutes.inWholeMilliseconds }
        .map { it.first }
        .onEach {
            println("Updating screen")
            val ip = try {
                InetAddress.getByName(getIpAddresses().find { it.addressType == AddressType.SiteLocal }?.address)
            } catch (e: Exception) {
                null
            }?.hostAddress?.let { if (it == "127.0.0.1") null else it }
            RunCommand.runPythonCodeAsync(
                "/home/pi/Desktop/einkscreendisplay.py",
                "IP: ${ip ?: "No Internet"}",
                it.pillWeights.name,
                "~${it.count} pills",
                "${it.pillWeights.pillWeight}(g) - ${it.pillWeights.bottleWeight}(g)",
                "PillCounter v$version"
            ).await()
        }
        .flowOn(Dispatchers.IO)
        .launchIn(this)

    /*RunCommand.runPythonCodeAsyncFlow("/home/pi/Desktop/button_input.py")
        .onEach {
            when (it) {
                "Switch 1" -> println("Do Switch 1 Action")
                "Switch 2" -> println("Do Switch 2 Action")
            }
        }
        .launchIn(this)*/

    flow {
        while (true) {
            delay(10000)
            val response = RunCommand.runPythonCodeAsync("/home/pi/Desktop/hx711_example.py")
                .await()
                .toFloatOrNull()
            emit(response)
        }
    }
        .filterNotNull()
        .map { it.roundToInt() }
        .onEach { println("Reading from sensor: $it") }
        .onEach { valueUpdate.emit(it) }
        .launchIn(this)
}

class NetworkHandling {

    private var jmdns: JmDNS? = null

    private fun setup() {
        try {
            if (jmdns == null) {
                val ipAddresses = getIpAddresses()

                jmdns = JmDNS.create(
                    InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }!!.address)
                )

                // Register a service
                val serviceInfo = ServiceInfo.create(
                    "_http._tcp.local.",
                    "pillcounter",
                    8080,
                    "path=index.html"
                )

                jmdns?.registerService(serviceInfo)
            }
        } catch (e: Exception) {
            println("Something went wrong here!")
            e.printStackTrace()
        }
    }

    suspend fun internetListener() {
        delay(10000)
        while (true) {
            try {
                val ipAddresses = getIpAddresses()
                if (
                    !InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }?.address)
                        .isReachable(10000)
                ) {
                    throw Exception("Can't reach network!")
                } else {
                    setup()
                }
                delay(10000)
            } catch (e: Exception) {
                e.printStackTrace()
                closeAll()
                //RunCommand.runAsync("sudo wifi-connect").await()
            }
        }
    }

    fun closeAll() {
        jmdns?.unregisterAllServices()
        jmdns = null
    }
}

fun Application.configureWifi(networkHandling: NetworkHandling) {
    launch { networkHandling.internetListener() }
}

object RunCommand {
    //val command = "python3 utilities/$fileName ${args.joinToString(" ")}"

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun runPythonCodeAsync(fileName: String, vararg args: String) =
        runAsync("python3", fileName, *args)

    fun runPythonCodeAsyncFlow(fileName: String, vararg args: String): Flow<String?> = flow {
        Runtime.getRuntime().exec(arrayOf("python3", fileName, *args))
            .inputStream.bufferedReader().use { r ->
                var line: String?
                while (r.readLine().also { l -> line = l } != null) {
                    emit(line)
                }
            }
    }
        .flowOn(Dispatchers.IO)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun runAsync(command: String) = coroutineScope {
        async {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.lines().collect(Collectors.joining("\n"))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun runAsync(vararg command: String) = coroutineScope {
        async {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.lines().collect(Collectors.joining("\n"))
        }
    }
}