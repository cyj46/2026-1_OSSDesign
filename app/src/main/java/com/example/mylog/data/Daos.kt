package com.example.mylog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Delete
    suspend fun delete(user: UserEntity)
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries WHERE userId = :userId AND date = :date ORDER BY updatedAt DESC")
    fun observeByDate(userId: Long, date: String): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diaries WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DiaryEntity?

    @Insert
    suspend fun insert(diary: DiaryEntity): Long

    @Update
    suspend fun update(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE userId = :userId AND date = :date ORDER BY updatedAt DESC")
    fun observeByDate(userId: Long, date: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE userId = :userId AND type = :type ORDER BY date DESC, updatedAt DESC")
    fun observeByType(userId: Long, type: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): RecordEntity?

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE userId = :userId AND uploadDate = :date ORDER BY id DESC")
    fun observeByDate(userId: Long, date: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE ownerType = :ownerType AND ownerId = :ownerId ORDER BY id DESC")
    fun observeByOwner(ownerType: String, ownerId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE userId = :userId")
    suspend fun listByUser(userId: Long): List<PhotoEntity>

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Query("UPDATE photos SET uploadDate = :date WHERE userId = :userId AND ownerType = :ownerType AND ownerId = :ownerId")
    suspend fun updateDateForOwner(userId: Long, ownerType: String, ownerId: Long, date: String)

    @Delete
    suspend fun delete(photo: PhotoEntity)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE userId = :userId AND isActive = 1 ORDER BY createdAt ASC")
    fun observeActive(userId: Long): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): RoutineEntity?

    @Insert
    suspend fun insert(routine: RoutineEntity): Long

    @Query("UPDATE routines SET isActive = 0 WHERE id = :id AND userId = :userId")
    suspend fun softDelete(id: Long, userId: Long)
}

@Dao
interface RoutineCheckDao {
    @Query(
        """
        SELECT routine_checks.* FROM routine_checks
        INNER JOIN routines ON routines.id = routine_checks.routineId
        WHERE routines.userId = :userId AND routine_checks.date = :date
        """
    )
    fun observeForDate(userId: Long, date: String): Flow<List<RoutineCheckEntity>>

    @Query("SELECT * FROM routine_checks WHERE routineId = :routineId AND date = :date LIMIT 1")
    suspend fun findForRoutineDate(routineId: Long, date: String): RoutineCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(check: RoutineCheckEntity): Long
}

@Dao
interface NotificationSettingDao {
    @Query("SELECT * FROM notification_settings WHERE userId = :userId LIMIT 1")
    fun observeForUser(userId: Long): Flow<NotificationSettingEntity?>

    @Query("SELECT * FROM notification_settings WHERE userId = :userId LIMIT 1")
    suspend fun findForUser(userId: Long): NotificationSettingEntity?

    @Upsert
    suspend fun upsert(setting: NotificationSettingEntity)
}
