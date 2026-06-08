package com.example.gastospersonales

object CodeGenerator {
    private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private const val CODE_LENGTH = 6

    fun generateGroupCode(): String {
        return (1..CODE_LENGTH)
            .map { CHARACTERS.random() }
            .joinToString("")
    }
}
