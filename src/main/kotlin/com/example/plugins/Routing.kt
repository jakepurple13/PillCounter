package com.example.plugins

import com.example.PillWeights
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun Application.configureRouting(
    pills: MutableStateFlow<Int>,
    pillWeights: MutableStateFlow<PillWeights>,
    pillInfoFile: File
) {
    routing {
        get("/currentCount") {
            val weight = pillWeights.value
            val pillCount = (pills.value - weight.pillWeight) / weight.pillWeight
            call.respond(PillCount(pillCount, pillWeights.value))
        }

        post<PillWeights> {
            pillWeights.tryEmit(it)
            pillInfoFile.writeText(Json.encodeToString(it))
        }
    }
}

@Serializable
data class PillCount(val count: Double, val pillWeights: PillWeights)