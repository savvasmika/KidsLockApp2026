package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ChildTheme
import com.example.security.CryptoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies KIDLOCK app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KIDLOCK", appName)
    }

    @Test
    fun `crypto utils hashes and verifies parent pin correctly`() {
        val pin = "1234"
        val hash = CryptoUtils.hashPin(pin)
        assertNotNull(hash)
        assertTrue(CryptoUtils.verifyPin(pin, hash))
        assertFalse(CryptoUtils.verifyPin("9999", hash))
    }

    @Test
    fun `crypto utils generates 6 digit confirmation code`() {
        val code = CryptoUtils.generateSecure6DigitCode()
        assertEquals(6, code.length)
        assertTrue(code.toIntOrNull() != null)
    }

    @Test
    fun `child theme loads themes and falls back safely`() {
        val space = ChildTheme.fromId("space")
        assertEquals(ChildTheme.SPACE, space)
        val gaming = ChildTheme.fromId("gaming")
        assertEquals(ChildTheme.GAMING, gaming)
        val fallback = ChildTheme.fromId("unknown_theme")
        assertEquals(ChildTheme.SPACE, fallback)
    }
}
