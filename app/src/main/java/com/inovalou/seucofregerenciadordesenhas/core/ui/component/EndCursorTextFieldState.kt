package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class EndCursorTextFieldState(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
    val onFocusChanged: (Boolean) -> Unit
)

@Composable
fun rememberEndCursorTextFieldState(
    text: String,
    onTextChange: (String) -> Unit,
    moveCursorToEndOnFocus: Boolean = true
): EndCursorTextFieldState {
    val currentText by rememberUpdatedState(text)
    var textFieldValue by remember { mutableStateOf(text.toInitialTextFieldValue()) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(text, textFieldValue.text) {
        textFieldValue = textFieldValue.syncWithExternalText(text)
    }

    return EndCursorTextFieldState(
        value = textFieldValue,
        onValueChange = { value ->
            textFieldValue = value
            if (value.text != currentText) {
                onTextChange(value.text)
            }
        },
        onFocusChanged = { focused ->
            if (focused && !isFocused && moveCursorToEndOnFocus) {
                textFieldValue = textFieldValue.withCursorAtTextEnd()
            }
            isFocused = focused
        }
    )
}

fun String.toInitialTextFieldValue(): TextFieldValue =
    TextFieldValue(text = this)

fun TextFieldValue.withCursorAtTextEnd(): TextFieldValue =
    copy(selection = TextRange(text.length))

fun TextFieldValue.syncWithExternalText(text: String): TextFieldValue =
    if (this.text == text) {
        this
    } else {
        copy(
            text = text,
            selection = selection.constrainToText(text),
            composition = null
        )
    }

private fun TextRange.constrainToText(text: String): TextRange {
    val endExclusive = text.length
    return TextRange(
        start = start.coerceIn(0, endExclusive),
        end = end.coerceIn(0, endExclusive)
    )
}
