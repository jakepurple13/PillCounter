package com.example

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.stream.Collectors
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlin.math.roundToInt

fun Application.configurePiSetup(
    valueUpdate: MutableStateFlow<Int> = MutableStateFlow(0)
) {
    /*console {
        title("Hello World")
        Pi4J.newAutoContext().run PI4J@{
            describe()
            analogInput(24) {
                id("weight")
                name("Weight Sensor")
                mockProvider()
                //piGpioProvider()
            }.run {
                printRegistry(this@PI4J)
                listen {
                    valueUpdate.tryEmit(it.value())
                }
            }
        }
    }*/

    flow {
        while (true) {
            delay(1000)
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
        runAsync("python3 $fileName ${args.joinToString(" ")}")

    @Suppress("BlockingMethodInNonBlockingContext")
    fun runPythonCodeAsyncFlow(fileName: String, vararg args: String): Flow<String> {
        var process: Process? = null
        return flow {
            process = Runtime.getRuntime().exec("python3 $fileName ${args.joinToString(" ")}")
            process!!.inputStream.bufferedReader().use { r ->
                var line: String?
                while (r.readLine().also { l -> line = l } != null) {
                    line?.let { emit(it) }
                }
            }
        }.onCompletion { process?.destroy() }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun runAsync(command: String) = coroutineScope {
        async {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.lines().collect(Collectors.joining("\n"))
        }
    }
}