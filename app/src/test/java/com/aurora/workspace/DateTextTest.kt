package com.aurora.workspace

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class DateTextTest {

    @Test
    fun testDateParsing() {
        val pattern = "MMM d"
        val date = SimpleDateFormat(pattern, Locale.ENGLISH).parse("Nov 28")
        val formatted = SimpleDateFormat("d MMM", Locale.ENGLISH).format(date!!)
        assertEquals("28 Nov", formatted)
    }

    @Test
    fun testExplicitYearDetection() {
        val withYear = Regex("\\d{4}").containsMatchIn("Nov 28, 2026")
        val withoutYear = Regex("\\d{4}").containsMatchIn("Nov 28")
        assertEquals(true, withYear)
        assertEquals(false, withoutYear)
    }
}
