// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.JniUtils
import helium314.keyboard.latin.utils.NextScreenIcon
import helium314.keyboard.latin.utils.SubtypeLocaleUtils.displayName
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.UncachedInputMethodManagerUtils
import helium314.keyboard.latin.utils.previewDark
import helium314.keyboard.settings.ExpressiveShapes
import helium314.keyboard.settings.Illustration
import helium314.keyboard.settings.IllustrationCanvas
import helium314.keyboard.settings.SearchSettingsScreen
import helium314.keyboard.settings.initPreview
import helium314.keyboard.settings.preferences.Preference
import helium314.keyboard.settings.preferences.SettingsGroup
import helium314.keyboard.settings.screens.gesturedata.END_DATE_EPOCH_MILLIS
import helium314.keyboard.settings.screens.gesturedata.TWO_WEEKS_IN_MILLIS

@Composable
fun MainSettingsScreen(
    onClickAbout: () -> Unit,
    onClickTextCorrection: () -> Unit,
    onClickPreferences: () -> Unit,
    onClickSoundVibration: () -> Unit,
    onClickClipboard: () -> Unit,
    onClickToolbar: () -> Unit,
    onClickGestureTyping: () -> Unit,
    onClickDataGathering: () -> Unit,
    onClickAdvanced: () -> Unit,
    onClickAppearance: () -> Unit,
    onClickLanguage: () -> Unit,
    onClickLayouts: () -> Unit,
    onClickDictionaries: () -> Unit,
    onClickBack: () -> Unit,
) {
    SearchSettingsScreen(
        onClickBack = onClickBack,
        title = stringResource(R.string.ime_settings),
        settings = emptyList(),
    ) {
        val enabledSubtypes = SubtypeSettings.getEnabledSubtypes(true)
        val colors = MaterialTheme.colorScheme
        // icon container colors and shapes vary between the entries, so the list is easier to scan
        val primary = colors.primaryContainer to colors.onPrimaryContainer
        val secondary = colors.secondaryContainer to colors.onSecondaryContainer
        val tertiary = colors.tertiaryContainer to colors.onTertiaryContainer

        val typing = listOfNotNull(
            MainEntry(stringResource(R.string.language_and_layouts_title), R.drawable.ic_settings_languages, primary,
                ExpressiveShapes.Cookie, onClickLanguage, enabledSubtypes.joinToString(", ") { it.displayName() }),
            MainEntry(stringResource(R.string.settings_screen_correction), R.drawable.ic_settings_correction, tertiary,
                ExpressiveShapes.Sunny, onClickTextCorrection),
            if (JniUtils.sHaveGestureLib)
                MainEntry(stringResource(R.string.settings_screen_gesture), R.drawable.ic_settings_gesture, secondary,
                    ExpressiveShapes.Clover, onClickGestureTyping)
            else null,
            MainEntry(stringResource(R.string.dictionary_settings_category), R.drawable.ic_dictionary, primary,
                ExpressiveShapes.Soft, onClickDictionaries),
        )
        val keyboard = listOf(
            MainEntry(stringResource(R.string.settings_screen_keys), R.drawable.ic_settings_preferences, secondary,
                ExpressiveShapes.Pentagon, onClickPreferences),
            MainEntry(stringResource(R.string.settings_screen_sound_vibration), R.drawable.ic_settings_sound, tertiary,
                ExpressiveShapes.Flower, onClickSoundVibration),
            MainEntry(stringResource(R.string.settings_category_clipboard_history), R.drawable.sym_keyboard_clipboard_rounded,
                primary, ExpressiveShapes.Cookie, onClickClipboard),
            MainEntry(stringResource(R.string.settings_screen_toolbar), R.drawable.ic_settings_toolbar, secondary,
                ExpressiveShapes.Sunny, onClickToolbar),
            MainEntry(stringResource(R.string.settings_screen_secondary_layouts), R.drawable.ic_settings_layout, tertiary,
                ExpressiveShapes.Clover, onClickLayouts),
        )
        val style = listOf(
            MainEntry(stringResource(R.string.settings_screen_appearance), R.drawable.ic_settings_appearance, primary,
                ExpressiveShapes.Flower, onClickAppearance, stringResource(R.string.hero_appearance)),
        )
        val system = listOfNotNull(
            MainEntry(stringResource(R.string.settings_screen_advanced), R.drawable.ic_settings_advanced, secondary,
                ExpressiveShapes.Cookie, onClickAdvanced),
            // we don't even show the menu if data gathering phase ended more than 2 weeks ago
            if (JniUtils.sHaveGestureLib && System.currentTimeMillis() < END_DATE_EPOCH_MILLIS + TWO_WEEKS_IN_MILLIS)
                MainEntry(stringResource(R.string.gesture_data_screen), R.drawable.ic_settings_gesture, tertiary,
                    ExpressiveShapes.Soft, onClickDataGathering)
            else null,
            MainEntry(stringResource(R.string.settings_screen_about), R.drawable.ic_settings_about, primary,
                ExpressiveShapes.Sunny, onClickAbout),
        )

        Scaffold(contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)) { innerPadding ->
            Column(
                Modifier.verticalScroll(rememberScrollState()).then(Modifier.padding(innerPadding))
            ) {
                MainHero()
                TryKeyboardField()
                MainGroup(stringResource(R.string.settings_group_typing), typing)
                MainGroup(stringResource(R.string.settings_group_keyboard), keyboard)
                MainGroup(stringResource(R.string.settings_group_style), style)
                MainGroup(stringResource(R.string.settings_group_system), system)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private class MainEntry(
    val name: String,
    @DrawableRes val icon: Int,
    val colors: Pair<Color, Color>,
    val shape: Shape,
    val onClick: () -> Unit,
    val description: String? = null,
)

@Composable
private fun MainGroup(title: String, entries: List<MainEntry>) {
    SettingsGroup(title, entries.size) {
        val entry = entries[it]
        Preference(
            name = entry.name,
            description = entry.description,
            onClick = entry.onClick,
            icon = entry.icon,
            iconContainerColor = entry.colors.first,
            iconContentColor = entry.colors.second,
            iconShape = entry.shape,
        ) { NextScreenIcon() }
    }
}

/** Large card with an illustration, a short introduction and whether the keyboard is ready to use. */
@Composable
private fun MainHero() {
    val ctx = LocalContext.current
    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var isEnabled by remember { mutableStateOf(UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm)) }
    var isCurrent by remember { mutableStateOf(UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm)) }
    LifecycleResumeEffect(Unit) {
        isEnabled = UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm)
        isCurrent = UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm)
        onPauseOrDispose { }
    }
    val appName = stringResource(R.string.english_ime_name)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column {
            IllustrationCanvas(Illustration.KEYBOARD, Modifier.fillMaxWidth().height(180.dp))
            Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)) {
                Text(stringResource(R.string.main_hero_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.main_hero_text),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val ready = isEnabled && isCurrent
                        Surface(
                            shape = CircleShape,
                            color = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            contentColor = if (ready) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
                        ) {
                            Icon(
                                painterResource(if (ready) R.drawable.ic_setup_check else R.drawable.ic_close),
                                null,
                                Modifier.padding(4.dp).size(16.dp)
                            )
                        }
                        Text(
                            stringResource(if (ready) R.string.keyboard_status_ready else R.string.keyboard_status_not_selected, appName),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                        )
                        if (!ready)
                            FilledTonalButton(onClick = {
                                if (isEnabled) imm.showInputMethodPicker()
                                else ctx.startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }) { Text(stringResource(R.string.keyboard_status_setup)) }
                    }
                }
            }
        }
    }
}

/** Text field to quickly try the keyboard after changing settings. */
@Composable
private fun TryKeyboardField() {
    var text by rememberSaveable { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(stringResource(R.string.try_keyboard_hint)) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_edit), null) },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Preview
@Composable
private fun PreviewScreen() {
    initPreview(LocalContext.current)
    Theme(previewDark) {
        Surface {
            MainSettingsScreen({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        }
    }
}
