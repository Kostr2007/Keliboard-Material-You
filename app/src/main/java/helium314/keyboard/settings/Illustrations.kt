// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.isDark
import helium314.keyboard.latin.utils.previewDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Illustrations and shapes in the style of Material 3 Expressive.
// Everything is drawn with the current color scheme, so it follows the dynamic (wallpaper) colors.

/** Hero illustrations shown at the top of the settings screens. */
enum class Illustration {
    KEYBOARD, SUGGESTIONS, LANGUAGES, GESTURE, DICTIONARY, TOOLBAR, SOUND, CLIPBOARD, ADVANCED, ABOUT, LAYOUTS, THEME, KEYS
}

/**
 * Path of a circle with a wavy outline, like the "cookie", "sunny" and "flower" shapes of
 * Material 3 Expressive. [lobes] = 0 gives a circle. The path fits into a circle of [radius].
 */
fun cookiePath(center: Offset, radius: Float, lobes: Int, depth: Float = 0.08f, rotationDegrees: Float = 0f): Path {
    val path = Path()
    val steps = 180
    val base = radius / (1f + depth)
    val rotation = rotationDegrees / 180f * PI.toFloat()
    for (i in 0..steps) {
        val t = i.toFloat() / steps * 2f * PI.toFloat()
        val r = if (lobes == 0) radius else base * (1f + depth * cos(lobes * t))
        val x = center.x + r * cos(t + rotation)
        val y = center.y + r * sin(t + rotation)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/** Shape version of [cookiePath], e.g. for icon containers. */
class CookieShape(private val lobes: Int, private val depth: Float = 0.08f, private val rotationDegrees: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(cookiePath(Offset(size.width / 2f, size.height / 2f), min(size.width, size.height) / 2f, lobes, depth, rotationDegrees))
}

/** A few shapes from the expressive shape set, used to give icons some variety. */
object ExpressiveShapes {
    val Sunny = CookieShape(8, 0.06f)
    val Cookie = CookieShape(9, 0.09f)
    val Flower = CookieShape(8, 0.16f)
    val Clover = CookieShape(4, 0.18f, 45f)
    val Soft = CookieShape(6, 0.07f)
    val Pentagon = CookieShape(5, 0.10f, -90f)
    val all = listOf(Cookie, Sunny, Clover, Soft, Flower, Pentagon)
}

/**
 * Card with a large illustration and an optional short explanation, shown at the top of a screen.
 */
@Composable
fun HeroIllustration(
    illustration: Illustration,
    text: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (MaterialTheme.colorScheme.isDark) 0.55f else 0.75f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column {
            IllustrationCanvas(illustration, Modifier.fillMaxWidth().height(172.dp))
            if (text != null)
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                )
        }
    }
}

/** Only the drawing, without background. */
@Composable
fun IllustrationCanvas(illustration: Illustration, modifier: Modifier = Modifier) {
    val c = IllustrationColors.current()
    val textMeasurer = rememberTextMeasurer()
    val transition = rememberInfiniteTransition(label = "illustration")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 60000, easing = LinearEasing)),
        label = "spin"
    )
    Canvas(modifier) {
        val d = Drawer(this, c, textMeasurer, spin)
        when (illustration) {
            Illustration.KEYBOARD -> d.keyboardScene()
            Illustration.SUGGESTIONS -> d.suggestions()
            Illustration.LANGUAGES -> d.languages()
            Illustration.GESTURE -> d.gesture()
            Illustration.DICTIONARY -> d.dictionary()
            Illustration.TOOLBAR -> d.toolbar()
            Illustration.SOUND -> d.sound()
            Illustration.CLIPBOARD -> d.clipboard()
            Illustration.ADVANCED -> d.advanced()
            Illustration.ABOUT -> d.about()
            Illustration.LAYOUTS -> d.layouts()
            Illustration.THEME -> d.theme()
            Illustration.KEYS -> d.keys()
        }
    }
}

