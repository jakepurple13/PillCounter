package com.example

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
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
            val response = RunPython.runPythonCodeAsync("hx711_example.py").await()
                .toFloatOrNull()
            emit(response)
        }
    }
        .filterNotNull()
        .map { it.roundToInt() }
        .onEach { valueUpdate.emit(it) }
        .launchIn(this)

}

object RunPython {
    @Suppress("BlockingMethodInNonBlockingContext")
    fun runPythonCodeAsync(fileName: String, vararg args: String) = GlobalScope.async {
        val command = "python3 utilities/$fileName ${args.joinToString(" ")}"
        val process = Runtime.getRuntime().exec(command)
        //TODO: Can this be last?
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.lines().collect(Collectors.joining("\n"))
    }
}