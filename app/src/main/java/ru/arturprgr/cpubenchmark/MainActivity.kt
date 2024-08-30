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

            buttonStartStopTest.setOnClickListener {
                isWorked = !isWorked
                if (isWorked) {
                    val textView = TextView(this@MainActivity)
                    textView.text =
                        "Во время теста, могут наблюдаться лаги, а в противном случае вылет приложения.\n\nP.S Могут возникать проблемы на прошивках MIUI и HyperOS (неполный список). Если у Вас так, то пробуйте снова и снова.\n\nВремя теста: 0:30"
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Начало теста")
                        .setView(textView)
                        .setPositiveButton("Начать") { _, _ ->
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
                        .create()
                        .show()
                    textView.updateLayoutParams<FrameLayout.LayoutParams> {
                        this.topMargin = 32
                        this.leftMargin = 70
                        this.rightMargin = 70
                    }
                } else {
                    thread = null
                    chronometer.stop()
                    buttonStartStopTest.text = resources.getString(R.string.start_test)
                }
            }
        }
    }
}