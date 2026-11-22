package com.example.someonetoldmeto.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.data.RecRepository
import com.example.someonetoldmeto.databinding.ActivityMainBinding
import com.example.someonetoldmeto.model.Recommendation
import com.google.android.material.divider.MaterialDividerItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val repo by lazy { RecRepository.get(this) }
    private val adapter = RecAdapter(::open)
    private var glide = false

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra(Extras.MESSAGE)?.let {
            showPanel(b.root, it, b.add)
            glide = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.root.padForSystemBars()
        b.list.adapter = adapter
        b.list.addItemDecoration(MaterialDividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            dividerColor = getColor(R.color.quiet)
            isLastItemDecorated = false
        })
        b.add.setOnClickListener { launcher.launch(Intent(this, AddEditActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        val recs = repo.list()
        b.empty.isVisible = recs.isEmpty()
        adapter.submitList(recs) {
            if (glide) b.list.glideToTop()
            glide = false
        }
    }

    private fun open(rec: Recommendation) =
        launcher.launch(Intent(this, DetailActivity::class.java).putExtra(Extras.REC_ID, rec.id))
}
