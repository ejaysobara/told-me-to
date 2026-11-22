package com.example.someonetoldmeto.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.databinding.ActivityDetailBinding
import com.example.someonetoldmeto.model.RecStatus
import com.example.someonetoldmeto.model.RecType
import com.example.someonetoldmeto.model.Recommendation
import com.example.someonetoldmeto.model.Rules

class DetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailBinding
    private lateinit var popOut: PopOut
    private lateinit var rec: Recommendation
    private val repo by lazy { RecRepository.get(this) }
    private val fromPick by lazy { intent.getBooleanExtra(Extras.FROM_PICK, false) }
    private val pickType by lazy { intent.getStringExtra(Extras.TYPE)?.let(RecType::of) }
    private var recId = -1L

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra(Extras.MESSAGE)?.let { showPanel(b.root, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.content.padForSystemBars()
        popOut = PopOut(this, b.content, b.popout)
        recId = savedInstanceState?.getLong(Extras.REC_ID) ?: intent.getLongExtra(Extras.REC_ID, -1)

        b.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        b.pickedLabel.isVisible = fromPick
        b.pickAgain.isVisible = fromPick
        b.pickAgain.setOnClickListener { pickAgain() }
        b.liked.setOnClickListener { rate(RecStatus.LIKED) }
        b.disliked.setOnClickListener { rate(RecStatus.DISLIKED) }
        b.edit.setOnClickListener {
            editLauncher.launch(Intent(this, AddEditActivity::class.java).putExtra(Extras.REC_ID, rec.id))
        }
        b.delete.setOnClickListener {
            popOut.show(
                getString(R.string.confirm_delete, rec.title), getString(R.string.confirm_delete_body),
                getString(R.string.cancel), getString(R.string.delete)
            ) {
                repo.delete(rec.id)
                finishWith(getString(R.string.msg_deleted, rec.title))
            }
        }
        if (fromPick && savedInstanceState == null) popInTitle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(Extras.REC_ID, recId)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        rec = repo.get(recId) ?: return finishWith(getString(R.string.msg_gone))
        val added = DateUtils.formatDateTime(this, rec.createdAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH)
        b.title.text = rec.title
        b.meta.text = getString(R.string.detail_meta, getString(rec.type.label()), rec.person, added)
        b.status.setText(rec.status.label())
        b.liked.setIconResource(if (rec.status == RecStatus.LIKED) R.drawable.ic_check else 0)
        b.disliked.setIconResource(if (rec.status == RecStatus.DISLIKED) R.drawable.ic_check else 0)
        b.note.isVisible = rec.note != null
        b.note.text = rec.note?.let { getString(R.string.note_quoted, it) }
        b.link.isVisible = rec.link != null
        b.link.text = rec.link
    }

    private fun rate(status: RecStatus) {
        repo.rate(rec.id, status)
        load()
        val score = repo.score(rec.personId) ?: return
        val res = if (status == RecStatus.LIKED) R.string.msg_rated_liked else R.string.msg_rated_disliked
        showPanel(b.root, getString(res, rec.person, score.liked, score.tried))
    }

    private fun pickAgain() {
        val next = Rules.pick(repo.untriedIds(pickType), recId)?.takeIf { it != recId }
            ?: return showPanel(b.root, getString(R.string.no_other_untried), error = true)
        recId = next
        load()
        popInTitle()
    }

    private fun popInTitle() = listOf(b.pickedLabel, b.title, b.meta).forEach { it.popIn() }
}
