package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var fileName: TextView // variable to show the downloaded repository name
    private lateinit var status: TextView // variable to show the status of the downloaded repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        fileName = findViewById(R.id.textView3)
        status = findViewById(R.id.textView4)

        // Get the bundle from the mainActivity
        val bundle = intent.getBundleExtra(getString(R.string.bundle_extra))

        // Set text to show the information related to the downloaded file
        fileName.text = bundle?.getString(getString(R.string.file_extra)) ?: ""

        if (bundle?.getString(getString(R.string.status_extra)) == "Success")
            status.text = getString(R.string.success)
        else {
            status.setTextColor(getColor(R.color.red)) // Change the text color if an error
            status.text = getString(R.string.failed)
        }

        // Dismiss the notification
        val notificationManager = getSystemService(
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelAll()

        detail_button.setOnClickListener {
            goBack()
        }

    }
    private fun goBack(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        // Set the flags to not come back to this activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
