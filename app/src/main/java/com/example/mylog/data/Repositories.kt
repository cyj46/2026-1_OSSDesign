package com.example.mylog.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.mylog.notification.RoutineNotificationScheduler
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class UserFacingException(message: String) : Exception(message)

object PasswordHasher {
    private const val SALT_BYTES = 16
    private val secureRandom = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val digest = sha256(salt, password)
        return "${Base64.getEncoder().encodeToString(salt)}:${Base64.getEncoder().encodeToString(digest)}"
    }

    fun verify(password: String, encoded: String): Boolean {
        val parts = encoded.split(":")
        if (parts.size != 2) return false
        val salt = runCatching { Base64.getDecoder().decode(parts[0]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.getDecoder().decode(parts[1]) }.getOrNull() ?: return false
        val actual = sha256(salt, password)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun sha256(salt: ByteArray, password: String): ByteArray {
        return MessageDigest.getInstance("SHA-256")
            .apply {
                update(salt)
                update(password.toByteArray(Charsets.UTF_8))
            }
            .digest()
    }
}

class AuthRepository(
    private val userDao: UserDao,
    private val photoDao: PhotoDao,
    private val sessionStore: SessionStore
) {
    val currentUserId: Flow<Long?> = sessionStore.currentUserId

    suspend fun register(email: String, password: String): Long {
        val normalizedEmail = email.trim().lowercase()
        validateCredentials(normalizedEmail, password)
        if (userDao.findByEmail(normalizedEmail) != null) {
            throw UserFacingException("이미 가입된 이메일입니다.")
        }
        val userId = userDao.insert(
            UserEntity(
                email = normalizedEmail,
                passwordHash = PasswordHasher.hash(password),
                createdAt = System.currentTimeMillis()
            )
        )
        sessionStore.setCurrentUser(userId)
        return userId
    }

    suspend fun login(email: String, password: String): Long {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: throw UserFacingException("존재하지 않는 이메일입니다.")
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            throw UserFacingException("비밀번호가 일치하지 않습니다.")
        }
        sessionStore.setCurrentUser(user.id)
        return user.id
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    suspend fun withdraw(userId: Long, password: String) {
        val user = userDao.findById(userId) ?: throw UserFacingException("사용자 정보를 찾을 수 없습니다.")
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            throw UserFacingException("비밀번호가 일치하지 않습니다.")
        }
        photoDao.listByUser(userId).forEach { photo ->
            runCatching { File(photo.filePath).delete() }
        }
        userDao.delete(user)
        sessionStore.clear()
    }

    private fun validateCredentials(email: String, password: String) {
        if (email.isBlank() || !email.contains("@")) {
            throw UserFacingException("이메일 형식으로 아이디를 입력하세요.")
        }
        if (password.length < 6) {
            throw UserFacingException("비밀번호는 6자 이상이어야 합니다.")
        }
    }
}

class DiaryRepository(private val diaryDao: DiaryDao) {
    fun observeByDate(userId: Long, date: String): Flow<List<DiaryEntity>> {
        return diaryDao.observeByDate(userId, date)
    }

    suspend fun findById(id: Long): DiaryEntity? = diaryDao.findById(id)

