package app.pillion.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pillion.core.Logger

/**
 * Shows the app's recent in-memory log — including the stack trace of a crash from a previous run —
 * so a report can be produced on the phone itself, with no PC, root or logcat access.
 */
@Composable
internal fun DiagnosticsDialog(onClose: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var lines by remember { mutableStateOf(Logger.snapshot()) }
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Diagnostics") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
            ) {
                if (lines.isEmpty()) {
                    Text(
                        "Nothing buffered yet. Start a stream, reproduce the problem, then open this " +
                            "screen again.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    SelectionContainer {
                        Text(
                            lines.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    clipboard.setText(AnnotatedString(lines.joinToString("\n")))
                    copied = true
                },
                enabled = lines.isNotEmpty(),
            ) { Text(if (copied) "Copied" else "Copy all") }
        },
        dismissButton = {
            TextButton(onClick = {
                Logger.clear()
                lines = emptyList()
                copied = false
            }) { Text("Clear") }
        },
    )
}
