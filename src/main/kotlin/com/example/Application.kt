package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val pills = MutableStateFlow(0)
    configureSockets()
    configureSerialization()
    configureRouting()
    launch { piSetup(pills) }
}

data class PillWeights(
    val bottleWeight: Double = 0.0,
    val pillWeight: Double = 0.0
)
