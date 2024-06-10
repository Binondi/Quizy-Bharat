package devs.org.quizzybharat.StatusBarColor

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window

class ColorAccordingTheme {

    private val window: Window

    constructor(window: Window) {
        this.window = window
    }

    @SuppressLint("ObsoleteSdkInt")
    fun statusBarColor() {
        if (!isDarkTheme()) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        val typedValue = android.util.TypedValue()
        window.context.theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        val defaultBackgroundColor = typedValue.data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = defaultBackgroundColor
        }
    }

    private fun isDarkTheme(): Boolean {
        return when (window.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
}
