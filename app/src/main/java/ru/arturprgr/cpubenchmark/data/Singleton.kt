package ru.arturprgr.cpubenchmark.data

import ru.arturprgr.cpubenchmark.adapter.ResultsAdapter

class Singleton {
    companion object {
        val resultsAdapter = ResultsAdapter()
    }
}