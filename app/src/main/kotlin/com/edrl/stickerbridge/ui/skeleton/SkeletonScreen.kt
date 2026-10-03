package com.edrl.stickerbridge.ui.skeleton

import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.pack.whatsapp.WhatsAppContract

@Composable
fun SkeletonScreen(viewModel: SkeletonViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val addPackLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onAddResult(result.resultCode == Activity.RESULT_OK)
        }

    LaunchedEffect(state.pendingAdd) {
        val pack = state.pendingAdd ?: return@LaunchedEffect
        viewModel.onAddLaunched()
        try {
            addPackLauncher.launch(WhatsAppContract.addPackIntent(pack))
        } catch (_: ActivityNotFoundException) {
            viewModel.onAddResult(accepted = false)
        }
    }

    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.skeleton_title), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = state.link,
                onValueChange = viewModel::onLinkChange,
                label = { Text(stringResource(R.string.skeleton_link_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("skeleton-link"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::run,
                    enabled = !state.running && state.link.isNotBlank(),
                    modifier = Modifier.testTag("skeleton-run"),
                ) { Text(stringResource(R.string.skeleton_run)) }
                OutlinedButton(
                    onClick = viewModel::addAnother,
                    enabled = !state.running && state.canAddAnother,
                    modifier = Modifier.testTag("skeleton-add-another"),
                ) { Text(stringResource(R.string.skeleton_add_another)) }
            }
            if (state.running) CircularProgressIndicator()
            state.lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
