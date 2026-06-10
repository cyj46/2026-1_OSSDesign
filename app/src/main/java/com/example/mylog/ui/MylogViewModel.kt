package com.example.mylog.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylog.data.AuthRepository
import com.example.mylog.data.DiaryEntity
import com.example.mylog.data.DiaryRepository
import com.example.mylog.data.NotificationRepository
import com.example.mylog.data.NotificationSettingEntity
import com.example.mylog.data.PhotoEntity
import com.example.mylog.data.PhotoOwnerType
import com.example.mylog.data.PhotoRepository
import com.example.mylog.data.RecordCategoryStore
import com.example.mylog.data.RecordEntity
import com.example.mylog.data.RecordRepository
import com.example.mylog.data.RoutineCheckEntity
import com.example.mylog.data.RoutineEntity
import com.example.mylog.data.RoutineRepository
import com.example.mylog.data.UserFacingException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

enum class RecordFilterMode {
    DATE,
    TYPE
}

@OptIn(ExperimentalCoroutinesApi::class)
class MylogViewModel(
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository,
    private val recordRepository: RecordRepository,
    private val photoRepository: PhotoRepository,
    private val routineRepository: RoutineRepository,
    private val notificationRepository: NotificationRepository,
    private val recordCategoryStore: RecordCategoryStore
) : ViewModel() {
    private val _feedback = MutableSharedFlow<String>()
    val feedback = _feedback.asSharedFlow()

    val currentUserId = authRepository.currentUserId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _selectedDate = MutableStateFlow(today())
    val selectedDate = _selectedDate.asStateFlow()

    private val _galleryDate = MutableStateFlow(today())
    val galleryDate = _galleryDate.asStateFlow()

    private val _recordFilterMode = MutableStateFlow(RecordFilterMode.DATE)
    val recordFilterMode = _recordFilterMode.asStateFlow()

    private val _recordFilterType = MutableStateFlow(RECORD_TYPES.first())
    val recordFilterType = _recordFilterType.asStateFlow()

    val recordTypes = recordCategoryStore.customTypes
        .map { customTypes ->
            RECORD_TYPES + customTypes.filter { customType ->
                RECORD_TYPES.none { defaultType -> defaultType.equals(customType, ignoreCase = true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RECORD_TYPES)

    val diaries = combine(currentUserId, selectedDate) { userId, date -> userId to date }
        .flatMapLatest { (userId, date) ->
            if (userId == null) flowOf(emptyList()) else diaryRepository.observeByDate(userId, date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val records = combine(
        currentUserId,
        selectedDate,
        recordFilterMode,
        recordFilterType
    ) { userId, date, mode, type -> RecordFilter(userId, date, mode, type) }
        .flatMapLatest { filter ->
            val userId = filter.userId
            if (userId == null) {
                flowOf(emptyList())
            } else if (filter.mode == RecordFilterMode.TYPE) {
                recordRepository.observeByType(userId, filter.type)
            } else {
                recordRepository.observeByDate(userId, filter.date)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val routines = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else routineRepository.observeActive(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val routineChecks = combine(currentUserId, selectedDate) { userId, date -> userId to date }
        .flatMapLatest { (userId, date) ->
            if (userId == null) flowOf(emptyList()) else routineRepository.observeChecks(userId, date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val galleryPhotos = combine(currentUserId, galleryDate) { userId, date -> userId to date }
        .flatMapLatest { (userId, date) ->
            if (userId == null) flowOf(emptyList()) else photoRepository.observeByDate(userId, date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notificationSetting = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(null) else notificationRepository.observeForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setGalleryDate(date: String) {
        _galleryDate.value = date
    }

    fun moveSelectedDate(days: Long) {
        _selectedDate.value = shiftDate(_selectedDate.value, days)
    }

    fun moveGalleryDate(days: Long) {
        _galleryDate.value = shiftDate(_galleryDate.value, days)
    }

    fun setRecordFilterMode(mode: RecordFilterMode) {
        _recordFilterMode.value = mode
    }

    fun setRecordFilterType(type: String) {
        _recordFilterType.value = type
    }

    suspend fun addRecordType(type: String): Boolean {
        return perform("카테고리를 추가했습니다.") {
            val cleaned = type.trim()
            if (cleaned.isBlank()) {
                throw UserFacingException("카테고리 이름을 입력하세요.")
            }
            if (recordTypes.value.any { it.equals(cleaned, ignoreCase = true) }) {
                throw UserFacingException("이미 있는 카테고리입니다.")
            }
            withContext(Dispatchers.IO) {
                recordCategoryStore.add(cleaned)
            }
            _recordFilterMode.value = RecordFilterMode.TYPE
            _recordFilterType.value = cleaned
        }
    }

    suspend fun register(email: String, password: String): Boolean {
        return perform("회원가입이 완료되었습니다.") {
            authRepository.register(email, password)
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return perform("로그인되었습니다.") {
            authRepository.login(email, password)
        }
    }

    suspend fun logout(): Boolean {
        return perform("로그아웃되었습니다.") {
            authRepository.logout()
        }
    }

    suspend fun withdraw(password: String): Boolean {
        return perform("회원 탈퇴가 완료되었습니다.") {
            authRepository.withdraw(requireUserId(), password)
        }
    }

    suspend fun findDiary(id: Long): DiaryEntity? = withContext(Dispatchers.IO) {
        diaryRepository.findById(id)
    }

    suspend fun saveDiary(
        id: Long?,
        title: String,
        content: String,
        date: String,
        mood: String,
        photoUri: Uri?
    ): Boolean {
        return perform("일기를 저장했습니다.") {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                val savedId = diaryRepository.save(userId, id, title, content, date, mood)
                photoRepository.updateDateForOwner(userId, PhotoOwnerType.DIARY, savedId, date)
                if (photoUri != null) {
                    photoRepository.attachPhoto(userId, PhotoOwnerType.DIARY, savedId, date, photoUri)
                }
            }
        }
    }

    suspend fun deleteDiary(id: Long): Boolean {
        return perform("일기를 삭제했습니다.") {
            withContext(Dispatchers.IO) {
                diaryRepository.delete(requireUserId(), id)
            }
        }
    }

    suspend fun findRecord(id: Long): RecordEntity? = withContext(Dispatchers.IO) {
        recordRepository.findById(id)
    }

    suspend fun saveRecord(
        id: Long?,
        title: String,
        content: String,
        date: String,
        type: String,
        photoUri: Uri?
    ): Boolean {
        return perform("기록을 저장했습니다.") {
            val userId = requireUserId()
            withContext(Dispatchers.IO) {
                val savedId = recordRepository.save(userId, id, title, content, date, type)
                photoRepository.updateDateForOwner(userId, PhotoOwnerType.RECORD, savedId, date)
                if (photoUri != null) {
                    photoRepository.attachPhoto(userId, PhotoOwnerType.RECORD, savedId, date, photoUri)
                }
            }
        }
    }

    suspend fun deleteRecord(id: Long): Boolean {
        return perform("기록을 삭제했습니다.") {
            withContext(Dispatchers.IO) {
                recordRepository.delete(requireUserId(), id)
            }
        }
    }

    fun photosFor(ownerType: String, ownerId: Long): Flow<List<PhotoEntity>> {
        return photoRepository.observeByOwner(ownerType, ownerId)
    }

    suspend fun deletePhoto(id: Long): Boolean {
        return perform("사진을 삭제했습니다.") {
            withContext(Dispatchers.IO) {
                photoRepository.deletePhoto(id)
            }
        }
    }

    suspend fun addRoutine(name: String): Boolean {
        return perform("루틴을 추가했습니다.") {
            withContext(Dispatchers.IO) {
                routineRepository.addRoutine(requireUserId(), name)
            }
        }
    }

    suspend fun deleteRoutine(routineId: Long): Boolean {
        return perform("루틴을 삭제했습니다.") {
            withContext(Dispatchers.IO) {
                routineRepository.deleteRoutine(requireUserId(), routineId)
            }
        }
    }

    suspend fun setRoutineCompleted(routineId: Long, date: String, completed: Boolean): Boolean {
        return perform(null) {
            withContext(Dispatchers.IO) {
                routineRepository.setCompleted(routineId, date, completed)
            }
        }
    }

    suspend fun saveNotification(message: String, time: String, enabled: Boolean): Boolean {
        return perform("알림 설정을 저장했습니다.") {
            withContext(Dispatchers.IO) {
                notificationRepository.save(requireUserId(), message, time, enabled)
            }
        }
    }

    private suspend fun perform(successMessage: String?, block: suspend () -> Unit): Boolean {
        return try {
            block()
            if (successMessage != null) _feedback.emit(successMessage)
            true
        } catch (exception: UserFacingException) {
            _feedback.emit(exception.message ?: "처리할 수 없습니다.")
            false
        } catch (exception: Exception) {
            _feedback.emit("처리 중 문제가 발생했습니다: ${exception.message ?: exception.javaClass.simpleName}")
            false
        }
    }

    private fun requireUserId(): Long {
        return currentUserId.value ?: throw UserFacingException("로그인이 필요합니다.")
    }

    private fun shiftDate(date: String, days: Long): String {
        return runCatching {
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE).plusDays(days).format(DateTimeFormatter.ISO_DATE)
        }.getOrElse {
            today()
        }
    }

    private data class RecordFilter(
        val userId: Long?,
        val date: String,
        val mode: RecordFilterMode,
        val type: String
    )

    companion object {
        val MOODS = listOf("좋음", "보통", "피곤", "슬픔", "화남")
        val RECORD_TYPES = listOf("책", "영화", "맛집", "여행", "기타")

        fun today(): String {
            return LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        }

        fun isCompleted(routine: RoutineEntity, checks: List<RoutineCheckEntity>): Boolean {
            return checks.firstOrNull { it.routineId == routine.id }?.isCompleted == true
        }

        fun defaultNotification(userId: Long): NotificationSettingEntity {
            return NotificationSettingEntity(
                userId = userId,
                message = "오늘의 루틴을 체크해 보세요.",
                time = "21:00",
                enabled = false
            )
        }
    }
}
