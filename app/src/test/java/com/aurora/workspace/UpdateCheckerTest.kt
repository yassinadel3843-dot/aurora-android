package com.aurora.workspace

import com.aurora.workspace.util.UpdateChecker
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The update dialog is only as trustworthy as the version comparison behind
 * it: a wrong answer either hides a real release or nags about an old one.
 */
class UpdateCheckerTest {

    @Test
    fun sameVersionIsNotNewer() {
        assertEquals(0, UpdateChecker.compareVersions("1.0.0", "1.0.0"))
        assertEquals(0, UpdateChecker.compareVersions("v1.0.0", "1.0.0"))
    }

    @Test
    fun patchAndMinorBumpsAreDetected() {
        assertEquals(1, UpdateChecker.compareVersions("1.0.1", "1.0.0"))
        assertEquals(1, UpdateChecker.compareVersions("1.1.0", "1.0.9"))
        assertEquals(-1, UpdateChecker.compareVersions("1.0.0", "1.0.1"))
    }

    @Test
    fun numbersAreComparedNotText() {
        // "1.10.0" is newer than "1.9.0" although it sorts first as a string.
        assertEquals(1, UpdateChecker.compareVersions("1.10.0", "1.9.0"))
        assertEquals(-1, UpdateChecker.compareVersions("1.9.0", "1.10.0"))
    }

    @Test
    fun shorterVersionsPadWithZero() {
        assertEquals(0, UpdateChecker.compareVersions("2.0", "2.0.0"))
        assertEquals(1, UpdateChecker.compareVersions("2.0.1", "2.0"))
    }

    @Test
    fun preReleaseSuffixIsIgnored() {
        // A "-beta.2" tag still describes 1.0.1, which is newer than 1.0.0.
        assertEquals(1, UpdateChecker.compareVersions("1.0.1-beta.2", "1.0.0"))
        assertEquals(0, UpdateChecker.compareVersions("1.0.0+build7", "1.0.0"))
    }

    // ---------------------------------------------------- release payload

    private fun release(tag: String, apk: Boolean = true) = JSONObject(
        """
        {
          "tag_name": "$tag",
          "html_url": "https://github.com/owner/repo/releases/tag/$tag",
          "body": "Fixed the thing.",
          "assets": [
            {"name": "notes.txt", "browser_download_url": "https://example.test/notes.txt", "size": 10}
            ${if (apk) ",{\"name\": \"aurora-" + tag + ".apk\", \"browser_download_url\": \"https://example.test/a.apk\", \"size\": 1234}" else ""}
          ]
        }
        """.trimIndent()
    )

    @Test
    fun newerReleaseWithApkIsOffered() {
        val update = UpdateChecker.parse(release("v1.1.0"), installed = "1.0.0")!!
        assertEquals("1.1.0", update.version)
        assertEquals("https://example.test/a.apk", update.apkUrl)
        assertEquals(1234L, update.sizeBytes)
        assertEquals("Fixed the thing.", update.notes)
    }

    @Test
    fun sameOrOlderReleaseIsNotOffered() {
        assertNull(UpdateChecker.parse(release("v1.0.0"), installed = "1.0.0"))
        assertNull(UpdateChecker.parse(release("v0.9.9"), installed = "1.0.0"))
    }

    @Test
    fun releaseWithoutApkIsIgnored() {
        // Nothing to install means nothing to announce.
        assertNull(UpdateChecker.parse(release("v1.2.0", apk = false), installed = "1.0.0"))
    }

    @Test
    fun brokenPayloadIsIgnored() {
        assertNull(UpdateChecker.parse(JSONObject("{}"), installed = "1.0.0"))
    }
}
