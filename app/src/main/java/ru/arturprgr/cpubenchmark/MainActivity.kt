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
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ru.arturprgr.cpubenchmark.databinding.ActivityMainBinding
import java.text.DecimalFormat

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    private lateinit var handler: Handler
    private lateinit var thread: Thread
    private lateinit var view: View
    private var isWorked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        vibrator = this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        view = View.inflate(this@MainActivity, R.layout.layout_send_result, null)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            handler = @SuppressLint("HandlerLeak") object : Handler() {
                @SuppressLint("SetTextI18n")
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    val result = "${msg.data.getString("result")}"
                    vibrate(500L)
                    thread.interrupt()
                    isWorked = false
                    getSharedPreferences("sPrefs", Context.MODE_PRIVATE).edit()
                        .putString("result", result).apply()
                    textResult.text = result
                    chronometer.stop()
                    buttonStartStopTest.isVisible = false

                    view.findViewById<TextView>(R.id.text_result).text = "Количество очков: $result"
                    view.findViewById<TextInputEditText>(R.id.edit_model).setText("${Build.MANUFACTURER} ${Build.MODEL}")
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(resources.getString(R.string.feedback))
                        .setView(view)
                        .setPositiveButton(resources.getString(R.string.send)) { _, _ ->
                            val model =
                                view.findViewById<TextInputEditText>(R.id.edit_model).text!!.trim()
                            Firebase.database.getReference("results/$model").setValue(result)
                            Toast.makeText(
                                this@MainActivity,
                                resources.getString(R.string.thanks),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                        .create()
                        .show()

                    view.updateLayoutParams<FrameLayout.LayoutParams> {
                        this.topMargin = 16
                        this.leftMargin = 70
                        this.rightMargin = 70
                        this.bottomMargin = 16
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

            textResult.text =
                getSharedPreferences("sPrefs", Context.MODE_PRIVATE).getString("result", "0")
            resetChronometer()

            buttonInfo.setOnClickListener {
                viewAlertDialog(
                    resources.getString(R.string.info),
                    resources.getString(R.string.information),
                    resources.getString(R.string.understand),
                    {}, {}
                )
            }

            buttonStartStopTest.setOnClickListener {
                if (!isWorked) viewAlertDialog(
                    resources.getString(R.string.start_test),
                    resources.getString(R.string.warn),
                    resources.getString(R.string.start),
                    {
                        thread.start()
                        isWorked = true
                        textResult.text = "Процесс..."
                        resetChronometer()
                        chronometer.start()
                        buttonStartStopTest.text = resources.getString(R.string.stop_test)
                    }, {}
                )
                else {
                    thread.interrupt()
                    isWorked = false
                    textResult.text = "0"
                    resetChronometer()
                    chronometer.stop()
                    buttonStartStopTest.isVisible = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isWorked) {
            binding.buttonStartStopTest.isVisible = false
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
            resetChronometer()
            textResult.text = "0"
            buttonStartStopTest.text = resources.getString(R.string.start_test)
            Log.d("Attempt", thread.isAlive.toString())
            if (thread.isAlive) thread.interrupt()
        }
    }

    private fun resetChronometer() {
        binding.chronometer.base = SystemClock.elapsedRealtime() + 60000
    }

    private fun vibrate(millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(
            VibrationEffect.createOneShot(
                millis,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
        else vibrator.vibrate(millis)
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