private class IllustrationColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val outline: Color,
    val error: Color,
) {
    companion object {
        @Composable
        fun current(): IllustrationColors {
            val s = MaterialTheme.colorScheme
            return IllustrationColors(
                primary = s.primary,
                onPrimary = s.onPrimary,
                primaryContainer = s.primaryContainer,
                secondary = s.secondary,
                secondaryContainer = s.secondaryContainer,
                tertiary = s.tertiary,
                tertiaryContainer = s.tertiaryContainer,
                surface = s.surfaceContainerLowest,
                surfaceVariant = s.surfaceContainerHighest,
                onSurface = s.onSurfaceVariant,
                outline = s.outlineVariant,
                error = s.error,
            )
        }
    }
}

/** Drawing code for the illustrations. Sizes are relative to the canvas height ([u] = 1/160 of the height). */
private class Drawer(val s: DrawScope, val c: IllustrationColors, val tm: TextMeasurer, val spin: Float) {
    val w = s.size.width
    val h = s.size.height
    val u = h / 160f
    val cx = w / 2f
    val cy = h / 2f

    // ---------- primitives ----------

    fun roundRect(color: Color, left: Float, top: Float, width: Float, height: Float, radius: Float) =
        s.drawRoundRect(color, Offset(left, top), Size(width, height), CornerRadius(radius))

    fun cookie(color: Color, center: Offset, radius: Float, lobes: Int, depth: Float = 0.08f, rotation: Float = 0f) =
        s.drawPath(cookiePath(center, radius, lobes, depth, rotation), color)

    fun sparkle(color: Color, center: Offset, r: Float) {
        val p = Path()
        p.moveTo(center.x, center.y - r)
        p.quadraticTo(center.x, center.y, center.x + r, center.y)
        p.quadraticTo(center.x, center.y, center.x, center.y + r)
        p.quadraticTo(center.x, center.y, center.x - r, center.y)
        p.quadraticTo(center.x, center.y, center.x, center.y - r)
        p.close()
        s.drawPath(p, color)
    }

