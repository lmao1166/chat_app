package com.example.chatapp.model.request

import kotlinx.serialization.Serializable


@Serializable
class AuthRequest {
    private var email : String
    private var password : String

    constructor(email: String, password: String) {
        this.email = email
        this.password = password
    }

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }
}