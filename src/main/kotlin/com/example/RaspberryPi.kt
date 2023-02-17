package com.example

import com.pi4j.Pi4J
import com.pi4j.ktx.console
import com.pi4j.ktx.io.analog.analogInput
import com.pi4j.ktx.io.analog.listen
import com.pi4j.ktx.io.analog.mockProvider
import com.pi4j.ktx.printRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

fun piSetup(
    valueUpdate: MutableStateFlow<Int> = MutableStateFlow(0)
) = console {
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
}

object RunPython {
    @Suppress("BlockingMethodInNonBlockingContext")
    fun runPythonCodeAsync(fileName: String, vararg args: String) = GlobalScope.async {
        val command = "python3 src/main/python/$fileName ${args.joinToString(" ")}"
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.lines().collect(Collectors.joining("\n"))
    }
}