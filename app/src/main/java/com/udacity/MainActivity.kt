package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var notificationManager: NotificationManager

    private var repoUrl: String? = null
    private var repoName: String? = null
    private var repoText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (isNetworkOn(this)) downloadContent() else {
                custom_button.setState(ButtonState.Clicked)
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.getId()) {
                R.id.radio_glide ->
                    if (checked) {
                        repoUrl = GLIDE
                        repoName = getString(R.string.repoNameGlide)
                        repoText = getString(R.string.glide)
                    }
                R.id.radio_loadApp ->
                    if (checked) {
                        repoUrl = UDACITY
                        repoName = getString(R.string.repoNameUdacity)
                        repoText = getString(R.string.loadApp)
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        repoUrl = RETROFIT
                        repoName = getString(R.string.repoNameRetrofit)
                        repoText = getString(R.string.retrofit)
                    }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            notificationManager = ContextCompat.getSystemService(
                context!!,
                NotificationManager::class.java
            ) as NotificationManager

            createChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.channel_name)
            )

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            var finishDownload = false
            while (!finishDownload) {
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_FAILED -> {
                            custom_button.setState(ButtonState.Completed)
                            notificationManager.cancelNotifications()
                            notificationManager.sendNotification(
                                getString(R.string.notification_info_not_ready),
                                repoText.toString(),
                                getString(R.string.statusFailed),
                                context
                            )
                            finishDownload = true
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            custom_button.setState(ButtonState.Completed)
                            notificationManager.cancelNotifications()
                            notificationManager.sendNotification(
                                getString(R.string.notification_info),
                                repoText.toString(),
                                getString(R.string.statusSuccess),
                                context
                            )
                            finishDownload = true
                        }
                    }
                }
            }
        }
    }

    private fun downloadContent() = when {
        repoName != null -> {
            custom_button.setState(ButtonState.Loading)
            download()
        }
        else -> {
            custom_button.setState(ButtonState.Clicked)
            Toast.makeText(
                this,
                getString(R.string.download_no_option_toast),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(repoUrl))
                .setTitle(repoName)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun isNetworkOn(context: Context): Boolean {
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

    companion object {
        private const val UDACITY =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive"
        private const val GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
}
