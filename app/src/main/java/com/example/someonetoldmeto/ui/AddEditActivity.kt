package com.example.someonetoldmeto.ui

import android.content.Intent
import android.database.SQLException
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.databinding.ActivityAddEditBinding
import com.example.someonetoldmeto.model.Problem
import com.example.someonetoldmeto.model.RecType
import com.example.someonetoldmeto.model.Rules
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout

class AddEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddEditBinding
    private lateinit var popOut: PopOut
    private val repo by lazy { RecRepository.get(this) }
    private val types by lazy {
        mapOf(b.typeShow to RecType.SHOW, b.typeSong to RecType.SONG, b.typePlace to RecType.PLACE, b.typeBook to RecType.BOOK, b.typeOther to RecType.OTHER)
    }
    private var editId: Long? = null
    private var fromShare = false
    private var initial = ""
    private var saved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.padForSystemBars()

        editId = intent.getLongExtra(Extras.REC_ID, -1).takeIf { it > 0 }
        fromShare = intent.action == Intent.ACTION_SEND
        b.toolbar.setTitle(if (editId == null) R.string.new_pick else R.string.edit_pick)
        b.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        b.save.setOnClickListener { save() }
        b.person.setOnEditorActionListener { _, action, _ -> (action == EditorInfo.IME_ACTION_DONE).also { if (it) save() } }

        listOf(b.titleLayout, b.personLayout, b.noteLayout, b.linkLayout).forEach { layout ->
            layout.editText?.doAfterTextChanged { layout.error = null }
        }
        b.person.doAfterTextChanged { showSuggestions(it.toString()) }

        onBackPressedDispatcher.addCallback(this) {
            if (snapshot() == initial) finish()
            else popOut.show(getString(R.string.confirm_discard), null, getString(R.string.keep_editing), getString(R.string.discard)) { finish() }
        }
        popOut = PopOut(this, b.content, b.popout)

        if (savedInstanceState == null) fill() else initial = savedInstanceState.getString(KEY_INITIAL).orEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_INITIAL, initial)
    }

    private fun fill() {
        val id = editId
        if (id != null) {
            val rec = repo.get(id) ?: return finishWith(getString(R.string.msg_gone))
            b.title.setText(rec.title)
            b.person.setText(rec.person)
            b.note.setText(rec.note)
            b.link.setText(rec.link)
            types.entries.first { it.value == rec.type }.key.isChecked = true
        } else {
            if (fromShare) b.link.setText(intent.getStringExtra(Intent.EXTRA_TEXT)?.trim())
            b.title.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        initial = snapshot()
    }

    private fun snapshot() = listOf(b.title.text, b.person.text, b.note.text, b.link.text, selectedType()).joinToString("\u0000")

    private fun selectedType() = types.entries.firstOrNull { it.key.isChecked }?.value ?: RecType.OTHER

    private fun showSuggestions(typed: String) {
        val name = Rules.normalizeName(typed)
        val names = if (name.isEmpty()) emptyList() else repo.personNames(name).filterNot { it.equals(name, ignoreCase = true) }
        b.suggestions.removeAllViews()
        names.forEach { n ->
            b.suggestions.addView(Chip(this).apply {
                text = n
                isCheckable = false
                contentDescription = getString(R.string.use_name, n)
                setOnClickListener {
                    b.person.setText(n)
                    b.person.setSelection(n.length)
                }
            })
        }
    }

    private fun save() {
        if (saved) return
        val title = b.title.text.toString().trim()
        val person = Rules.normalizeName(b.person.text.toString())
        val note = b.note.text.toString().trim()
        val link = b.link.text.toString().trim()
        val ok = listOf(
            b.titleLayout.report(Rules.check(title, Rules.TITLE_MAX), R.string.err_title_empty, R.string.err_title_long),
            b.personLayout.report(Rules.check(person, Rules.PERSON_MAX), R.string.err_person_empty, R.string.err_person_long),
            b.noteLayout.report(Rules.check(note, Rules.TEXT_MAX, required = false), 0, R.string.err_note_long),
            b.linkLayout.report(Rules.check(link, Rules.TEXT_MAX, required = false), 0, R.string.err_link_long),
        )
        val firstBad = ok.indexOf(false)
        if (firstBad >= 0) {
            listOf(b.title, b.person, b.note, b.link)[firstBad].requestFocus()
            return
        }
        val id = try {
            repo.save(editId, title, person, selectedType(), note.ifEmpty { null }, link.ifEmpty { null })
        } catch (e: SQLException) {
            showPanel(b.root, getString(R.string.err_save), error = true)
            return
        }
        saved = true
        val savedName = repo.get(id)?.person ?: person
        val message = if (editId == null) getString(R.string.msg_saved, savedName) else getString(R.string.msg_changes_saved)
        if (fromShare) {
            initial = snapshot()
            WindowCompat.getInsetsController(window, b.root).hide(WindowInsetsCompat.Type.ime())
            showPanel(b.root, message)
            b.root.postDelayed(::finish, 1200)
        } else {
            finishWith(message)
        }
    }

    private fun TextInputLayout.report(problem: Problem?, @StringRes empty: Int, @StringRes tooLong: Int): Boolean {
        error = when (problem) {
            Problem.EMPTY -> getString(empty)
            Problem.TOO_LONG -> getString(tooLong)
            null -> null
        }
        return problem == null
    }

    private fun finishWith(message: String) {
        setResult(RESULT_OK, Intent().putExtra(Extras.MESSAGE, message))
        finish()
    }

    private companion object {
        const val KEY_INITIAL = "initial"
    }
}
