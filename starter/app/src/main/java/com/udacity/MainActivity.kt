package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 1
    private var file_name = ""
    private var download_status = ""

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 0
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        radioGroup = findViewById(R.id.download_options)

        custom_button.setOnClickListener {
            download()
        }

        buildChannel(
            getString(R.string.channel_name))
    }

    /**
     * Function to start downloading a file.
     * Send a notification when the download has been finished.
     */
    private fun download() {
        custom_button.buttonState = ButtonState.Loading
        if (radioGroup.checkedRadioButtonId == -1){
            // Send a message to inform the user to select an option
            Toast.makeText(this, getString(R.string.select_option), Toast.LENGTH_SHORT).show()

            // Change the state of the button
            custom_button.buttonState = ButtonState.Completed
            return
        }

        // Save the downloaded file in the storage
        val directory = File(getExternalFilesDir(null), "/udacity")
        if (!directory.exists()) {
            directory.mkdirs()
        }


        radioButton = findViewById(radioGroup.checkedRadioButtonId)
        file_name = radioButton.text.toString()
        Log.i("MainActivity", "RadioButton Text = "+radioButton1.text.toString())

        val url = when(radioGroup.checkedRadioButtonId){
            radioButton1.id -> URLs[0]
            radioButton2.id -> URLs[1]
            else -> URLs[2]
        }

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/udacity/" )
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)

            //Checking the download status
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            val query = DownloadManager.Query() // Filter DownloadManager queries
            query.setFilterById(downloadID)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()){
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        download_status = "Success"
                    }
                    DownloadManager.STATUS_FAILED ->{
                        download_status = "Failed"
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        val reason = cursor.getInt(cursor.getColumnIndex(
                            DownloadManager.COLUMN_REASON))
                        when(reason){
                            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> {
                                Toast.makeText(context, getString(R.string.toast_no_internet),
                                    Toast.LENGTH_SHORT).show()
                            }
                            DownloadManager.PAUSED_WAITING_TO_RETRY -> {
                                Toast.makeText(context, getString(R.string.toast_download_paused),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            if(downloadID == id){
                val notificationManager = getSystemService(
                    NotificationManager::class.java
                ) as NotificationManager

                // Create the Bundle to send all the information to the details screen
                val bundle = Bundle()
                bundle.putString(getString(R.string.file_extra), file_name)
                bundle.putString(getString(R.string.status_extra), download_status)


                // Send the notification
                notificationManager.sendNotification(
                    getString(R.string.notification_description),
                    bundle,
                    context!!
                )

                // Change the button state to completed as the download finished
                custom_button.buttonState = ButtonState.Completed
            }

        }
    }

    private fun buildChannel(channelName: String) {
        // Creating a channel for the notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                channelName, // what the users will see in their settings screen.
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)// This setting will enable the
            notificationChannel.lightColor = Color.WHITE // lights when a notification is shown.
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_title)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun NotificationManager.sendNotification(
        messageBody: String, bundle: Bundle, applicationContext: Context) {
        // ContentIntent that launches the detailActivity
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(getString(R.string.bundle_extra), bundle)

        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Building the notification
        val builder = NotificationCompat.Builder( // To support devices running older versions
            applicationContext,
            CHANNEL_ID // channel id
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent) // passing the PendingIntent
            .setAutoCancel(true) // when the user taps on the notification, the notification dismisses
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            .addAction( // setting up the action button
                R.drawable.ic_assistant_black_24dp,
                applicationContext.getString(R.string.notification_button),
                contentPendingIntent// By tapping on the notification and the action button,
                // it goes to details screen, as it is required
            )

        notify(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private val URLs = listOf(
            "https://github.com/bumptech/glide/blob/master/README.md",
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
            "https://github.com/square/retrofit")
        private const val CHANNEL_ID = "load_app"
    }

}
