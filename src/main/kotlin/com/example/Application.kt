package com.example

import com.example.plugins.PillCount
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    val pillCount = combine(fullWeight, pillWeights) { f, p -> PillCount(calculatePillCount(f, p), p) }
    configureSockets(pillCount)
    configureSerialization()
    configureRouting(fullWeight, pillWeights, pillInfoFile)
    launch { piSetup(fullWeight) }

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
    val bottleWeight: Double = 0.0,
    val pillWeight: Double = 0.0
)


fun calculatePillCount(weight: Int, pillWeight: PillWeights) =
    (weight - pillWeight.bottleWeight) / pillWeight.pillWeight