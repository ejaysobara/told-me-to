package com.example.someonetoldmeto.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.databinding.ActivityMainBinding
import com.example.someonetoldmeto.model.RecStatus
import com.example.someonetoldmeto.model.RecType
import com.example.someonetoldmeto.model.Recommendation
import com.example.someonetoldmeto.model.Rules
import com.google.android.material.divider.MaterialDividerItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val repo by lazy { RecRepository.get(this) }
    private val adapter = RecAdapter(::open)
    private val types by lazy {
        mapOf(b.typeShow.id to RecType.SHOW, b.typeSong.id to RecType.SONG, b.typePlace.id to RecType.PLACE, b.typeBook.id to RecType.BOOK, b.typeOther.id to RecType.OTHER)
    }
    private val statuses by lazy {
        mapOf(b.statusUntried.id to RecStatus.UNTRIED, b.statusLiked.id to RecStatus.LIKED, b.statusDisliked.id to RecStatus.DISLIKED)
    }
    private var personId: Long? = null
    private var personName: String? = null
    private var glide = false

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra(Extras.MESSAGE)?.let {
            showPanel(b.root, it, b.add)
            glide = true
        }
    }

    private val peopleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.let { setPerson(it.getLongExtra(Extras.PERSON_ID, -1), it.getStringExtra(Extras.PERSON_NAME)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.padForSystemBars()
        savedInstanceState?.let {
            personId = it.getLong(Extras.PERSON_ID).takeIf { id -> id > 0 }
            personName = it.getString(Extras.PERSON_NAME)
        }

        b.list.adapter = adapter
        b.list.addItemDecoration(MaterialDividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            dividerColor = getColor(R.color.quiet)
            isLastItemDecorated = false
        })
        b.list.smoothWheel()
        b.toolbar.setOnMenuItemClickListener {
            peopleLauncher.launch(Intent(this, PeopleActivity::class.java))
            true
        }
        b.add.setOnClickListener { launcher.launch(Intent(this, AddEditActivity::class.java)) }
        b.pick.setOnClickListener { pick() }
        b.search.doAfterTextChanged { reload() }
        b.search.setOnEditorActionListener { _, _, _ ->
            doneTyping()
            true
        }
        b.types.setOnCheckedStateChangeListener { _, _ -> doneTyping(); reload() }
        b.statuses.setOnCheckedStateChangeListener { _, _ -> doneTyping(); reload() }
        b.personChip.closeIconContentDescription = getString(R.string.clear_person)
        b.personChip.setOnCloseIconClickListener { setPerson(null, null) }
        b.clearFilters.setOnClickListener { clearFilters() }
        showPerson()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        personId?.let { outState.putLong(Extras.PERSON_ID, it) }
        outState.putString(Extras.PERSON_NAME, personName)
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun type() = types[b.types.checkedChipId]

    private fun status() = statuses[b.statuses.checkedChipId]

    private fun filtered() = !b.search.text.isNullOrBlank() || type() != null || status() != null || personId != null

    private fun reload() {
        val recs = repo.list(b.search.text.toString(), type(), status(), personId)
        val filtered = filtered()
        b.empty.isVisible = recs.isEmpty()
        b.emptyTitle.setText(if (filtered) R.string.no_match_title else R.string.empty_title)
        b.emptyBody.isVisible = !filtered
        b.clearFilters.isVisible = filtered
        val anything = recs.isNotEmpty() || filtered
        b.filters.isVisible = anything
        b.pick.isVisible = anything
        adapter.submitList(recs) {
            if (glide) b.list.glideToTop()
            glide = false
        }
    }

    private fun doneTyping() {
        if (!b.search.hasFocus()) return
        b.search.clearFocus()
        WindowCompat.getInsetsController(window, b.search).hide(WindowInsetsCompat.Type.ime())
    }

    private fun setPerson(id: Long?, name: String?) {
        personId = id?.takeIf { it > 0 }
        personName = name
        showPerson()
        reload()
    }

    private fun showPerson() {
        b.personChip.isVisible = personId != null
        b.personChip.text = personName?.let { getString(R.string.person_filter, it) }
    }

    private fun clearFilters() {
        b.search.text = null
        b.types.check(b.typeAll.id)
        b.statuses.check(b.statusAll.id)
        setPerson(null, null)
    }

    private fun pick() {
        val type = type()
        val id = Rules.pick(repo.untriedIds(type)) ?: return showPanel(b.root, getString(R.string.nothing_untried), b.add, error = true)
        launcher.launch(
            Intent(this, DetailActivity::class.java)
                .putExtra(Extras.REC_ID, id)
                .putExtra(Extras.FROM_PICK, true)
                .putExtra(Extras.TYPE, type?.db)
        )
    }

    private fun open(rec: Recommendation) =
        launcher.launch(Intent(this, DetailActivity::class.java).putExtra(Extras.REC_ID, rec.id))
}
