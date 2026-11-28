package com.example.flashcardapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryValidationTest {
    @Test
    fun trimField_preservesSpacesAndLineBreaksInsideText() {
        assertEquals("First  line\nSecond line", trimField(" \tFirst  line\nSecond line\n "))
    }

    @Test
    fun profileName_requiresTextAndHasNoSpecifiedMaximum() {
        assertFalse(isValidProfileName(""))
        assertFalse(isValidProfileName(" \t\n"))
        assertTrue(isValidProfileName(" A "))
        assertTrue(isValidProfileName("a".repeat(2001)))
    }

    @Test
    fun setNames_acceptOneThroughEightyCharactersAfterTrimming() {
        assertFalse(isValidSetName(""))
        assertFalse(isValidSetName(" \t\n"))
        assertTrue(isValidSetName(" A "))
        assertTrue(isValidSetName(" " + "a".repeat(80) + "\n"))
        assertFalse(isValidSetName("a".repeat(81)))
    }

    @Test
    fun description_blankIsValidAndStoredAsNull() {
        assertTrue(isValidDescription(""))
        assertNull(normalizeDescription(""))
        assertTrue(isValidDescription(" \t\n"))
        assertNull(normalizeDescription(" \t\n"))
    }

    @Test
    fun description_checksFiveHundredCharacterLimitAfterTrimming() {
        val description = "a".repeat(500)
        val padded = "\t$description\n"

        assertTrue(isValidDescription(padded))
        assertEquals(description, normalizeDescription(padded))
        assertFalse(isValidDescription(" $description! "))
    }

    @Test
    fun cardFaces_acceptOneThroughTwoThousandCharactersAfterTrimming() {
        assertFalse(isValidCardFace(""))
        assertFalse(isValidCardFace(" \t\n"))
        assertTrue(isValidCardFace(" A "))
        assertTrue(isValidCardFace("\t" + "a".repeat(2000) + " "))
        assertFalse(isValidCardFace("a".repeat(2001)))
    }

    @Test
    fun identicalNamesAndCardTextCanBeUsedAgain() {
        val names = listOf("Biology", "Biology")
        val faces = listOf("Cell", "Cell")

        assertTrue(names.all { isValidSetName(it) })
        assertTrue(faces.all { isValidCardFace(it) })
    }
}
