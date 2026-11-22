package com.example.someonetoldmeto.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
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
}

val lenisEase = Interpolator { t -> min(1f, 1.001f - 2f.pow(-10f * t)) }

fun RecyclerView.glideToTop() = smoothScrollBy(0, -computeVerticalScrollOffset(), lenisEase, 600)

fun View.padForSystemBars() = ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val bars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
    )
    v.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
    insets
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
