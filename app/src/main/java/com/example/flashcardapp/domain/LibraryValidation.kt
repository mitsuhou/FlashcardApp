package com.example.flashcardapp.domain

private const val MAX_SET_NAME_LENGTH = 80
private const val MAX_DESCRIPTION_LENGTH = 500
private const val MAX_CARD_FACE_LENGTH = 2000

fun trimField(value: String): String = value.trim()

fun isValidProfileName(value: String): Boolean = trimField(value).isNotEmpty()

fun isValidSetName(value: String): Boolean =
    trimField(value).length in 1..MAX_SET_NAME_LENGTH

fun isValidDescription(value: String): Boolean =
    trimField(value).length <= MAX_DESCRIPTION_LENGTH

fun normalizeDescription(value: String): String? = trimField(value).ifEmpty { null }

fun isValidCardFace(value: String): Boolean =
    trimField(value).length in 1..MAX_CARD_FACE_LENGTH
