package com.example.mylog

import android.app.Application
import com.example.mylog.data.AppDatabase
import com.example.mylog.data.AuthRepository
import com.example.mylog.data.DiaryRepository
import com.example.mylog.data.NotificationRepository
import com.example.mylog.data.PhotoRepository
import com.example.mylog.data.RecordCategoryStore
import com.example.mylog.data.RecordRepository
import com.example.mylog.data.RoutineRepository
import com.example.mylog.data.SessionStore
import com.example.mylog.notification.RoutineNotificationScheduler

class MylogApplication : Application() {
    lateinit var container: MylogContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = MylogContainer(this)
        RoutineNotificationScheduler().ensureChannel(this)
    }
}

class MylogContainer(application: Application) {
    private val database = AppDatabase.getInstance(application)
    private val sessionStore = SessionStore(application)
    private val scheduler = RoutineNotificationScheduler()
    val recordCategoryStore = RecordCategoryStore(application)

    val authRepository = AuthRepository(database.userDao(), database.photoDao(), sessionStore)
    val diaryRepository = DiaryRepository(database.diaryDao())
    val recordRepository = RecordRepository(database.recordDao())
    val photoRepository = PhotoRepository(application, database.photoDao())
    val routineRepository = RoutineRepository(database.routineDao(), database.routineCheckDao())
    val notificationRepository = NotificationRepository(
        application,
        database.notificationSettingDao(),
        scheduler
    )
}
