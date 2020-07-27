package com.dhy.apiholder.demo

enum class ServerConfig(val release: String) {
    APP("1"), PUSH("2");

    fun dynamic(): String {
        return name
    }
}