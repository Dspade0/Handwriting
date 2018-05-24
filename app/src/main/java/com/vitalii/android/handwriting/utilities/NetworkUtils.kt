package com.vitalii.android.handwriting.utilities

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

fun buildUrl(address: String): URL {
    val builder = StringBuilder(address)
    val scheme = "http://"
    val httpPort = "8080"
    if (!builder.contains(scheme)) builder.insert(0, scheme)
    if (!builder.contains(""":\d{2,4}""")) builder.append(":$httpPort")
    return URL(builder.toString())
}

fun sendPostToHttpUrl(data: String, url: URL) {
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    val outputStream = DataOutputStream(connection.outputStream)
    outputStream.apply {
        writeBytes(data)
        flush()
        close()
    }
    val responseCode = connection.responseCode
    connection.disconnect()
}

fun getResponseFromHttpUrl(url: URL): String? {
    val stream = url.openConnection().getInputStream()
    val scanner = Scanner(stream)
    scanner.useDelimiter("\\A")
    return if (scanner.hasNext()) scanner.next() else null
}
