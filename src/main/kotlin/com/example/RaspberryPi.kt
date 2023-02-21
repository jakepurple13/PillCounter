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
            val response = RunCommand.runPythonCodeAsync("hx711_example.py")
                .await()
                .toFloatOrNull()
            emit(response)
        }
    }
        .filterNotNull()
        .map { it.roundToInt() }
        .onEach { valueUpdate.emit(it) }
        .launchIn(this)

}

fun Application.configureWifi() {
    launch {
        while (true) {
            try {
                val ipAddresses = getIpAddresses()
                if (
                    !InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }!!.address)
                        .isReachable(10000)
                ) {
                    throw Exception("Can't reach network!")
                }
                delay(10000)
            } catch (e: Exception) {
                e.printStackTrace()
                RunCommand.runAsync("sudo wifi-connect").await()
            }
        }
    }
}

object RunCommand {
    //TODO: Make a utilities folder on the pi where python scripts will go to
    //val command = "python3 utilities/$fileName ${args.joinToString(" ")}"
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun runPythonCodeAsync(fileName: String, vararg args: String) =
        runAsync("python3 $fileName ${args.joinToString(" ")}")

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