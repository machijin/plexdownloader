package com.plexdownloader.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadDecisionTest {
    @Test
    fun `picks smallest existing version without asking for a quality`() {
        val smallest = DownloadDecision.smallestAvailable(
            listOf(
                MediaVersion("original", 1080, 2_000_000_000, false),
                MediaVersion("optimized", 720, 850_000_000, true),
            )
        )

        assertEquals("optimized", smallest?.id)
    }

    @Test
    fun `only offers make smaller when selected version exceeds 720p`() {
        assertTrue(DownloadDecision.canMakeSmaller(MediaVersion("big", 1080, 1, false)))
        assertFalse(DownloadDecision.canMakeSmaller(MediaVersion("small", 720, 1, true)))
        assertFalse(DownloadDecision.canMakeSmaller(MediaVersion("smallest", 480, 1, false)))
    }
}

