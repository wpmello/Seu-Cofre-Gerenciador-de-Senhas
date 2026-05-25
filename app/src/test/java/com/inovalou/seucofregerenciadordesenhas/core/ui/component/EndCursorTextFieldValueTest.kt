package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class EndCursorTextFieldValueTest {

    @Test
    fun givenInitialText_whenCreatingTextFieldValue_thenDoesNotForceCursorToTextEnd() {
        val value = "credential-name".toInitialTextFieldValue()

        assertEquals("credential-name", value.text)
        assertEquals(TextRange.Zero, value.selection)
    }

    @Test
    fun givenFieldGainsFocus_whenMovingCursor_thenPlacesSelectionAtTextEnd() {
        val value = TextFieldValue(
            text = "credential-name",
            selection = TextRange(3)
        )

        val focused = value.withCursorAtTextEnd()

        assertEquals("credential-name", focused.text)
        assertEquals(TextRange("credential-name".length), focused.selection)
    }

    @Test
    fun givenManualSelection_whenExternalTextHasNotChanged_thenPreservesSelection() {
        val value = TextFieldValue(
            text = "credential-name",
            selection = TextRange(3)
        )

        val synced = value.syncWithExternalText("credential-name")

        assertEquals("credential-name", synced.text)
        assertEquals(TextRange(3), synced.selection)
    }

    @Test
    fun givenRejectedLocalInput_whenExternalTextIsAcceptedValue_thenSyncsTextAndBoundsSelection() {
        val value = TextFieldValue(
            text = "credential-name-over-limit",
            selection = TextRange("credential-name-over-limit".length)
        )

        val synced = value.syncWithExternalText("credential-name")

        assertEquals("credential-name", synced.text)
        assertEquals(TextRange("credential-name".length), synced.selection)
    }
}
