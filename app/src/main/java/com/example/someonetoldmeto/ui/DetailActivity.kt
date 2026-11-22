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
import com.example.someonetoldmeto.model.Recommendation

class DetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailBinding
    private lateinit var popOut: PopOut
    private lateinit var rec: Recommendation
    private val repo by lazy { RecRepository.get(this) }

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra(Extras.MESSAGE)?.let { showPanel(b.root, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.padForSystemBars()
        popOut = PopOut(this, b.content, b.popout)

        b.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        rec = repo.get(intent.getLongExtra(Extras.REC_ID, -1)) ?: return finishWith(getString(R.string.msg_gone))
        val added = DateUtils.formatDateTime(this, rec.createdAt, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH)
        b.title.text = rec.title
        b.meta.text = getString(R.string.detail_meta, getString(rec.type.label()), rec.person, added)
        b.status.setText(rec.status.label())
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

    private fun finishWith(message: String) {
        setResult(RESULT_OK, Intent().putExtra(Extras.MESSAGE, message))
        finish()
    }
}
