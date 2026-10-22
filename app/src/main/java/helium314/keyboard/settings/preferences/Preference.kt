// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.preferences

import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import helium314.keyboard.latin.utils.isDark
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.settings.IconOrImage
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark

// partially taken from StreetComplete / SCEE

/** Section header above a group of settings. */
@Composable
fun PreferenceCategory(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge
    )
}

/** Color of the cards that contain the settings, slightly different from the screen background. */
@Composable
fun settingsCardColor(): Color =
    if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surfaceBright

/**
 * Shape of an item inside a group of settings: the group as a whole has large rounded corners,
 * while items inside are only slightly rounded and separated by a small gap (M3 Expressive lists).
 */
fun groupItemShape(index: Int, count: Int): Shape {
    val outer = 24.dp
    val inner = 4.dp
    return RoundedCornerShape(
        topStart = if (index == 0) outer else inner,
        topEnd = if (index == 0) outer else inner,
        bottomStart = if (index == count - 1) outer else inner,
        bottomEnd = if (index == count - 1) outer else inner,
    )
}

/** A single item of a settings group, see [groupItemShape]. */
@Composable
fun SettingsGroupItem(index: Int, count: Int, content: @Composable () -> Unit) {
    Surface(
        shape = groupItemShape(index, count),
        color = settingsCardColor(),
        modifier = Modifier.fillMaxWidth().padding(top = if (index == 0) 0.dp else 2.dp)
    ) {
        content()
    }
}

/** A titled group of settings, each item is drawn on its own segment of a rounded card. */
@Composable
fun SettingsGroup(
    title: String?,
    itemCount: Int,
    modifier: Modifier = Modifier,
    item: @Composable (Int) -> Unit,
) {
    if (itemCount == 0) return
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp).animateContentSize()) {
        if (title != null)
            PreferenceCategory(title, Modifier.padding(start = 12.dp))
        else
            Spacer(Modifier.height(12.dp))
        for (i in 0 until itemCount) {
            SettingsGroupItem(i, itemCount) { item(i) }
        }
    }
}

/**
 * Colorful container behind setting icons, the shape and colors can be varied to make
 * lists of screens easier to scan.
 */
@Composable
fun IconContainer(
    @DrawableRes icon: Int,
    name: String?,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    shape: Shape = CircleShape,
) {
    // images like the app icon have their own shape and colors
    if (ContextCompat.getDrawable(LocalContext.current, icon) !is VectorDrawable) {
        IconOrImage(icon, name, 40)
        return
    }
    Box(
        modifier = Modifier.size(40.dp).clip(shape).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            IconOrImage(icon, name, 24)
        }
    }
}

@Composable
fun Preference(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    @DrawableRes icon: Int? = null,
    iconContainerColor: Color? = null,
    iconContentColor: Color? = null,
    iconShape: Shape = CircleShape,
    value: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = 56.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null)
            IconContainer(
                icon,
                name,
                containerColor = iconContainerColor ?: MaterialTheme.colorScheme.secondaryContainer,
                contentColor = iconContentColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                shape = iconShape
            )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal))
            if (description != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (value != null) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    textAlign = TextAlign.End,
                    hyphens = Hyphens.Auto
                ),
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) { value() }
            }
        }
    }
}

@Preview
@Composable
private fun PreferencePreview() {
    Theme(previewDark) {
        Surface {
            Column {
                PreferenceCategory("Preference Category")
                Preference(
                    name = "Preference",
                    onClick = {},
                )
                Preference(
                    name = "Preference with icon",
                    onClick = {},
                    icon = R.drawable.ic_settings_about
                )
                SliderPreference(
                    name = "SliderPreference",
                    key = "",
                    default = 1,
                    description = { it.toString() },
                    range = -5f..5f
                )
                Preference(
                    name = "Preference with icon and description",
                    description = "some text",
                    onClick = {},
                    icon = R.drawable.ic_settings_about
                )
                Preference(
                    name = "Preference with switch",
                    onClick = {}
                ) {
                    Switch(checked = true, onCheckedChange = {})
                }
                SwitchPreference(
                    name = "SwitchPreference",
                    key = "none",
                    default = true
                )
                Preference(
                    name = "Preference",
                    onClick = {},
                    description = "A long description which may actually be several lines long, so it should wrap."
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_left), null)
                }
                Preference(
                    name = "Long preference name that wraps",
                    onClick = {},
                ) {
                    Text("Long preference value")
                }
                Preference(
                    name = "Long preference name 2",
                    onClick = {},
                    description = "hello I am description"
                ) {
                    Text("Long preference value")
                }
            }
        }
    }
}
