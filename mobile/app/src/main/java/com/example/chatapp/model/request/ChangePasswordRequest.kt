package com.example.chatapp.model.request

import kotlinx.serialization.Serializable

@Serializable
class ChangePasswordRequest {
    private var password : String
    private var newPassword: String
    private var confirmPassword : String

    constructor(password: String, newPassword: String, confirmPassword: String) {
        this.password = password
        this.newPassword = newPassword
        this.confirmPassword = confirmPassword
    }

    fun getPassword(): String {
        return password
    }

    fun getNewPassword(): String {
        return newPassword
    }

    fun getConfirmPassword(): String {
        return confirmPassword
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun setNewPassword(newPassword: String) {
        this.newPassword = newPassword
    }

    fun setConfirmPassword(confirmPassword: String) {
        this.confirmPassword = confirmPassword
    }
}