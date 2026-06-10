@file:Suppress("DEPRECATION", "SENSELESS_COMPARISON")

package com.example.mylog.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.mylog.data.DiaryEntity
import com.example.mylog.data.NotificationSettingEntity
import com.example.mylog.data.PhotoEntity
import com.example.mylog.data.PhotoOwnerType
import com.example.mylog.data.RecordEntity
import com.example.mylog.data.RoutineCheckEntity
import com.example.mylog.data.RoutineEntity
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val DIARY = "diary"
    const val DIARY_NEW = "diary/new"
    const val DIARY_EDIT = "diary/edit/{id}"
    const val RECORD = "record"
    const val RECORD_NEW = "record/new"
    const val RECORD_EDIT = "record/edit/{id}"
    const val ROUTINE = "routine"
    const val GALLERY = "gallery"
    const val SETTINGS = "settings"
    const val WITHDRAW = "settings/withdraw"
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val mainNavItems = listOf(
    NavItem(Routes.HOME, "홈", Icons.Filled.Home),
    NavItem(Routes.DIARY, "일기", Icons.Filled.MenuBook),
    NavItem(Routes.RECORD, "기록", Icons.Filled.Category),
    NavItem(Routes.ROUTINE, "루틴", Icons.Filled.TaskAlt),
    NavItem(Routes.GALLERY, "사진", Icons.Filled.PhotoLibrary),
    NavItem(Routes.SETTINGS, "설정", Icons.Filled.Settings)
)

@Composable
fun MylogApp(viewModel: MylogViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val userId by viewModel.currentUserId.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.feedback.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (userId == null) {
                AuthFlow(viewModel)
            } else {
                MainFlow(viewModel)
            }
        }
    }
}

@Composable
private fun AuthFlow(viewModel: MylogViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    if (isSignUp) {
        SignUpScreen(
            viewModel = viewModel,
            onBackToLogin = { isSignUp = false }
        )
    } else {
        LoginScreen(
            viewModel = viewModel,
            onSignUp = { isSignUp = true }
        )
    }
}

@Composable
private fun LoginScreen(viewModel: MylogViewModel, onSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AuthSurface(title = "Mylog", subtitle = "일기, 기록, 루틴을 한 곳에서 관리하세요.") {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { scope.launch(Dispatchers.Main.immediate) { viewModel.login(email, password) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }
        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원가입")
        }
    }
}

