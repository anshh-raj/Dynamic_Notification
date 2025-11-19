package com.example.dynamic_notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class OfferService : Service(){

    private lateinit var handlerThread: HandlerThread //it is a thread that has a looper
    //looper keeps the thread alive and keeps executing the messages on the thread as we pass them
    private lateinit var handler: Handler // this Handler class is used to pass messages to the looper
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("OfferService")
        handlerThread.start() //starting the thread

        handler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startOfferForegroundService()

        handler.post{
            trackSeconds()
            stopSelf()// to end the service
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun trackSeconds() {
        for (i in 10 downTo 0){
            Thread.sleep(1000)
            //Notification Update
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager //this is the old way
            notificationBuilder
                .setContentText("$i second to redeem the offer")
                .setSilent(true)//to disable notification sound while updating notification
            notificationManager.notify(111, notificationBuilder.build()) //same id passed during creation of foreground service
        }
    }

    fun startOfferForegroundService(){
        createNotificationChannel()
        notificationBuilder = createNotification()
        startForeground(111, notificationBuilder.build())
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel("ID", "D_Notification", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) //this is the new way
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun createNotification(): NotificationCompat.Builder{
        val notification = NotificationCompat.Builder(this, "ID")//same id passed during notification channel creation
            .setContentTitle("Offer you can't refuse")
            .setContentText("60% off on selected items")
            .setContentIntent(getPendingIntent())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}