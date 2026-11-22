package com.example.someonetoldmeto.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.databinding.ActivityPeopleBinding
import com.google.android.material.divider.MaterialDividerItemDecoration

class PeopleActivity : AppCompatActivity() {

    private lateinit var b: ActivityPeopleBinding
    private val repo by lazy { RecRepository.get(this) }
    private val adapter = PeopleAdapter { person ->
        setResult(RESULT_OK, Intent().putExtra(Extras.PERSON_ID, person.id).putExtra(Extras.PERSON_NAME, person.name))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityPeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.padForSystemBars()
        b.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        b.list.adapter = adapter
        b.list.addItemDecoration(MaterialDividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            dividerColor = getColor(R.color.quiet)
            isLastItemDecorated = false
        })
        b.list.smoothWheel()
    }

    override fun onResume() {
        super.onResume()
        val people = repo.peopleScores()
        b.empty.isVisible = people.isEmpty()
        adapter.submitList(people)
    }
}
