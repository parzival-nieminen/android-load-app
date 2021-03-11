package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private var downloadID: Long = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var repoUrl: String? = null
    private var repoName: String? = null

    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (isNetworkAvailable(this)) {
                download()
            } else {
                custom_button.setState(ButtonState.Clicked)
                Toast.makeText(this@MainActivity, "No Network No Download", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun download() {
        when {
            repoName != null -> {
                custom_button.setState(ButtonState.Loading)
                //downloadContent()
            }
            else -> {
                custom_button.setState(ButtonState.Completed)
                Toast.makeText(this, "Nothing to do, choose a download", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun downloadContent() {
        val request =
            DownloadManager.Request(Uri.parse(repoUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        var finishDownload = false
        while (!finishDownload) {
            val cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        custom_button.setState(ButtonState.Completed)
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total =
                            cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded =
                                cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            //_progress.postValue((downloaded * 100f) / total)
                            //custom_button.setProgress((downloaded * 100f) / total)

                            println("is Running, current: progress is: " + (downloaded * 100f) / total + " total: " + total)
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finishDownload = true
                        custom_button.setState(ButtonState.Completed)
                    }
                }
            }
        }
    }


    companion object {
        private const val URL =
            "https://ftp.nluug.nl/pub/graphics/blender/release/Blender2.92/blender-2.92.0-macOS.dmg"
        private const val UDACITY =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            when (view.getId()) {
                R.id.radio_glide ->
                    if (checked) {
                        repoUrl = GLIDE
                        repoName = "Glide"
                    }
                R.id.radio_loadApp ->
                    if (checked) {
                        repoUrl = UDACITY
                        repoName = "Udacity"
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        repoUrl = RETROFIT
                        repoName = "Retrofit"
                    }
                else ->
                    Toast.makeText(this@MainActivity, "ABC", Toast.LENGTH_LONG).show()
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }
}
