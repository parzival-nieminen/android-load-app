package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    //private lateinit var activityMainBinding: Actima

    private var downloadID: Long = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private val _progress = MutableLiveData<Float>()
    private val RESET_PROGRESS: Float = 0.0f

    init {
        _progress.value = 0f
    }

    val progress: MutableLiveData<Float>
        get() = _progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            launch {
                download()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                _progress.value = RESET_PROGRESS
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
                println("is completed")
            }
        }
    }

    private suspend fun download() {
        withContext(Dispatchers.IO) {
            val request =
                    DownloadManager.Request(Uri.parse(URL))
                            .setTitle(getString(R.string.app_name))
                            .setDescription(getString(R.string.app_description))
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.

            var finishDownload = false
            while (!finishDownload) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_FAILED -> {
                            finishDownload = true
                            _progress.postValue(RESET_PROGRESS)
                            println("is failed")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            val total = cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (total >= 0) {
                                val downloaded = cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                _progress.postValue((downloaded * 100f) / total)
                                println("is Running, current: progress is: " + _progress.value + " total: " + total)
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            finishDownload = true
                            _progress.postValue(100f)
                            println("is successful")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                //"https://ftp.nluug.nl/pub/graphics/blender/release/Blender2.92/blender-2.92.0-macOS.dmg"
        private const val CHANNEL_ID = "channelId"
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_glide ->
                    if (checked) {
                        // Pirates are the best
                    }
                R.id.radio_loadApp ->
                    if (checked) {
                        // Ninjas rule
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        // Ninjas rule
                    }
                else ->
                    Toast.makeText(this@MainActivity, "ABC", Toast.LENGTH_LONG).show()
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}
