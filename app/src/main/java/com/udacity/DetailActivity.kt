package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        cancelNotification()

        val extras = intent.extras

        extras?.let {
            detail_file_name_text.text = it.getString(EXTRA_FILE_NAME)
            detail_state_text.text = it.getString(EXTRA_DOWNLOAD_STATUS)
        }

        detail_close_button.setOnClickListener {
            finish()
        }
    }

    private fun cancelNotification() {
        val notificationManager =
            ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancelNotifications()
    }
}