    fun text(text: String, color: Color, center: Offset, size: Float, bold: Boolean = true) {
        val layout = tm.measure(text, TextStyle(color = color, fontSize = (size / s.density / s.fontScale).sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
        s.drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
    }

    /** a rounded bar, used as placeholder for text */
    fun bar(color: Color, left: Float, centerY: Float, width: Float, thickness: Float = 6 * u) =
        roundRect(color, left, centerY - thickness / 2, width, thickness, thickness / 2)

    fun backgroundDecor(rotation: Float = spin) {
        cookie(c.tertiaryContainer, Offset(w * 0.14f, h * 0.22f), 46 * u, 9, 0.09f, rotation)
        cookie(c.secondaryContainer, Offset(w * 0.88f, h * 0.80f), 38 * u, 4, 0.18f, -rotation)
        s.drawCircle(c.primary.copy(alpha = 0.25f), 10 * u, Offset(w * 0.80f, h * 0.17f))
        sparkle(c.tertiary, Offset(w * 0.24f, h * 0.78f), 9 * u)
    }

    /** simple keyboard drawing inside the given rect */
    fun keyboard(left: Float, top: Float, width: Float, height: Float, highlightKey: Int = -1, accent: Color = c.primary) {
        roundRect(c.surface, left, top, width, height, 16 * u)
        val pad = width * 0.035f
        val gap = width * 0.016f
        val keyW = (width - 2 * pad - 9 * gap) / 10
        val rowH = (height - 2 * pad - 3 * gap) / 4
        val radius = rowH * 0.32f
        var y = top + pad
        // row 1
        for (i in 0 until 10) {
            val color = if (i == highlightKey) accent else c.surfaceVariant
            roundRect(color, left + pad + i * (keyW + gap), y, keyW, rowH, radius)
        }
        y += rowH + gap
        // row 2
        for (i in 0 until 9)
            roundRect(c.surfaceVariant, left + pad + keyW / 2 + i * (keyW + gap), y, keyW, rowH, radius)
        y += rowH + gap
        // row 3 with shift and delete
        val wideW = keyW * 1.5f + gap / 2
        roundRect(c.secondaryContainer, left + pad, y, wideW, rowH, radius)
        for (i in 0 until 7)
            roundRect(c.surfaceVariant, left + pad + wideW + gap + i * (keyW + gap), y, keyW, rowH, radius)
        roundRect(c.secondaryContainer, left + width - pad - wideW, y, wideW, rowH, radius)
        y += rowH + gap
        // bottom row
        roundRect(c.secondaryContainer, left + pad, y, wideW, rowH, radius)
        roundRect(c.surfaceVariant, left + pad + wideW + gap, y, keyW, rowH, radius)
        val spaceLeft = left + pad + wideW + keyW + 2 * gap
        val spaceW = width - 2 * pad - 2 * wideW - 2 * keyW - 4 * gap
        roundRect(c.surfaceVariant, spaceLeft, y, spaceW, rowH, radius)
        roundRect(c.surfaceVariant, spaceLeft + spaceW + gap, y, keyW, rowH, radius)
        roundRect(accent, left + width - pad - wideW, y, wideW, rowH, rowH / 2)
    }

    // ---------- scenes ----------

    fun keyboardScene() {
        backgroundDecor()
        val kbW = min(w * 0.62f, 300 * u)
        val kbH = kbW * 0.52f
        val left = cx - kbW / 2
        val top = cy - kbH / 2 + 14 * u
        keyboard(left, top, kbW, kbH, highlightKey = 3)
        // key preview popup above the highlighted key
        val pad = kbW * 0.035f
        val gap = kbW * 0.016f
        val keyW = (kbW - 2 * pad - 9 * gap) / 10
        val keyCenterX = left + pad + 3 * (keyW + gap) + keyW / 2
        val popupW = keyW * 1.9f
        roundRect(c.primary, keyCenterX - popupW / 2, top - popupW * 1.05f, popupW, popupW * 1.15f, popupW * 0.4f)
        text("K", c.onPrimary, Offset(keyCenterX, top - popupW * 0.47f), popupW * 0.6f)
        sparkle(c.tertiary, Offset(keyCenterX + popupW, top - popupW * 0.9f), 8 * u)
    }

    fun suggestions() {
        backgroundDecor()
        // text field with a misspelled word
        val fieldW = min(w * 0.72f, 320 * u)
        val fieldH = 40 * u
        val fieldLeft = cx - fieldW / 2
        val fieldTop = 22 * u
        roundRect(c.surface, fieldLeft, fieldTop, fieldW, fieldH, fieldH / 2)
        val textY = fieldTop + fieldH / 2
        bar(c.onSurface.copy(alpha = 0.55f), fieldLeft + 18 * u, textY, fieldW * 0.22f)
        bar(c.onSurface.copy(alpha = 0.55f), fieldLeft + 26 * u + fieldW * 0.22f, textY, fieldW * 0.16f)
        val wrongLeft = fieldLeft + 34 * u + fieldW * 0.38f
        val wrongW = fieldW * 0.26f
        bar(c.onSurface.copy(alpha = 0.8f), wrongLeft, textY, wrongW)
        // squiggly underline
        val squiggle = Path()
        val sy = textY + 9 * u
        squiggle.moveTo(wrongLeft, sy)
        var x = wrongLeft
        var up = true
        val step = 4 * u
        while (x < wrongLeft + wrongW) {
            squiggle.quadraticTo(x + step / 2, sy + if (up) -3 * u else 3 * u, x + step, sy)
            x += step
            up = !up
        }
        s.drawPath(squiggle, c.error, style = Stroke(width = 2 * u, cap = StrokeCap.Round))
        // cursor
        roundRect(c.primary, wrongLeft + wrongW + 6 * u, textY - 10 * u, 2.5f * u, 20 * u, 2 * u)

        // suggestion strip
        val stripTop = fieldTop + fieldH + 26 * u
        val chipH = 36 * u
        val chipW = fieldW * 0.3f
        val chipGap = fieldW * 0.05f
        val stripLeft = cx - (3 * chipW + 2 * chipGap) / 2
        for (i in 0 until 3) {
            val l = stripLeft + i * (chipW + chipGap)
            val isMain = i == 1
            roundRect(if (isMain) c.primary else c.surface, l, stripTop, chipW, chipH, chipH / 2)
            bar(if (isMain) c.onPrimary else c.onSurface.copy(alpha = 0.5f), l + chipW * 0.2f, stripTop + chipH / 2, chipW * 0.6f)
        }
        sparkle(c.tertiary, Offset(cx + chipW * 0.55f, stripTop - 4 * u), 10 * u)
        sparkle(c.tertiary.copy(alpha = 0.6f), Offset(cx + chipW * 0.75f, stripTop + 8 * u), 5 * u)
        // a few keys hinting at the keyboard below
        val keysTop = stripTop + chipH + 14 * u
        val keyW = 26 * u
        for (i in -3..3)
            roundRect(c.surface.copy(alpha = 0.7f), cx + i * (keyW + 6 * u) - keyW / 2, keysTop, keyW, 30 * u, 9 * u)
    }

    fun languages() {
        backgroundDecor()
        // globe
        val globeCenter = Offset(cx - min(w * 0.18f, 90 * u), cy + 6 * u)
        val r = 50 * u
        s.drawCircle(c.primary, r, globeCenter)
        val line = c.onPrimary.copy(alpha = 0.55f)
        val stroke = Stroke(width = 2.5f * u)
        s.drawOval(line, Offset(globeCenter.x - r * 0.45f, globeCenter.y - r), Size(r * 0.9f, 2 * r), style = stroke)
        s.drawLine(line, Offset(globeCenter.x, globeCenter.y - r), Offset(globeCenter.x, globeCenter.y + r), 2.5f * u)
        s.drawLine(line, Offset(globeCenter.x - r, globeCenter.y), Offset(globeCenter.x + r, globeCenter.y), 2.5f * u)
        for (f in listOf(-0.55f, 0.55f)) {
            val yy = globeCenter.y + f * r
            val half = r * 0.835f
            s.drawLine(line, Offset(globeCenter.x - half, yy), Offset(globeCenter.x + half, yy), 2.5f * u)
        }
        // speech bubbles
        fun bubble(center: Offset, bw: Float, bh: Float, color: Color, textColor: Color, label: String, tailLeft: Boolean) {
            roundRect(color, center.x - bw / 2, center.y - bh / 2, bw, bh, bh / 2)
            val tail = Path()
            val tx = if (tailLeft) center.x - bw * 0.3f else center.x + bw * 0.3f
            tail.moveTo(tx - 7 * u, center.y + bh / 2 - 2 * u)
            tail.lineTo(tx + if (tailLeft) -12 * u else 12 * u, center.y + bh / 2 + 10 * u)
            tail.lineTo(tx + 7 * u, center.y + bh / 2 - 2 * u)
            tail.close()
            s.drawPath(tail, color)
            text(label, textColor, center, bh * 0.42f)
        }
        val bx = cx + min(w * 0.14f, 70 * u)
        bubble(Offset(bx, cy - 40 * u), 96 * u, 36 * u, c.surface, c.onSurface, "Привет", true)
        bubble(Offset(bx + 30 * u, cy + 6 * u), 84 * u, 34 * u, c.tertiaryContainer, c.tertiary, "Hello", true)
        bubble(Offset(bx - 4 * u, cy + 50 * u), 72 * u, 32 * u, c.secondaryContainer, c.secondary, "안녕", true)
    }

    fun gesture() {
        backgroundDecor()
        val cols = 7
        val rows = 3
        val keyW = min(w * 0.08f, 34 * u)
        val gap = 8 * u
        val gridW = cols * keyW + (cols - 1) * gap
        val gridH = rows * keyW + (rows - 1) * gap
        val left = cx - gridW / 2
        val top = cy - gridH / 2
        roundRect(c.surface, left - 12 * u, top - 12 * u, gridW + 24 * u, gridH + 24 * u, 20 * u)
        fun keyCenter(col: Int, row: Int) = Offset(left + col * (keyW + gap) + keyW / 2, top + row * (keyW + gap) + keyW / 2)
        val pathKeys = listOf(0 to 1, 2 to 0, 3 to 2, 5 to 0, 6 to 2)
        for (row in 0 until rows) for (col in 0 until cols) {
            val k = keyCenter(col, row)
            val hit = pathKeys.contains(col to row)
            roundRect(if (hit) c.primaryContainer else c.surfaceVariant, k.x - keyW / 2, k.y - keyW / 2, keyW, keyW, keyW * 0.3f)
        }
        val points = pathKeys.map { keyCenter(it.first, it.second) }
        val trail = Path()
        trail.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]
            val mid = Offset((p0.x + p1.x) / 2, (p0.y + p1.y) / 2)
            trail.quadraticTo(p0.x + (mid.x - p0.x) * 1.4f, p0.y, mid.x, mid.y)
            trail.quadraticTo(p1.x - (p1.x - mid.x) * 1.4f, p1.y, p1.x, p1.y)
        }
        s.drawPath(
            trail,
            Brush.linearGradient(listOf(c.primary.copy(alpha = 0.25f), c.primary), points.first(), points.last()),
            style = Stroke(width = 7 * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        s.drawCircle(c.primary, 9 * u, points.last())
        s.drawCircle(c.onPrimary, 3.5f * u, points.last())
        sparkle(c.tertiary, Offset(points.last().x + 16 * u, points.last().y - 30 * u), 9 * u)
    }

    fun dictionary() {
        backgroundDecor()
        val pageW = min(w * 0.24f, 110 * u)
        val pageH = 100 * u
        val top = cy - pageH / 2 + 4 * u
        fun page(left: Boolean) {
            val p = Path()
            val outer = if (left) cx - pageW else cx + pageW
            p.moveTo(cx, top + 8 * u)
            p.quadraticTo((cx + outer) / 2, top - 6 * u, outer, top + 4 * u)
            p.lineTo(outer, top + pageH)
            p.quadraticTo((cx + outer) / 2, top + pageH - 12 * u, cx, top + pageH + 4 * u)
            p.close()
            s.drawPath(p, c.surface)
        }
        // cover behind the pages
        roundRect(c.primary, cx - pageW - 8 * u, top + 8 * u, 2 * pageW + 16 * u, pageH + 6 * u, 10 * u)
        page(true)
        page(false)
        s.drawLine(c.outline, Offset(cx, top + 8 * u), Offset(cx, top + pageH + 4 * u), 2 * u)
        text("Aa", c.primary, Offset(cx - pageW / 2, top + pageH * 0.42f), 34 * u)
        for (i in 0 until 3)
            bar(c.onSurface.copy(alpha = 0.35f), cx - pageW * 0.8f, top + pageH * 0.68f + i * 10 * u, pageW * (0.6f - 0.12f * i), 4 * u)
        for (i in 0 until 6)
            bar(c.onSurface.copy(alpha = 0.35f), cx + pageW * 0.18f, top + 22 * u + i * 12 * u, pageW * (0.65f - 0.05f * (i % 3)), 4 * u)
        // bookmark
        val bm = Path()
        val bl = cx + pageW * 0.7f
        bm.moveTo(bl, top - 2 * u)
        bm.lineTo(bl + 14 * u, top - 2 * u)
        bm.lineTo(bl + 14 * u, top + 34 * u)
        bm.lineTo(bl + 7 * u, top + 27 * u)
        bm.lineTo(bl, top + 34 * u)
        bm.close()
        s.drawPath(bm, c.tertiary)
    }

    fun toolbar() {
        backgroundDecor()
        val n = 5
        val iconR = 17 * u
        val gap = 14 * u
        val stripW = n * 2 * iconR + (n - 1) * gap + 28 * u
        val stripH = 2 * iconR + 20 * u
        val top = cy - stripH / 2 - 14 * u
        roundRect(c.surface, cx - stripW / 2, top, stripW, stripH, stripH / 2)
        for (i in 0 until n) {
            val center = Offset(cx - stripW / 2 + 14 * u + iconR + i * (2 * iconR + gap), top + stripH / 2)
            val selected = i == 2
            if (selected) cookie(c.primary, center, iconR + 2 * u, 8, 0.07f, spin)
            else s.drawCircle(c.secondaryContainer, iconR, center)
            val fg = if (selected) c.onPrimary else c.secondary
            when (i) {
                0 -> s.drawCircle(fg, iconR * 0.35f, center, style = Stroke(2.5f * u))
                1 -> roundRect(fg, center.x - iconR * 0.4f, center.y - iconR * 0.4f, iconR * 0.8f, iconR * 0.8f, 3 * u)
                2 -> sparkle(fg, center, iconR * 0.55f)
                3 -> {
                    bar(fg, center.x - iconR * 0.45f, center.y - 5 * u, iconR * 0.9f, 3 * u)
                    bar(fg, center.x - iconR * 0.45f, center.y + 5 * u, iconR * 0.6f, 3 * u)
                }
                else -> s.drawCircle(fg, iconR * 0.25f, center)
            }
        }
        val keyW = 26 * u
        val keysTop = top + stripH + 14 * u
        for (i in -4..4)
            roundRect(c.surface.copy(alpha = 0.7f), cx + i * (keyW + 6 * u) - keyW / 2, keysTop, keyW, 30 * u, 9 * u)
    }

    fun sound() {
        backgroundDecor()
        val keySize = 64 * u
        roundRect(c.surface, cx - keySize / 2, cy - keySize / 2, keySize, keySize, 20 * u)
        text("A", c.primary, Offset(cx, cy), 30 * u)
        // sound waves to the right
        for (i in 1..3) {
            val r = keySize / 2 + i * 16 * u
            s.drawArc(
                c.tertiary.copy(alpha = 1f - i * 0.22f), -40f, 80f, false,
                Offset(cx - r, cy - r), Size(2 * r, 2 * r),
                style = Stroke(width = 5 * u, cap = StrokeCap.Round)
            )
        }
        // vibration wave to the left
        val wave = Path()
        val startX = cx - keySize / 2 - 110 * u
        wave.moveTo(startX, cy)
        val segments = 8
        val segW = 90 * u / segments
        for (i in 0 until segments) {
            val amp = (if (i % 2 == 0) -1 else 1) * (6 + i * 2.2f) * u
            wave.quadraticTo(startX + i * segW + segW / 2, cy + amp * 2, startX + (i + 1) * segW, cy)
        }
        s.drawPath(wave, c.primary, style = Stroke(width = 5 * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    fun clipboard() {
        backgroundDecor()
        val boardW = 96 * u
        val boardH = 120 * u
        val left = cx - boardW - 6 * u
        val top = cy - boardH / 2 + 6 * u
        roundRect(c.secondaryContainer, left, top, boardW, boardH, 16 * u)
        roundRect(c.surface, left + 10 * u, top + 16 * u, boardW - 20 * u, boardH - 26 * u, 8 * u)
        roundRect(c.primary, left + boardW / 2 - 22 * u, top - 8 * u, 44 * u, 20 * u, 10 * u)
        for (i in 0 until 5)
            bar(c.onSurface.copy(alpha = 0.35f), left + 22 * u, top + 38 * u + i * 14 * u, (boardW - 44 * u) * (1f - 0.15f * (i % 3)), 5 * u)
        // floating clips
        val cardW = 104 * u
        val cardH = 34 * u
        for (i in 0 until 3) {
            val cl = cx + 16 * u + i * 6 * u
            val ct = cy - 52 * u + i * (cardH + 8 * u)
            roundRect(if (i == 0) c.primaryContainer else c.surface, cl, ct, cardW, cardH, 12 * u)
            bar(c.onSurface.copy(alpha = 0.45f), cl + 14 * u, ct + cardH / 2, cardW * 0.55f, 5 * u)
            if (i == 0) s.drawCircle(c.primary, 7 * u, Offset(cl + cardW - 16 * u, ct + cardH / 2))
        }
    }

    fun advanced() {
        backgroundDecor()
        val trackW = min(w * 0.42f, 190 * u)
        val left = cx - trackW / 2 - 30 * u
        val values = listOf(0.7f, 0.35f, 0.55f)
        for (i in values.indices) {
            val y = cy - 36 * u + i * 36 * u
            roundRect(c.surface, left, y - 8 * u, trackW, 16 * u, 8 * u)
            roundRect(c.primary, left, y - 8 * u, trackW * values[i], 16 * u, 8 * u)
            roundRect(c.primary, left + trackW * values[i] + 4 * u, y - 16 * u, 6 * u, 32 * u, 3 * u)
        }
        val gearCenter = Offset(left + trackW + 62 * u, cy)
        cookie(c.tertiary, gearCenter, 40 * u, 8, 0.16f, spin * 2)
        s.drawCircle(c.tertiaryContainer, 16 * u, gearCenter)
    }

    fun about() {
        cookie(c.tertiaryContainer, Offset(w * 0.18f, h * 0.70f), 40 * u, 5, 0.10f, -spin)
        cookie(c.secondaryContainer, Offset(w * 0.84f, h * 0.30f), 34 * u, 4, 0.18f, spin)
        val center = Offset(cx, cy)
        cookie(c.primary, center, 64 * u, 12, 0.07f, spin)
        // small keyboard glyph
        val gw = 64 * u
        val keyW = gw / 5 - 3 * u
        for (row in 0 until 3) {
            val count = if (row == 2) 1 else 5
            for (i in 0 until count) {
                val kw = if (row == 2) gw * 0.6f else keyW
                val kl = if (row == 2) cx - kw / 2 else cx - gw / 2 + i * (keyW + 3 * u) + 1.5f * u
                roundRect(c.onPrimary, kl, cy - 22 * u + row * 16 * u, kw, 11 * u, 4 * u)
            }
        }
        sparkle(c.tertiary, Offset(cx + 78 * u, cy - 46 * u), 12 * u)
        sparkle(c.tertiary.copy(alpha = 0.6f), Offset(cx - 84 * u, cy - 40 * u), 7 * u)
        s.drawCircle(c.primary.copy(alpha = 0.3f), 6 * u, Offset(cx - 70 * u, cy + 52 * u))
    }

    fun layouts() {
        backgroundDecor()
        val kbW = min(w * 0.5f, 240 * u)
        val kbH = kbW * 0.48f
        val left = cx - kbW / 2
        val top = cy - kbH / 2 + 10 * u
        roundRect(c.tertiaryContainer, left + 28 * u, top - 34 * u, kbW, kbH, 16 * u)
        roundRect(c.secondaryContainer, left + 14 * u, top - 17 * u, kbW, kbH, 16 * u)
        keyboard(left, top, kbW, kbH, highlightKey = 0, accent = c.tertiary)
        text("?123", c.primary, Offset(left + kbW + 34 * u, top - 22 * u), 18 * u)
        text("#@&", c.tertiary, Offset(left - 34 * u, top + kbH * 0.3f), 16 * u)
    }

    fun theme() {
        // "wallpaper" with an expressive shape, colors are extracted from it
        val wallW = 84 * u
        val wallH = 128 * u
        val left = cx - wallW - 40 * u
        val top = cy - wallH / 2
        roundRect(c.surface, left - 6 * u, top - 6 * u, wallW + 12 * u, wallH + 12 * u, 22 * u)
        s.drawRoundRect(
            Brush.verticalGradient(listOf(c.tertiaryContainer, c.primaryContainer), top, top + wallH),
            Offset(left, top), Size(wallW, wallH), CornerRadius(18 * u)
        )
        cookie(c.primary, Offset(left + wallW / 2, top + wallH * 0.55f), 28 * u, 9, 0.09f, spin)
        sparkle(c.tertiary, Offset(left + wallW * 0.75f, top + wallH * 0.2f), 8 * u)
        // arrow
        val ax = left + wallW + 18 * u
        s.drawLine(c.onSurface.copy(alpha = 0.5f), Offset(ax, cy), Offset(ax + 22 * u, cy), 4 * u, StrokeCap.Round)
        s.drawLine(c.onSurface.copy(alpha = 0.5f), Offset(ax + 22 * u, cy), Offset(ax + 14 * u, cy - 8 * u), 4 * u, StrokeCap.Round)
        s.drawLine(c.onSurface.copy(alpha = 0.5f), Offset(ax + 22 * u, cy), Offset(ax + 14 * u, cy + 8 * u), 4 * u, StrokeCap.Round)
        // swatches
        val swatches = listOf(c.primary, c.secondary, c.tertiary, c.primaryContainer)
        val sx = ax + 60 * u
        for (i in swatches.indices) {
            val center = Offset(sx + (i % 2) * 48 * u, cy - 24 * u + (i / 2) * 48 * u)
            s.drawCircle(c.surface, 22 * u, center)
            s.drawCircle(swatches[i], 18 * u, center)
        }
    }

    fun keys() {
        backgroundDecor()
        val keyW = 50 * u
        val keyH = 58 * u
        val keyTop = cy + 2 * u
        // neighbouring keys
        for (i in -2..2) {
            if (i == 0) continue
            roundRect(c.surface.copy(alpha = 0.8f), cx + i * (keyW + 10 * u) - keyW / 2, keyTop, keyW, keyH, 16 * u)
            text(listOf("q", "w", "", "r", "t")[i + 2], c.onSurface.copy(alpha = 0.6f), Offset(cx + i * (keyW + 10 * u), keyTop + keyH / 2), 20 * u, false)
        }
        roundRect(c.surface, cx - keyW / 2, keyTop, keyW, keyH, 16 * u)
        text("e", c.onSurface, Offset(cx, keyTop + keyH / 2), 24 * u)
        text("3", c.onSurface.copy(alpha = 0.5f), Offset(cx + keyW / 2 - 9 * u, keyTop + 10 * u), 10 * u, false)
        // long press popup with alternatives
        val alternatives = listOf("é", "è", "ê", "ë", "ē")
        val cellW = 34 * u
        val popupW = alternatives.size * cellW + 16 * u
        val popupH = 46 * u
        val popupTop = keyTop - popupH - 12 * u
        roundRect(c.primary, cx - popupW / 2, popupTop, popupW, popupH, popupH / 2)
        for (i in alternatives.indices) {
            val center = Offset(cx - popupW / 2 + 8 * u + cellW * i + cellW / 2, popupTop + popupH / 2)
            if (i == 1) s.drawCircle(c.onPrimary.copy(alpha = 0.25f), cellW * 0.48f, center)
            text(alternatives[i], c.onPrimary, center, 20 * u)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme(previewDark) {
        Surface {
            Column {
                HeroIllustration(Illustration.SUGGESTIONS, "Smart suggestions and autocorrect")
                HeroIllustration(Illustration.LANGUAGES, null)
            }
        }
    }
}
