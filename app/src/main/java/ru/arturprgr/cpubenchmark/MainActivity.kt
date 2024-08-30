package ru.arturprgr.cpubenchmark

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import ru.arturprgr.cpubenchmark.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var thread: Thread? = Thread()
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
            chronometer.base = SystemClock.elapsedRealtime() + 30000

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
                    resources.getString(R.string.information_2),
                    resources.getString(R.string.start)
                ) {
                    chronometer.base = SystemClock.elapsedRealtime() + 30000
                    val handler = @SuppressLint("HandlerLeak") object : Handler() {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            if (!(msg.data.getBoolean("isWorked"))) {
                                thread = null
                                isWorked = !isWorked
                                chronometer.stop()
                                buttonStartStopTest.text =
                                    resources.getString(R.string.start_test)
                            } else textResult.text = "${msg.data.getInt("result")}"
                        }
                    }
                    thread = Thread {
                        for (scores in 1..2147483647) {
                            try {
                                if (chronometer.base <= SystemClock.elapsedRealtime()) {
                                    val bundle = Bundle()
                                    val message = handler.obtainMessage()
                                    bundle.putInt("result", scores)
                                    bundle.putBoolean("isWorked", false)
                                    message.data = bundle
                                    handler.sendMessage(message)
                                    break
                                } else {
                                    val bundle = Bundle()
                                    val message = handler.obtainMessage()
                                    bundle.putInt("result", scores)
                                    bundle.putBoolean("isWorked", true)
                                    message.data = bundle
                                    handler.sendMessage(message)
                                }
                            } catch (_: NullPointerException) {
                                break
                            }
                        }
                    }
                    thread!!.start()
                    chronometer.base = SystemClock.elapsedRealtime() + 30000
                    chronometer.start()
                    buttonStartStopTest.text = resources.getString(R.string.stop_test)
                }
                else {
                    thread = null
                    chronometer.stop()
                    buttonStartStopTest.text = resources.getString(R.string.start_test)
                }
            }
        }
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