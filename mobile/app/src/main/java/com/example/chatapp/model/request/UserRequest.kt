package com.example.chatapp.model.request

import kotlinx.serialization.Serializable


@Serializable
class UserRequest {
    private var username : String
    private var email : String
    private var password : String
    private var confirmPassword: String

    constructor(username: String, email: String, password: String, confirmPassword: String) {
        this.username = username
        this.email = email
        this.password = password
        this.confirmPassword = confirmPassword
    }

    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }

    fun getConfirmPassword(): String {
        return confirmPassword
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun setConfirmPassword(confirmPassword: String) {
        this.confirmPassword = confirmPassword
    }

}