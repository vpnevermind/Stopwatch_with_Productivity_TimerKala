package org.hyperskill.stopwatch

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import android.app.AlertDialog
import android.app.Notification
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var textViewTimer: TextView
    lateinit var startButton: Button
    lateinit var resetButton: Button
    lateinit var settingsButton: Button
    lateinit var progressBar: ProgressBar
    val handler = Handler()
    var seconds = 0
    var setTime = Integer.MAX_VALUE

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("DefaultLocale", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewTimer = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        progressBar = findViewById(R.id.progressBar)
        settingsButton = findViewById(R.id.settingsButton)

        checkPermission()

        startButton.setOnClickListener {
            if (seconds == 0) {
                handler.postDelayed(startTimer, 1000)
            }
            progressBar.isVisible = true
            settingsButton.isEnabled = false
        }

        resetButton.setOnClickListener {
            handler.removeCallbacks(startTimer)
            seconds = 0
            textViewTimer.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
            progressBar.isGone = true
            settingsButton.isEnabled = true
            setColorOfTimer()
        }

        settingsButton.setOnClickListener {
            val contentView = layoutInflater.inflate(R.layout.dialog_main, null)
            AlertDialog.Builder(this)
                .setTitle(R.string.alert_dialog_title)
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    setTime = editText.text.toString().toInt()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        }

    }


    private val startTimer = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            seconds++
            changeProgressBarColor()
            textViewTimer.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
            setColorOfTimer()
            if (seconds == setTime) {
                notification()
            }
            handler.postDelayed(this, 1000)
        }
    }

    fun changeProgressBarColor() {
        progressBar.indeterminateTintList = ColorStateList.valueOf(Random.nextInt())
    }

    fun setColorOfTimer() {
        if (seconds > setTime) {
            textViewTimer.setTextColor(Color.RED)
        } else {
            textViewTimer.setTextColor(Color.GRAY)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermission() {
        val applicationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if(granted) {
                    return@registerForActivityResult
                }
            }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                return
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                AlertDialog.Builder(this)
                    .setTitle("Grant access?")
                    .setMessage("Please grant access to notifications")
                    .setPositiveButton("GRANT") { _, _ ->
                        applicationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
            }
            else -> applicationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    fun notification() {
        val notificationManager : NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "org.hyperskill"
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "TimerIsUp", importance).apply { description = "Notification about timer completion" }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or  PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("Time exceeded")
            .setPriority(importance)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        notificationBuilder.flags = notificationBuilder.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(393939, notificationBuilder)
    }


}