    suspend fun save(
        userId: Long,
        id: Long?,
        title: String,
        content: String,
        date: String,
        mood: String
    ): Long {
        if (title.isBlank()) throw UserFacingException("일기 제목을 입력하세요.")
        if (content.isBlank()) throw UserFacingException("일기 내용을 입력하세요.")
        val now = System.currentTimeMillis()
        return if (id == null || id == 0L) {
            diaryDao.insert(
                DiaryEntity(
                    userId = userId,
                    title = title.trim(),
                    content = content.trim(),
                    date = date,
                    mood = mood,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val previous = diaryDao.findById(id) ?: throw UserFacingException("일기를 찾을 수 없습니다.")
            if (previous.userId != userId) throw UserFacingException("수정 권한이 없습니다.")
            diaryDao.update(
                previous.copy(
                    title = title.trim(),
                    content = content.trim(),
                    date = date,
                    mood = mood,
                    updatedAt = now
                )
            )
            id
        }
    }

    suspend fun delete(userId: Long, id: Long) {
        diaryDao.delete(id, userId)
    }
}

class RecordRepository(private val recordDao: RecordDao) {
    fun observeByDate(userId: Long, date: String): Flow<List<RecordEntity>> {
        return recordDao.observeByDate(userId, date)
    }

    fun observeByType(userId: Long, type: String): Flow<List<RecordEntity>> {
        return recordDao.observeByType(userId, type)
    }

    suspend fun findById(id: Long): RecordEntity? = recordDao.findById(id)

    suspend fun save(
        userId: Long,
        id: Long?,
        title: String,
        content: String,
        date: String,
        type: String
    ): Long {
        if (title.isBlank()) throw UserFacingException("기록 제목을 입력하세요.")
        if (content.isBlank()) throw UserFacingException("기록 내용을 입력하세요.")
        val now = System.currentTimeMillis()
        return if (id == null || id == 0L) {
            recordDao.insert(
                RecordEntity(
                    userId = userId,
                    title = title.trim(),
                    content = content.trim(),
                    date = date,
                    type = type,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val previous = recordDao.findById(id) ?: throw UserFacingException("기록을 찾을 수 없습니다.")
            if (previous.userId != userId) throw UserFacingException("수정 권한이 없습니다.")
            recordDao.update(
                previous.copy(
                    title = title.trim(),
                    content = content.trim(),
                    date = date,
                    type = type,
                    updatedAt = now
                )
            )
            id
        }
    }

    suspend fun delete(userId: Long, id: Long) {
        recordDao.delete(id, userId)
    }
}

class PhotoRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {
    fun observeByDate(userId: Long, date: String): Flow<List<PhotoEntity>> {
        return photoDao.observeByDate(userId, date)
    }

    fun observeByOwner(ownerType: String, ownerId: Long): Flow<List<PhotoEntity>> {
        return photoDao.observeByOwner(ownerType, ownerId)
    }

    suspend fun attachPhoto(userId: Long, ownerType: String, ownerId: Long, date: String, sourceUri: Uri): Long {
        val target = copyIntoAppStorage(userId, sourceUri)
        return photoDao.insert(
            PhotoEntity(
                userId = userId,
                ownerType = ownerType,
                ownerId = ownerId,
                filePath = target.absolutePath,
                uploadDate = date
            )
        )
    }

    suspend fun updateDateForOwner(userId: Long, ownerType: String, ownerId: Long, date: String) {
        photoDao.updateDateForOwner(userId, ownerType, ownerId, date)
    }

    suspend fun deletePhoto(id: Long) {
        val photo = photoDao.findById(id) ?: return
        runCatching { File(photo.filePath).delete() }
        photoDao.delete(photo)
    }

    private fun copyIntoAppStorage(userId: Long, sourceUri: Uri): File {
        val extension = resolveExtension(sourceUri)
        val targetDir = File(context.filesDir, "photos/$userId").apply { mkdirs() }
        val target = File(targetDir, "${UUID.randomUUID()}$extension")
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: throw UserFacingException("사진을 열 수 없습니다.")
        input.use { source ->
            target.outputStream().use { output ->
                source.copyTo(output)
            }
        }
        return target
    }

    private fun resolveExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return when {
            extension.isNullOrBlank() -> ".jpg"
            extension == "jpeg" -> ".jpg"
            else -> ".$extension"
        }
    }
}

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val routineCheckDao: RoutineCheckDao
) {
    fun observeActive(userId: Long): Flow<List<RoutineEntity>> {
        return routineDao.observeActive(userId)
    }

    fun observeChecks(userId: Long, date: String): Flow<List<RoutineCheckEntity>> {
        return routineCheckDao.observeForDate(userId, date)
    }

    suspend fun addRoutine(userId: Long, name: String): Long {
        if (name.isBlank()) throw UserFacingException("루틴 이름을 입력하세요.")
        return routineDao.insert(
            RoutineEntity(
                userId = userId,
                name = name.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRoutine(userId: Long, routineId: Long) {
        routineDao.softDelete(routineId, userId)
    }

    suspend fun setCompleted(routineId: Long, date: String, completed: Boolean) {
        val previous = routineCheckDao.findForRoutineDate(routineId, date)
        routineCheckDao.insertOrReplace(
            previous?.copy(isCompleted = completed)
                ?: RoutineCheckEntity(routineId = routineId, date = date, isCompleted = completed)
        )
    }
}

class NotificationRepository(
    private val context: Context,
    private val settingDao: NotificationSettingDao,
    private val scheduler: RoutineNotificationScheduler
) {
    fun observeForUser(userId: Long): Flow<NotificationSettingEntity?> {
        return settingDao.observeForUser(userId)
    }

    suspend fun save(userId: Long, message: String, time: String, enabled: Boolean) {
        if (message.isBlank()) throw UserFacingException("알림 메시지를 입력하세요.")
        if (!Regex("""^\d{2}:\d{2}$""").matches(time)) {
            throw UserFacingException("알림 시간은 HH:mm 형식으로 입력하세요.")
        }
        val previous = settingDao.findForUser(userId)
        val setting = NotificationSettingEntity(
            id = previous?.id ?: 0,
            userId = userId,
            message = message.trim(),
            time = time,
            enabled = enabled
        )
        settingDao.upsert(setting)
        if (enabled) {
            scheduler.schedule(context, userId, time, message.trim())
        } else {
            scheduler.cancel(context, userId)
        }
    }
}
