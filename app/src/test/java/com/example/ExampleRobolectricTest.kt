package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesRepository
import com.example.ipc.HephaistosIpcManager
import com.example.model.TerminalEntryType
import com.example.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hephaistos", appName)
  }

  @Test
  fun `test preferences repository theme change`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PreferencesRepository(context)
    repo.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, repo.themeMode.value)

    repo.toggleManualPackage("com.test.tool", true)
    assertTrue(repo.manualPackages.value.contains("com.test.tool"))
  }

  @Test
  fun `test ipc manager logging and response`() {
    HephaistosIpcManager.clearLog()
    HephaistosIpcManager.log("Test log entry", TerminalEntryType.INFO)
    val entries = HephaistosIpcManager.terminalEntries.value
    assertTrue(entries.isNotEmpty())

    val dummyResponse = """{"id":"1","source":"com.hephaistos.test","status":"success","code":0}"""
    HephaistosIpcManager.onResponseReceived(dummyResponse)
    assertNotNull(HephaistosIpcManager.terminalEntries.value.lastOrNull())
  }
}
