package com.pupptmstr.scooternav_s

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pupptmstr.scooternav_s.models.Element
import com.pupptmstr.scooternav_s.models.Response
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

suspend fun main() {
    val client = HttpClient(CIO.create{
        requestTimeout = 0
    }) {
        expectSuccess = false
    }

    val body = getOverpassApiData(client, getAllPedestrianStreets())

//    val database = DatabaseConnector("bolt://localhost:7687", "neo4j", "test")
//
//    database.printGreeting("hello, world!")

}

suspend fun getOverpassApiData(httpClient: HttpClient, requestString: String): Array<Element> {
    println("зашел в метод, начинаю делать запрос")
    val response = httpClient.post("https://overpass-api.de/api/interpreter") {
        setBody(requestString)
    }
    println("начинаю преобразовывать ответ в данные")
//    val builder = GsonBuilder()
//    val gson = builder
//        .setPrettyPrinting()
//        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//        .create()
//    val responseDataAsObject = gson.fromJson(response.body<String>(), Response::class.java)
    writeResponseToFile("response.json", response.body<String>())
    println("закончил запись в файл")
//    return responseDataAsObject.elements
    return emptyArray()
}

suspend fun writeResponseToFile(fileName: String, requestBody: String) {
    println("зашел в метод")
    val file = File(fileName)

    if (!file.exists()) {
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
    }

    val writer = BufferedWriter(withContext(Dispatchers.IO) {
        FileWriter(file)
    })
    println("начинаю запись в файл")
    withContext(Dispatchers.IO) {
        writer.write(requestBody)
    }
}