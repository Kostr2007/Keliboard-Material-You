// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.content.SharedPreferences
import androidx.core.content.edit
import helium314.keyboard.latin.settings.Settings
import java.util.Locale

/**
 * Small typing helpers on top of the dictionary based suggestions, similar to what big commercial
 * keyboards do: fixing accidental double capitals and understanding stretched words.
 */
object SmartCorrections {

    /**
     * Returns the word with only the first letter capitalized if it looks like the shift key was released
     * too late ("HEllo" -> "Hello", "THe" -> "The"), null otherwise.
     * Requires at least 3 letters, the first two upper case and all others lower case,
     * so abbreviations like "IDs" or all-caps words are kept.
     */
    @JvmStatic
    fun fixDoubleCaps(word: String, locale: Locale): String? {
        val codePoints = word.codePoints().toArray()
        if (codePoints.size < 3) return null
        if (!Character.isUpperCase(codePoints[0]) || !Character.isUpperCase(codePoints[1])) return null
        for (i in 2 until codePoints.size) {
            if (!Character.isLowerCase(codePoints[i])) return null
        }
        // "IDs", "PCs", "URLs": plural abbreviations are intentional
        if (codePoints.size <= 4 && codePoints.last() == 's'.code && codePoints.count { Character.isUpperCase(it) } == codePoints.size - 1)
            return null
        val firstCharLength = Character.charCount(codePoints[0])
        return word.substring(0, firstCharLength) + word.substring(firstCharLength).lowercase(locale)
    }

    /**
     * For words containing runs of 3 or more identical letters ("sooooo", "дааааа"), returns
     * the words with all such runs reduced to a double and to a single letter, e.g. ["soo", "so"].
     * The double letter variant comes first, so "cooool" ends up as "cool" and not "col".
     * Returns an empty list if there is no such run.
     */
    @JvmStatic
    fun collapseRepeatedLetters(word: String): List<String> {
        val codePoints = word.codePoints().toArray()
        if (codePoints.size < 3) return emptyList()
        val single = StringBuilder()
        val double = StringBuilder()
        var hasLongRun = false
        var i = 0
        while (i < codePoints.size) {
            val cp = codePoints[i]
            var runEnd = i + 1
            while (runEnd < codePoints.size && codePoints[runEnd] == cp) runEnd++
            val runLength = runEnd - i
            if (runLength >= 3 && Character.isLetter(cp)) {
                hasLongRun = true
                single.appendCodePoint(cp)
                double.appendCodePoint(cp).appendCodePoint(cp)
            } else {
                repeat(runLength) {
                    single.appendCodePoint(cp)
                    double.appendCodePoint(cp)
                }
            }
            i = runEnd
        }
        if (!hasLongRun) return emptyList()
        return listOf(double.toString(), single.toString()).distinct()
    }
}

/**
 * Words for which the user undid an autocorrection (backspace right after the correction).
 * Such words are not autocorrected again, similar to how other keyboards learn from rejected corrections.
 * Stored in lower case, so "Teh" and "teh" are treated the same.
 */
object RejectedAutocorrections {
    private const val MAX_WORDS = 1000
    private val words = LinkedHashSet<String>()

    /** (re)load from preferences, called whenever settings are loaded */
    @JvmStatic
    @Synchronized
    fun load(prefs: SharedPreferences) {
        words.clear()
        words.addAll(prefs.getStringSet(Settings.PREF_REJECTED_AUTOCORRECT_WORDS, null).orEmpty())
    }

    @JvmStatic
    @Synchronized
    fun contains(word: String): Boolean = words.isNotEmpty() && word.lowercase(Locale.ROOT) in words

    @JvmStatic
    @Synchronized
    fun add(prefs: SharedPreferences, word: String) {
        val lower = word.trim().lowercase(Locale.ROOT)
        if (lower.isEmpty() || lower.contains(' ')) return
        words.remove(lower) // move to the end, so the oldest entries are removed first
        words.add(lower)
        while (words.size > MAX_WORDS) words.remove(words.first())
        val copy = HashSet(words)
        prefs.edit { putStringSet(Settings.PREF_REJECTED_AUTOCORRECT_WORDS, copy) }
    }

    @JvmStatic
    @Synchronized
    fun clear(prefs: SharedPreferences) {
        words.clear()
        prefs.edit { remove(Settings.PREF_REJECTED_AUTOCORRECT_WORDS) }
    }

    @JvmStatic
    @Synchronized
    fun count(): Int = words.size
}
