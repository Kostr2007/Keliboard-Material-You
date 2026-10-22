// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import helium314.keyboard.latin.suggestions.SmartCorrections
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SmartCorrectionsTest {
    private val en = Locale.ENGLISH
    private val ru = Locale.forLanguageTag("ru")

    @Test fun `double caps are fixed`() {
        assertEquals("Hello", SmartCorrections.fixDoubleCaps("HEllo", en))
        assertEquals("The", SmartCorrections.fixDoubleCaps("THe", en))
        assertEquals("Привет", SmartCorrections.fixDoubleCaps("ПРивет", ru))
    }

    @Test fun `intentional capitalization is kept`() {
        assertNull(SmartCorrections.fixDoubleCaps("Hello", en))
        assertNull(SmartCorrections.fixDoubleCaps("HELLO", en))
        assertNull(SmartCorrections.fixDoubleCaps("HELlo", en))
        assertNull(SmartCorrections.fixDoubleCaps("IDs", en))
        assertNull(SmartCorrections.fixDoubleCaps("PCs", en))
        assertNull(SmartCorrections.fixDoubleCaps("OK", en))
        assertNull(SmartCorrections.fixDoubleCaps("iPhone", en))
        assertNull(SmartCorrections.fixDoubleCaps("McDonald", en))
        assertNull(SmartCorrections.fixDoubleCaps("HE1lo", en))
    }

    @Test fun `stretched words are collapsed`() {
        assertEquals(listOf("soo", "so"), SmartCorrections.collapseRepeatedLetters("sooooo"))
        assertEquals(listOf("даа", "да"), SmartCorrections.collapseRepeatedLetters("дааааа"))
        assertEquals(listOf("yyeess", "yes"), SmartCorrections.collapseRepeatedLetters("yyyeeesss"))
        // the double letter variant comes first, as runs of 2 are normal spelling ("cool", not "col")
        assertEquals(listOf("cool", "col"), SmartCorrections.collapseRepeatedLetters("cooool"))
    }

    @Test fun `words without long runs are not changed`() {
        assertTrue(SmartCorrections.collapseRepeatedLetters("hello").isEmpty())
        assertTrue(SmartCorrections.collapseRepeatedLetters("bookkeeper").isEmpty())
        assertTrue(SmartCorrections.collapseRepeatedLetters("1000").isEmpty())
        assertTrue(SmartCorrections.collapseRepeatedLetters("ok").isEmpty())
    }
}
