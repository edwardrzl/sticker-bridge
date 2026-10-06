package com.edrl.stickerbridge.ui.entry

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Brand()
        LinkField(link, invalidLink, onChange = { link = it }, onSearch = { onSearch(link) })
        Button(
            onClick = { onSearch(link) },
            enabled = link.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("entry-search"),
        ) {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, Modifier.size(20.dp))
            Text(stringResource(R.string.entry_search), Modifier.padding(start = 8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Shortcut(R.drawable.ic_image, R.string.entry_import, onImport, Modifier.weight(1f).testTag("entry-import"))
            Shortcut(
                R.drawable.ic_layers,
                R.string.entry_packs,
                onOpenPacks,
                Modifier.weight(1f).testTag("entry-packs"),
            )
        }
        Text(
            stringResource(R.string.entry_share_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Brand() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(8.dp))
        Image(
            painterResource(R.drawable.ic_brand_mark),
            contentDescription = null,
            modifier =
                Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                    .padding(12.dp),
        )
        Text(stringResource(R.string.entry_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.entry_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The link box, with a button that pastes whatever was copied in TikTok. */
@Composable
private fun LinkField(
    link: String,
    invalid: Boolean,
    onChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = link,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.entry_link_label)) },
        singleLine = true,
        isError = invalid,
        supportingText = { if (invalid) Text(stringResource(R.string.error_invalid_link)) },
        trailingIcon = {
            IconButton(onClick = { onChange(copiedText(context)) }, modifier = Modifier.testTag("entry-paste")) {
                Icon(painterResource(R.drawable.ic_paste), stringResource(R.string.entry_paste))
            }
        },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = Modifier.fillMaxWidth().testTag("entry-link"),
    )
}

@Composable
private fun Shortcut(
    icon: Int,
    label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(label), style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** What the person copied last, or nothing. It is read only when they tap "paste". */
private fun copiedText(context: Context): String {
    val clip = context.getSystemService(ClipboardManager::class.java)?.primaryClip
    return clip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(context)
        ?.toString()
        .orEmpty()
}
