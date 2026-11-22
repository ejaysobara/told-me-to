package com.example.someonetoldmeto.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.InputDevice
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.databinding.ViewPanelBinding
import com.example.someonetoldmeto.model.RecStatus
import com.example.someonetoldmeto.model.RecType
import com.google.android.material.snackbar.Snackbar
import kotlin.math.min
import kotlin.math.pow

object Extras {
    const val REC_ID = "rec_id"
    const val MESSAGE = "message"
    const val FROM_PICK = "from_pick"
    const val TYPE = "type"
    const val PERSON_ID = "person_id"
    const val PERSON_NAME = "person_name"
}

val lenisEase = Interpolator { t -> min(1f, 1.001f - 2f.pow(-10f * t)) }

fun RecyclerView.glideToTop() = smoothScrollBy(0, -computeVerticalScrollOffset(), lenisEase, 600)

fun RecyclerView.smoothWheel() = setOnGenericMotionListener { _, e ->
    if (e.action != MotionEvent.ACTION_SCROLL || !e.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) return@setOnGenericMotionListener false
    val dy = -e.getAxisValue(MotionEvent.AXIS_VSCROLL) * ViewConfiguration.get(context).scaledVerticalScrollFactor
    smoothScrollBy(0, dy.toInt(), lenisEase, 350)
    true
}

fun View.padForSystemBars() = ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val bars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
    )
    v.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
    insets
}

fun View.popIn(endAction: () -> Unit = {}) {
    alpha = 0f
    scaleX = 0.9f
    scaleY = 0.9f
    translationY = 12 * resources.displayMetrics.density
    animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(200)
        .setInterpolator(OvershootInterpolator(1.2f)).withEndAction(endAction).start()
}

fun showPanel(root: View, text: CharSequence, anchor: View? = null, error: Boolean = false) {
    val bar = Snackbar.make(root, text, 2500).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
    anchor?.let { bar.anchorView = it }
    val layout = bar.view as ViewGroup
    layout.setBackgroundColor(Color.TRANSPARENT)
    layout.elevation = 0f
    val gap = (8 * root.resources.displayMetrics.density).toInt()
    layout.setPadding(gap, gap, gap, gap)
    layout.clipToPadding = false
    layout.getChildAt(0).visibility = View.GONE
    val panel = ViewPanelBinding.inflate(LayoutInflater.from(root.context), layout, false)
    panel.text.text = text
    panel.icon.isVisible = !error
    layout.addView(panel.root)
    bar.show()
}

fun Activity.finishWith(message: String) {
    setResult(Activity.RESULT_OK, Intent().putExtra(Extras.MESSAGE, message))
    finish()
}

fun RecType.label() = when (this) {
    RecType.SHOW -> R.string.type_show
    RecType.SONG -> R.string.type_song
    RecType.PLACE -> R.string.type_place
    RecType.BOOK -> R.string.type_book
    RecType.OTHER -> R.string.type_other
}

fun RecStatus.label() = when (this) {
    RecStatus.UNTRIED -> R.string.status_untried
    RecStatus.LIKED -> R.string.status_liked
    RecStatus.DISLIKED -> R.string.status_disliked
}
