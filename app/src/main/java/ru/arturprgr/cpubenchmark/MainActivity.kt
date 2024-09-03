package ru.arturprgr.cpubenchmark

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import ru.arturprgr.cpubenchmark.databinding.ActivityMainBinding
import java.text.DecimalFormat

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var thread: Thread? = null
    private var isWorked: Boolean = false

    @SuppressLint("SetTextI18n", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            resetChronometer()

            buttonInfo.setOnClickListener {
                viewAlertDialog(
                    resources.getString(R.string.info),
                    resources.getString(R.string.information),
                    resources.getString(R.string.understand)
                ) {}
            }

            buttonStartStopTest.setOnClickListener {
                isWorked = !isWorked
                if (isWorked) viewAlertDialog(
                    resources.getString(R.string.start_test),
                    resources.getString(R.string.warn),
                    resources.getString(R.string.start)
                ) {
                    val handler = @SuppressLint("HandlerLeak") object : Handler() {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            thread = null
                            isWorked = !isWorked
                            textResult.text = "${msg.data.getString("result")}"
                            chronometer.stop()
                            buttonStartStopTest.text = resources.getString(R.string.start_test)
                            val vibrator =
                                this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        500L,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                vibrator.vibrate(500L)
                            }
                        }
                    }
                    thread = Thread {
                        for (scores in 0..2147483647) {
                            if (chronometer.base <= SystemClock.elapsedRealtime()) {
                                val bundle = Bundle()
                                val msg = handler.obtainMessage()
                                bundle.putString(
                                    "result",
                                    DecimalFormat("###,###.##").format(scores)
                                )
                                msg.data = bundle
                                handler.sendMessage(msg)
                                break
                            }
                        }
                    }
                    textResult.text = "Процесс..."
                    thread!!.start()
                    resetChronometer()
                    chronometer.start()
                    buttonStartStopTest.text = resources.getString(R.string.stop_test)
                }
                else {
                    textResult.text = "0"
                    thread = null
                    resetChronometer()
                    chronometer.stop()
                    buttonStartStopTest.text = resources.getString(R.string.start_test)
                }
            }
        }
    }

    private fun resetChronometer() {
        binding.chronometer.base = SystemClock.elapsedRealtime() + 60000
    }

    private fun viewAlertDialog(
        title: String,
        message: String,
        buttonText: String,
        onClick: () -> Unit,
    ) = binding.apply {
        val textView = TextView(this@MainActivity)
        textView.text = message
        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setView(textView)
            .setPositiveButton(buttonText) { _, _ ->
                onClick()
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