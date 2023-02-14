package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val pillInfoFile = File("pillInfo.json")
    val initialInfo = try {
        Json.decodeFromString<PillWeights>(pillInfoFile.readText())
    } catch (e: Exception) {
        PillWeights()
    }
    val pills = MutableStateFlow(0)
    val pillWeights = MutableStateFlow(initialInfo)
    configureSockets()
    configureSerialization()
    configureRouting(pills, pillWeights, pillInfoFile)
    launch { piSetup(pills) }
}

@Serializable
data class PillWeights(
    val bottleWeight: Double = 0.0,
    val pillWeight: Double = 0.0
)