@Composable
private fun SignUpScreen(viewModel: MylogViewModel, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AuthSurface(title = "회원가입", subtitle = "로컬 저장소에 계정을 만들고 바로 시작합니다.") {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = { Text("비밀번호 확인") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (localError != null) {
            Text(
                text = localError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = {
                scope.launch(Dispatchers.Main.immediate) {
                    if (password != passwordConfirm) {
                        localError = "비밀번호 확인이 일치하지 않습니다."
                        return@launch
                    }
                    localError = null
                    viewModel.register(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("계정 만들기")
        }
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인으로 돌아가기")
        }
    }
}

@Composable
private fun AuthSurface(title: String, subtitle: String, content: @Composable ColumnScopeMarker.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        PolishedCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
        }
    }
}

private typealias ColumnScopeMarker = androidx.compose.foundation.layout.ColumnScope

@Composable
private fun Modifier.pressFeedback(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "press-scale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

@Composable
private fun PolishedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScopeMarker.() -> Unit
) {
    val cardModifier = if (onClick == null) modifier else modifier.pressFeedback(onClick)
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun StickyControlSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun MainFlow(viewModel: MylogViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route.orEmpty()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                mainNavItems.forEach { item ->
                    val selected = currentRoute == item.route || currentRoute.startsWith("${item.route}/")
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                if (item.route == Routes.HOME) {
                                    if (!navController.popBackStack(Routes.HOME, inclusive = false)) {
                                        navController.navigate(Routes.HOME) {
                                            launchSingleTop = true
                                        }
                                    }
                                } else {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(viewModel, navController)
            }
            composable(Routes.DIARY) {
                DiaryScreen(viewModel, navController)
            }
            composable(Routes.DIARY_NEW) {
                DiaryEditScreen(viewModel, diaryId = null, onDone = { navController.popBackStack() })
            }
            composable(
                Routes.DIARY_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                DiaryEditScreen(
                    viewModel = viewModel,
                    diaryId = entry.arguments?.getLong("id"),
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Routes.RECORD) {
                RecordScreen(viewModel, navController)
            }
            composable(Routes.RECORD_NEW) {
                RecordEditScreen(viewModel, recordId = null, onDone = { navController.popBackStack() })
            }
            composable(
                Routes.RECORD_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                RecordEditScreen(
                    viewModel = viewModel,
                    recordId = entry.arguments?.getLong("id"),
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Routes.ROUTINE) {
                RoutineScreen(viewModel)
            }
            composable(Routes.GALLERY) {
                GalleryScreen(viewModel)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel, navController)
            }
            composable(Routes.WITHDRAW) {
                WithdrawScreen(viewModel, onDone = { navController.navigate(Routes.HOME) })
            }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: MylogViewModel, navController: NavHostController) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val diaries by viewModel.diaries.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val checks by viewModel.routineChecks.collectAsStateWithLifecycle()
    val completed = routines.count { MylogViewModel.isCompleted(it, checks) }

    ScreenScaffold(title = "Mylog") {
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            )
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("오늘의 기록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(date, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "일기 ${diaries.size}개 · 기록 ${records.size}개 · 루틴 $completed/${routines.size} 완료",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MenuTile("일기", Icons.Filled.MenuBook, Modifier.weight(1f)) { navController.navigate(Routes.DIARY) }
                    MenuTile("기록", Icons.Filled.Category, Modifier.weight(1f)) { navController.navigate(Routes.RECORD) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MenuTile("루틴", Icons.Filled.TaskAlt, Modifier.weight(1f)) { navController.navigate(Routes.ROUTINE) }
                    MenuTile("사진첩", Icons.Filled.PhotoLibrary, Modifier.weight(1f)) { navController.navigate(Routes.GALLERY) }
                }
            }
            item {
                SectionTitle("오늘 루틴")
            }
            if (routines.isEmpty()) {
                item { EmptyState("아직 등록된 루틴이 없습니다.", "루틴 화면에서 매일 반복할 일을 추가하세요.") }
            } else {
                items(routines, key = { it.id }) { routine ->
                    RoutineRow(
                        routine = routine,
                        checked = MylogViewModel.isCompleted(routine, checks),
                        date = date,
                        viewModel = viewModel,
                        allowDelete = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiaryScreen(viewModel: MylogViewModel, navController: NavHostController) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val diaries by viewModel.diaries.collectAsStateWithLifecycle()

    ScreenScaffold(title = "일기") {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stickyHeader {
                    StickyControlSurface {
                        DateNavigator(
                            date = date,
                            onDateChange = viewModel::setSelectedDate,
                            onPrevious = { viewModel.moveSelectedDate(-1) },
                            onNext = { viewModel.moveSelectedDate(1) }
                        )
                    }
                }
                if (diaries.isEmpty()) {
                    item { EmptyState("선택한 날짜의 일기가 없습니다.", "작성 버튼으로 오늘의 감정과 내용을 남기세요.") }
                } else {
                    items(diaries, key = { it.id }) { diary ->
                        DiaryCard(diary) { navController.navigate("diary/edit/${diary.id}") }
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.DIARY_NEW) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("일기 작성") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
            )
        }
    }
}

@Composable
private fun DiaryEditScreen(viewModel: MylogViewModel, diaryId: Long?, onDone: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var date by remember(diaryId) { mutableStateOf(selectedDate) }
    var mood by remember { mutableStateOf(MylogViewModel.MOODS.first()) }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedPhoto = uri
    }
    val existingPhotos by remember(diaryId) {
        if (diaryId == null) flowOf(emptyList()) else viewModel.photosFor(PhotoOwnerType.DIARY, diaryId)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(diaryId) {
        if (diaryId != null) {
            viewModel.findDiary(diaryId)?.let { diary ->
                title = diary.title
                content = diary.content
                date = diary.date
                mood = diary.mood
            }
        }
    }

    EditScaffold(
        title = if (diaryId == null) "일기 작성" else "일기 수정",
        onBack = onDone,
        onSave = {
            scope.launch(Dispatchers.Main.immediate) {
                if (viewModel.saveDiary(diaryId, title, content, date, mood, selectedPhoto)) {
                    viewModel.setSelectedDate(date)
                    if (selectedPhoto != null || existingPhotos.isNotEmpty()) {
                        viewModel.setGalleryDate(date)
                    }
                    onDone()
                }
            }
        },
        onDelete = if (diaryId == null) null else ({ confirmDelete = true })
    ) {
        LogForm(
            title = title,
            onTitleChange = { title = it },
            content = content,
            onContentChange = { content = it },
            date = date,
            onDateChange = { date = it },
            selectorLabel = "기분",
            selectorValue = mood,
            selectorValues = MylogViewModel.MOODS,
            onSelectorChange = { mood = it },
            selectedPhoto = selectedPhoto,
            existingPhotos = existingPhotos,
            onPickPhoto = {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDeletePhoto = { photoId -> scope.launch(Dispatchers.Main.immediate) { viewModel.deletePhoto(photoId) } }
        )
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "일기 삭제",
            body = "이 일기를 삭제할까요?",
            onDismiss = { confirmDelete = false },
            onConfirm = {
                val targetId = diaryId
                confirmDelete = false
                if (targetId != null) {
                    scope.launch(Dispatchers.Main.immediate) {
                        if (viewModel.deleteDiary(targetId)) onDone()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordScreen(viewModel: MylogViewModel, navController: NavHostController) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val mode by viewModel.recordFilterMode.collectAsStateWithLifecycle()
    val type by viewModel.recordFilterType.collectAsStateWithLifecycle()
    val recordTypes by viewModel.recordTypes.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    ScreenScaffold(title = "기록") {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stickyHeader {
                    StickyControlSurface {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = mode == RecordFilterMode.DATE,
                                    onClick = { viewModel.setRecordFilterMode(RecordFilterMode.DATE) },
                                    label = { Text("날짜") }
                                )
                                FilterChip(
                                    selected = mode == RecordFilterMode.TYPE,
                                    onClick = { viewModel.setRecordFilterMode(RecordFilterMode.TYPE) },
                                    label = { Text("카테고리") }
                                )
                            }
                            if (mode == RecordFilterMode.DATE) {
                                DateNavigator(
                                    date = date,
                                    onDateChange = viewModel::setSelectedDate,
                                    onPrevious = { viewModel.moveSelectedDate(-1) },
                                    onNext = { viewModel.moveSelectedDate(1) }
                                )
                            } else {
                                FlowChips(
                                    values = recordTypes,
                                    selected = type,
                                    onSelected = viewModel::setRecordFilterType
                                )
                                OutlinedButton(onClick = { showAddCategoryDialog = true }) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("카테고리 추가")
                                }
                            }
                        }
                    }
                }
                if (records.isEmpty()) {
                    item { EmptyState("조회된 기록이 없습니다.", "책, 영화, 여행, 맛집 등 남기고 싶은 내용을 작성하세요.") }
                } else {
                    items(records, key = { it.id }) { record ->
                        RecordCard(record) { navController.navigate("record/edit/${record.id}") }
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Routes.RECORD_NEW) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("기록 작성") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
            )
        }
    }

    if (showAddCategoryDialog) {
        AddRecordTypeDialog(
            viewModel = viewModel,
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}

@Composable
private fun RecordEditScreen(viewModel: MylogViewModel, recordId: Long?, onDone: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val recordFilterMode by viewModel.recordFilterMode.collectAsStateWithLifecycle()
    val recordFilterType by viewModel.recordFilterType.collectAsStateWithLifecycle()
    val recordTypes by viewModel.recordTypes.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var date by remember(recordId) { mutableStateOf(selectedDate) }
    var type by remember(recordId) {
        mutableStateOf(
            if (recordId == null && recordFilterMode == RecordFilterMode.TYPE) {
                recordFilterType
            } else {
                recordTypes.firstOrNull() ?: MylogViewModel.RECORD_TYPES.first()
            }
        )
    }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedPhoto = uri
    }
    val existingPhotos by remember(recordId) {
        if (recordId == null) flowOf(emptyList()) else viewModel.photosFor(PhotoOwnerType.RECORD, recordId)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(recordId) {
        if (recordId != null) {
            viewModel.findRecord(recordId)?.let { record ->
                title = record.title
                content = record.content
                date = record.date
                type = record.type
            }
        }
    }

    EditScaffold(
        title = if (recordId == null) "기록 작성" else "기록 수정",
        onBack = onDone,
        onSave = {
            scope.launch(Dispatchers.Main.immediate) {
                if (viewModel.saveRecord(recordId, title, content, date, type, selectedPhoto)) {
                    viewModel.setSelectedDate(date)
                    viewModel.setRecordFilterMode(RecordFilterMode.DATE)
                    viewModel.setRecordFilterType(type)
                    if (selectedPhoto != null || existingPhotos.isNotEmpty()) {
                        viewModel.setGalleryDate(date)
                    }
                    onDone()
                }
            }
        },
        onDelete = if (recordId == null) null else ({ confirmDelete = true })
    ) {
        LogForm(
            title = title,
            onTitleChange = { title = it },
            content = content,
            onContentChange = { content = it },
            date = date,
            onDateChange = { date = it },
            selectorLabel = "카테고리",
            selectorValue = type,
            selectorValues = if (type in recordTypes) recordTypes else listOf(type) + recordTypes,
            onSelectorChange = { type = it },
            selectedPhoto = selectedPhoto,
            existingPhotos = existingPhotos,
            onPickPhoto = {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDeletePhoto = { photoId -> scope.launch(Dispatchers.Main.immediate) { viewModel.deletePhoto(photoId) } }
        )
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "기록 삭제",
            body = "이 기록을 삭제할까요?",
            onDismiss = { confirmDelete = false },
            onConfirm = {
                val targetId = recordId
                confirmDelete = false
                if (targetId != null) {
                    scope.launch(Dispatchers.Main.immediate) {
                        if (viewModel.deleteRecord(targetId)) onDone()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineScreen(viewModel: MylogViewModel) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val checks by viewModel.routineChecks.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    ScreenScaffold(title = "루틴") {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stickyHeader {
                    StickyControlSurface {
                        DateNavigator(
                            date = date,
                            onDateChange = viewModel::setSelectedDate,
                            onPrevious = { viewModel.moveSelectedDate(-1) },
                            onNext = { viewModel.moveSelectedDate(1) }
                        )
                    }
                }
                if (routines.isEmpty()) {
                    item { EmptyState("등록된 루틴이 없습니다.", "매일 반복할 일을 추가하고 완료 여부를 체크하세요.") }
                } else {
                    items(routines, key = { it.id }) { routine ->
                        RoutineRow(
                            routine = routine,
                            checked = MylogViewModel.isCompleted(routine, checks),
                            date = date,
                            viewModel = viewModel,
                            allowDelete = true
                        )
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("루틴 추가") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
            )
        }
    }

    if (showAddDialog) {
        AddRoutineDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun GalleryScreen(viewModel: MylogViewModel) {
    val date by viewModel.galleryDate.collectAsStateWithLifecycle()
    val photos by viewModel.galleryPhotos.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    ScreenScaffold(title = "사진첩") {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DateNavigator(
                date = date,
                onDateChange = viewModel::setGalleryDate,
                onPrevious = { viewModel.moveGalleryDate(-1) },
                onNext = { viewModel.moveGalleryDate(1) }
            )
            if (photos.isEmpty()) {
                EmptyState("선택한 날짜의 사진이 없습니다.", "일기나 기록에 사진을 첨부하면 이곳에서 날짜별로 볼 수 있습니다.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(132.dp),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoTile(photo = photo, onDelete = { scope.launch(Dispatchers.Main.immediate) { viewModel.deletePhoto(photo.id) } })
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: MylogViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val userId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val setting by viewModel.notificationSetting.collectAsStateWithLifecycle()
    var enabled by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("21:00") }
    var message by remember { mutableStateOf("오늘의 루틴을 체크해 보세요.") }
    val scope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch(Dispatchers.Main.immediate) {
                viewModel.saveNotification(message, time, enabled = true)
            }
        } else {
            enabled = false
            scope.launch(Dispatchers.Main.immediate) {
                viewModel.saveNotification(message, time, enabled = false)
            }
        }
    }

    LaunchedEffect(setting, userId) {
        val next = setting ?: userId?.let { MylogViewModel.defaultNotification(it) }
        if (next != null) {
            enabled = next.enabled
            time = next.time
            message = next.message
        }
    }

    ScreenScaffold(title = "설정") {
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PolishedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("루틴 알림", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Switch(checked = enabled, onCheckedChange = { enabled = it })
                        }
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("알림 시간") },
                            placeholder = { Text("21:00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("알림 메시지") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val needsNotificationPermission = enabled &&
                                    Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED

                                if (needsNotificationPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    scope.launch(Dispatchers.Main.immediate) {
                                        viewModel.saveNotification(message, time, enabled)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("알림 설정 저장")
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { scope.launch(Dispatchers.Main.immediate) { viewModel.logout() } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("로그아웃")
                }
            }
            item {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.WITHDRAW) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PersonRemove, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("회원 탈퇴")
                }
            }
        }
    }
}

@Composable
private fun WithdrawScreen(viewModel: MylogViewModel, onDone: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    EditScaffold(title = "회원 탈퇴", onBack = onDone, onSave = { confirm = true }, saveLabel = "탈퇴") {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("회원 탈퇴 시 계정과 모든 일기, 기록, 루틴, 사진 정보가 기기에서 삭제됩니다.")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호 확인") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (confirm) {
        ConfirmDialog(
            title = "회원 탈퇴",
            body = "정말 탈퇴할까요? 이 작업은 되돌릴 수 없습니다.",
            onDismiss = { confirm = false },
            onConfirm = {
                confirm = false
                scope.launch(Dispatchers.Main.immediate) {
                    if (viewModel.withdraw(password)) onDone()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenScaffold(title: String, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScaffold(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveLabel: String = "저장",
    onDelete: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "삭제")
                        }
                    }
                    TextButton(onClick = onSave) {
                        Text(saveLabel)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

@Composable
private fun LogForm(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    selectorLabel: String,
    selectorValue: String,
    selectorValues: List<String>,
    onSelectorChange: (String) -> Unit,
    selectedPhoto: Uri?,
    existingPhotos: List<PhotoEntity>,
    onPickPhoto: () -> Unit,
    onDeletePhoto: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("날짜") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(selectorLabel, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            FlowChips(selectorValues, selectorValue, onSelectorChange)
        }
        item {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("내용") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedButton(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (selectedPhoto == null) "사진 선택" else "선택한 사진 변경")
            }
        }
        if (selectedPhoto != null) {
            item {
                AsyncImage(
                    model = selectedPhoto,
                    contentDescription = "선택한 사진",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        if (existingPhotos.isNotEmpty()) {
            item { SectionTitle("첨부 사진") }
            items(existingPhotos, key = { it.id }) { photo ->
                PhotoAttachmentRow(photo, onDeletePhoto)
            }
        }
    }
}

@Composable
private fun DateNavigator(
    date: String,
    onDateChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    PolishedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "이전 날짜")
            }
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("날짜") },
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "다음 날짜")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(values: List<String>, selected: String, onSelected: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        values.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(value, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun DiaryCard(diary: DiaryEntity, onClick: () -> Unit) {
    LogCard(
        title = diary.title,
        meta = "${diary.date} · ${diary.mood}",
        content = diary.content,
        icon = Icons.Filled.MenuBook,
        onClick = onClick
    )
}

@Composable
private fun RecordCard(record: RecordEntity, onClick: () -> Unit) {
    LogCard(
        title = record.title,
        meta = "${record.date} · ${record.type}",
        content = record.content,
        icon = Icons.Filled.Category,
        onClick = onClick
    )
}

@Composable
private fun LogCard(title: String, meta: String, content: String, icon: ImageVector, onClick: () -> Unit) {
    PolishedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(content, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RoutineRow(
    routine: RoutineEntity,
    checked: Boolean,
    date: String,
    viewModel: MylogViewModel,
    allowDelete: Boolean
) {
    val scope = rememberCoroutineScope()
    PolishedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { scope.launch(Dispatchers.Main.immediate) { viewModel.setRoutineCompleted(routine.id, date, !checked) } },
        containerColor = if (checked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { next ->
                    scope.launch(Dispatchers.Main.immediate) { viewModel.setRoutineCompleted(routine.id, date, next) }
                }
            )
            Text(
                routine.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (checked) {
                AssistChip(
                    onClick = {},
                    label = { Text("완료") },
                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
            if (allowDelete) {
                IconButton(onClick = { scope.launch(Dispatchers.Main.immediate) { viewModel.deleteRoutine(routine.id) } }) {
                    Icon(Icons.Filled.Delete, contentDescription = "루틴 삭제")
                }
            }
        }
    }
}

@Composable
private fun AddRoutineDialog(viewModel: MylogViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("루틴 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("루틴 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.Main.immediate) {
                        if (viewModel.addRoutine(name)) onDismiss()
                    }
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun AddRecordTypeDialog(viewModel: MylogViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("카테고리 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("카테고리 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.Main.immediate) {
                        if (viewModel.addRecordType(name)) onDismiss()
                    }
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun PhotoTile(photo: PhotoEntity, onDelete: () -> Unit) {
    PolishedCard(modifier = Modifier.fillMaxWidth()) {
        Box {
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = "사진",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.48f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "사진 삭제", tint = Color.White)
            }
        }
    }
}

@Composable
private fun PhotoAttachmentRow(photo: PhotoEntity, onDeletePhoto: (Long) -> Unit) {
    PolishedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = "첨부 사진",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Text(photo.uploadDate, modifier = Modifier.weight(1f))
            IconButton(onClick = { onDeletePhoto(photo.id) }) {
                Icon(Icons.Filled.Delete, contentDescription = "사진 삭제")
            }
        }
    }
}

@Composable
private fun MenuTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    PolishedCard(
        modifier = modifier
            .height(104.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyState(title: String, body: String) {
    PolishedCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
