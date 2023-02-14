package com.example

import com.pi4j.Pi4J
import com.pi4j.ktx.console
import com.pi4j.ktx.io.analog.analogInput
import com.pi4j.ktx.io.analog.listen
import com.pi4j.ktx.io.analog.mockProvider
import com.pi4j.ktx.printRegistry
import kotlinx.coroutines.flow.MutableStateFlow

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
        }.run {
            printRegistry(this@PI4J)
            listen {
                valueUpdate.tryEmit(it.value())
            }
        }
    }
}