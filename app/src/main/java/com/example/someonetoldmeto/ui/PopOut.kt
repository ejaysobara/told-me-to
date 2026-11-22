package com.example.someonetoldmeto.ui

import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.example.someonetoldmeto.databinding.ViewPopoutBinding

class PopOut(activity: ComponentActivity, private val content: View, private val b: ViewPopoutBinding) {

    private val rise = 12 * activity.resources.displayMetrics.density
    private val back = activity.onBackPressedDispatcher.addCallback(activity, false) { hide() }

    init {
        val padBottom = b.panel.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(b.panel) { v, insets ->
            v.updatePadding(bottom = padBottom + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
        b.scrim.setOnClickListener { hide() }
        b.cancel.setOnClickListener { hide() }
    }

    fun show(title: String, message: String?, cancel: String, confirm: String, onConfirm: () -> Unit) {
        b.popTitle.text = title
        b.popMessage.text = message
        b.popMessage.isVisible = message != null
        b.cancel.text = cancel
        b.confirm.text = confirm
        b.confirm.setOnClickListener {
            hide()
            onConfirm()
        }
        content.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        ViewCompat.setAccessibilityPaneTitle(b.panel, title)
        b.root.isVisible = true
        b.scrim.alpha = 0f
        b.scrim.animate().alpha(1f).setDuration(200).start()
        b.panel.apply {
            alpha = 0f
            scaleX = 0.9f
            scaleY = 0.9f
            translationY = rise
            animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(200)
                .setInterpolator(OvershootInterpolator(1.2f)).start()
        }
        back.isEnabled = true
    }

    fun hide() {
        back.isEnabled = false
        content.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        b.panel.animate().alpha(0f).translationY(rise).setDuration(150).start()
        b.scrim.animate().alpha(0f).setDuration(150).withEndAction { b.root.isVisible = false }.start()
    }
}
