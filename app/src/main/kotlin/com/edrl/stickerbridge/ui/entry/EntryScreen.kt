package com.edrl.stickerbridge.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R

/** P1 — paste a link, import from the gallery, or open the packs (FR1.2, FR7.3). */
@Composable
fun EntryScreen(
    invalidLink: Boolean,
    onSearch: (String) -> Unit,
    onImport: () -> Unit,
    onOpenPacks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var link by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.entry_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.entry_hint), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text(stringResource(R.string.entry_link_label)) },
            singleLine = true,
            isError = invalidLink,
            supportingText = { if (invalidLink) Text(stringResource(R.string.error_invalid_link)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(link) }),
            modifier = Modifier.fillMaxWidth().testTag("entry-link"),
        )
        Button(
            onClick = { onSearch(link) },
            enabled = link.isNotBlank(),
            modifier = Modifier.fillMaxWidth().testTag("entry-search"),
        ) { Text(stringResource(R.string.entry_search)) }
        OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth().testTag("entry-import")) {
            Text(stringResource(R.string.entry_import))
        }
        TextButton(onClick = onOpenPacks, modifier = Modifier.fillMaxWidth().testTag("entry-packs")) {
            Text(stringResource(R.string.entry_packs))
        }
    }
}
