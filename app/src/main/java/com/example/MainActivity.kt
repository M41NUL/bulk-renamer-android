/*
 * File: MainActivity.kt
 * Purpose: Main activity handling edge-to-edge configuration, dynamic locale wrapping, and ViewModel binding
 * Author: CODEX-M41NUL
 * Project: Bulk Renamer
 * Date: 2026-08-26
 */

package com.example

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.LanguageChoice
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import java.util.Locale

class LocalizedContext(
    base: Context,
    private val localizedContext: Context
) : ContextWrapper(base), ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry
        get() = (baseContext as? ActivityResultRegistryOwner)?.activityResultRegistry
            ?: throw IllegalStateException("Base context is not an ActivityResultRegistryOwner")

    override fun getResources(): Resources = localizedContext.resources
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()

            val locale = remember(userSettings.languageChoice) {
                val code = if (userSettings.languageChoice == LanguageChoice.BANGLA) "bn" else "en"
                val loc = Locale(code)
                Locale.setDefault(loc)
                loc
            }

            val currentContext = LocalContext.current
            val localizedConfiguration = remember(currentContext, locale) {
                Configuration(currentContext.resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }

            val localizedContext = remember(currentContext, locale, localizedConfiguration) {
                val configCtx = currentContext.createConfigurationContext(localizedConfiguration)
                LocalizedContext(currentContext, configCtx)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                MainScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

