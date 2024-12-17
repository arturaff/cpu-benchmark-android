package ru.arturprgr.cpubenchmark.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ru.arturprgr.cpubenchmark.adapter.ResultsAdapter
import ru.arturprgr.cpubenchmark.databinding.FragmentResultsBinding
import ru.arturprgr.cpubenchmark.data.Singleton
import java.util.Locale

class ResultsFragment : Fragment() {
    private lateinit var binding: FragmentResultsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(inflater, container, false)

        binding.apply {
            listResults.layoutManager = LinearLayoutManager(context)
            listResults.adapter = Singleton.resultsAdapter
            editDevice.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    //not init
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //not init
                }

                override fun afterTextChanged(s: Editable?) {
                    val adapter = ResultsAdapter()
                    listResults.adapter = adapter
                    for (pack in Singleton.resultsAdapter.getList()) if (pack.device.lowercase(
                            Locale.getDefault()
                        ).contains("$s".lowercase(Locale.getDefault()))
                    ) adapter.addResult(pack)
                }
            })
        }

        return binding.root
    }
}