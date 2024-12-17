package ru.arturprgr.cpubenchmark.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import ru.arturprgr.cpubenchmark.R
import ru.arturprgr.cpubenchmark.data.FirebaseHelper
import ru.arturprgr.cpubenchmark.data.Result
import ru.arturprgr.cpubenchmark.data.Singleton
import ru.arturprgr.cpubenchmark.databinding.ActivityMainBinding
import java.text.DecimalFormat
import kotlin.text.StringBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private lateinit var thread: Thread
    private var isWorked: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseHelper("results").getValue { results ->
            var builder: StringBuilder = StringBuilder()
            for (index in 1..results.length - 2) {
                if (results[index] != ',') builder.append(results[index])
                else {
                    val device = StringBuilder()
                    val result = StringBuilder()
                    if (builder[0] == ' ') builder.deleteCharAt(0)
                    for (iDevice in 0..<builder.indexOf('=')) {
                        device.append(builder[iDevice])
                    }
                    for (iResult in builder.indexOf('=') + 1..<builder.length) {
                        result.append(builder[iResult])
                    }
                    Singleton.resultsAdapter.addResult(
                        Result(
                            0,
                            device.toString(),
                            result.toString().toLong()
                        )
                    )
                    builder = StringBuilder()
                }
            }
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.start_color)

        binding.apply {
            textManufacturer.text =
                "${resources.getString(R.string.manufacturer)}: ${Build.MANUFACTURER}"
            textModel.text =
                "${resources.getString(R.string.model)}:${Build.MANUFACTURER} ${Build.BRAND} ${Build.MODEL}"
            textProcessor.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    text =
                        "${resources.getString(R.string.processor)}: ${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}"
                else isVisible = false
            }
            textResult.text =
                getSharedPreferences("sPrefs", Context.MODE_PRIVATE).getString("result", "0")
            chronometer.base = SystemClock.elapsedRealtime() + 60000

            handler = Handler(mainLooper)
//            кусок прошлой версии
//            handler = @SuppressLint("HandlerLeak") object : Handler() {
//                @SuppressLint("SetTextI18n")
//                override fun handleMessage(msg: Message) {
//                    super.handleMessage(msg)
//                    val result = msg.data.getInt("result")
//                    thread.interrupt()
//                    getSharedPreferences("sPrefs", Context.MODE_PRIVATE).edit()
//                        .putInt("result", result).apply()
//                    chronometer.stop()
//                    buttonStartStopTest.isVisible = false
//                    FirebaseHelper("results/${("${Build.MANUFACTURER} " + Build.MODEL).replace("/", "Slash")}").setValue(result)
//                }
//            }

            thread = Thread {
                for (scores in 0..2147483647) {
                    if (chronometer.base <= SystemClock.elapsedRealtime()) {
                        val result = DecimalFormat("###,###.##").format(scores)
                        val device = "${Build.MANUFACTURER} ${Build.BRAND} ${Build.MODEL}"
                        device.replace("/", "Slash")
                        getSharedPreferences("sPrefs", Context.MODE_PRIVATE).edit()
                            .putString("result", result).apply()
                        FirebaseHelper("results/$device").setValue(scores)
                        handler.post {
                            textResult.text = result
                            buttonStartStopTest.isVisible = false
                            buttonStats.isVisible = true
                            buttonInfo.isVisible = true
                            chronometer.stop()
                        }
                        thread.interrupt()
                        break
//                        кусок прошлой версии
//                        val bundle = Bundle()
//                        val msg = handler.obtainMessage()
//                        bundle.putInt("result", scores)
//                        msg.data = bundle
//                        handler.sendMessage(msg)
//                        break
                    }
                }
            }
            thread.priority = Thread.MAX_PRIORITY

            buttonInfo.setOnClickListener {
                viewAlertDialog(
                    resources.getString(R.string.info),
                    resources.getString(R.string.information),
                    resources.getString(R.string.understand),
                    {}, {}
                )
            }

            buttonStats.setOnClickListener {
                fragmentTest.isVisible = !fragmentTest.isVisible
                fragmentResults.isVisible = !fragmentResults.isVisible
            }

            buttonStartStopTest.setOnClickListener {
                if (!isWorked) viewAlertDialog(
                    resources.getString(R.string.start_test),
                    resources.getString(R.string.warn),
                    resources.getString(R.string.start),
                    {
                        isWorked = true
                        thread.start()
                        textResult.text = resources.getString(R.string.process)
                        chronometer.base = SystemClock.elapsedRealtime() + 60000
                        chronometer.start()
                        buttonStartStopTest.text = resources.getString(R.string.stop_test)
                        buttonStats.isVisible = false
                        buttonInfo.isVisible = false
                    }, {}
                )
                else {
                    isWorked = false
                    thread.interrupt()
                    textResult.text = resources.getString(R.string.canceled)
                    chronometer.stop()
                    chronometer.base = SystemClock.elapsedRealtime() + 60000
                    buttonStats.isVisible = true
                    buttonInfo.isVisible = true
                    buttonStartStopTest.isVisible = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isWorked) {
            isWorked = false
            viewAlertDialog(
                resources.getString(R.string.test_stopped),
                resources.getString(R.string.app_pause),
                resources.getString(R.string.understand),
                {}, {}
            )
        }
    }

    override fun onPause() = with(binding) {
        super.onPause()
        if (isWorked) {
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime() + 60000
            buttonStartStopTest.isVisible = false
            buttonStats.isVisible = true
            buttonInfo.isVisible = true
            textResult.text = resources.getString(R.string.canceled)
            if (thread.isAlive) thread.interrupt()
        }
    }

    private fun viewAlertDialog(
        title: String,
        message: String,
        buttonText: String,
        onClick: () -> Unit,
        onCancel: () -> Unit,
    ) = binding.apply {
        val textView = TextView(this@MainActivity)
        textView.text = message
        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setView(textView)
            .setPositiveButton(buttonText) { _, _ ->
                onClick()
            }
            .setOnCancelListener {
                onCancel()
            }
            .create()
            .show()

        textView.updateLayoutParams<FrameLayout.LayoutParams> {
            this.topMargin = 32
            this.leftMargin = 70
            this.rightMargin = 70
        }
    }
}