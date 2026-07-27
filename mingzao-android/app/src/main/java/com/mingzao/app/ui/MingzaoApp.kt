package com.mingzao.app.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mingzao.app.R
import com.mingzao.app.audio.RiverSoundPlayer
import com.mingzao.app.audio.VoiceRecorder
import com.mingzao.app.data.MingzaoStore
import com.mingzao.app.data.NightNote
import com.mingzao.app.data.PetProfile
import com.mingzao.app.ui.theme.Coral
import com.mingzao.app.ui.theme.Cream
import com.mingzao.app.ui.theme.Honey
import com.mingzao.app.ui.theme.Ink
import com.mingzao.app.ui.theme.Moss
import com.mingzao.app.ui.theme.Night
import com.mingzao.app.ui.theme.NightBlue
import com.mingzao.app.ui.theme.SoftInk
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Screen {
    HOME,
    NIGHT,
    CAPTURE,
    INBOX,
    GARDEN,
}

private data class CatBreedOption(
    val id: String,
    val name: String,
    val shortName: String,
    val alertRes: Int,
    val fishingRes: Int,
)

private val CatBreedOptions = listOf(
    CatBreedOption("orange", "橘色家猫", "橘猫", R.drawable.cat_orange_gentle_v2, R.drawable.cat_orange_fishing_v2),
    CatBreedOption("lihua", "中华狸花猫", "狸花", R.drawable.cat_lihua_gentle_v2, R.drawable.cat_lihua_fishing),
    CatBreedOption("british", "英国短毛猫", "英短", R.drawable.cat_british_gentle_v2, R.drawable.cat_british_fishing),
    CatBreedOption("american", "美国短毛猫", "美短", R.drawable.cat_american_gentle_v2, R.drawable.cat_american_fishing),
    CatBreedOption("ragdoll", "布偶猫", "布偶", R.drawable.cat_ragdoll_gentle_v2, R.drawable.cat_ragdoll_fishing),
    CatBreedOption("siamese", "暹罗猫", "暹罗", R.drawable.cat_siamese_gentle_v2, R.drawable.cat_siamese_fishing),
    CatBreedOption("maine", "缅因猫", "缅因", R.drawable.cat_maine_gentle_v2, R.drawable.cat_maine_fishing),
    CatBreedOption("persian", "波斯猫", "波斯", R.drawable.cat_persian_gentle_v2, R.drawable.cat_persian_fishing),
)

private fun catBreedOption(id: String): CatBreedOption =
    CatBreedOptions.firstOrNull { it.id == id } ?: CatBreedOptions.first()

@Composable
fun MingzaoApp() {
    val context = LocalContext.current
    val store = remember { MingzaoStore(context) }
    var profile by remember { mutableStateOf(store.loadProfile()) }
    val notes = remember {
        mutableStateListOf<NightNote>().apply { addAll(store.loadNotes()) }
    }
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }

    fun updateProfile(updated: PetProfile) {
        profile = updated
        store.saveProfile(updated)
    }

    fun addNote(note: NightNote) {
        notes.add(0, note)
        store.saveNotes(notes)
    }

    if (!profile.adopted) {
        AdoptionScreen { type, breed, name ->
            updateProfile(
                profile.copy(
                    adopted = true,
                    type = type,
                    breed = breed,
                    name = name.ifBlank { if (type == "cat") "小满" else "团子" },
                ),
            )
        }
        return
    }

    when (screen) {
        Screen.NIGHT -> NightScreen(
            profile = profile,
            onCapture = { screen = Screen.CAPTURE },
            onMorning = { screen = Screen.HOME },
        )

        Screen.CAPTURE -> CaptureScreen(
            onSave = {
                addNote(it)
                screen = Screen.NIGHT
            },
            onBack = { screen = Screen.NIGHT },
        )

        else -> {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = Cream,
                bottomBar = {
                    BottomNav(
                        active = screen,
                        onSelected = { screen = it },
                    )
                },
            ) { padding ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        modifier = Modifier.padding(padding),
                        profile = profile,
                        pendingCount = notes.count { !it.completed },
                        onNight = { screen = Screen.NIGHT },
                        onInbox = { screen = Screen.INBOX },
                    )

                    Screen.INBOX -> InboxScreen(
                        modifier = Modifier.padding(padding),
                        notes = notes,
                        onToggle = { id ->
                            val index = notes.indexOfFirst { it.id == id }
                            if (index >= 0) {
                                notes[index] = notes[index].copy(completed = !notes[index].completed)
                                store.saveNotes(notes)
                            }
                        },
                    )

                    Screen.GARDEN -> GardenScreen(
                        modifier = Modifier.padding(padding),
                        profile = profile,
                    )

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun AdoptionScreen(onAdopt: (String, String, String) -> Unit) {
    var petType by rememberSaveable { mutableStateOf("cat") }
    var catBreed by rememberSaveable { mutableStateOf("orange") }
    var petName by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFCF5), Color(0xFFF4EAD4)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "明早再想",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(34.dp))
        Text(
            text = "领养一位\n夜间伙伴",
            color = Ink,
            fontSize = 37.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "它替你守住念头，也提醒你把夜晚还给自己。",
            color = SoftInk,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(30.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PetChoice(
                selected = petType == "cat",
                emoji = "🐱",
                label = "爱钓鱼的小猫",
                onClick = { petType = "cat" },
            )
            PetChoice(
                selected = petType == "dog",
                emoji = "🐶",
                label = "爱守夜的小狗",
                onClick = { petType = "dog" },
            )
        }

        AnimatedVisibility(visible = petType == "cat") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Text(
                    "选择猫咪品种 · 已收录 ${CatBreedOptions.size} 种",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CatBreedOptions) { breed ->
                        CatBreedChoice(
                            breed = breed,
                            selected = catBreed == breed.id,
                            onClick = { catBreed = breed.id },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = petName,
            onValueChange = { petName = it.take(8) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("给伙伴取个名字") },
            placeholder = { Text(if (petType == "cat") "例如：小满" else "例如：团子") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onAdopt(petType, if (petType == "cat") catBreed else "dog", petName) },
            ),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "“夜里我去钓鱼，明早给你炖汤。”",
            color = Moss,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onAdopt(petType, if (petType == "cat") catBreed else "dog", petName) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("带它回家", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CatBreedChoice(
    breed: CatBreedOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.54f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Honey else Color(0xFFE5DDCC),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(breed.alertRes),
            contentDescription = breed.name,
            modifier = Modifier.size(74.dp),
        )
        Text(
            breed.shortName,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun PetChoice(
    selected: Boolean,
    emoji: String,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.5f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Honey else Color(0xFFE5DDCC),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(label, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (selected) "已选择" else "轻触选择",
            color = if (selected) Moss else SoftInk,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    profile: PetProfile,
    pendingCount: Int,
    onNight: () -> Unit,
    onInbox: () -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("早上好", color = SoftInk, fontSize = 14.sp)
                    Text(
                        "昨晚的事，明早再想。",
                        color = Ink,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(shape = CircleShape, color = Color.White) {
                    Text(
                        if (profile.type == "cat") "🐱" else "🐶",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 22.sp,
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0E9)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(258.dp),
                ) {
                    DayPetScene(profile.type)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(20.dp),
                    ) {
                        Text(
                            "${profile.name}正在炖鱼汤",
                            color = Ink,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("给认真睡觉的你补充能量", color = SoftInk, fontSize = 13.sp)
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.86f),
                    ) {
                        Text(
                            "🐟 ${profile.fish} 条",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onInbox),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF1D3)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✉", color = Ink, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "昨夜来信",
                            color = Ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (pendingCount == 0) "昨夜没有留下未处理的念头"
                            else "有 $pendingCount 个念头等你在白天看看",
                            color = SoftInk,
                            fontSize = 13.sp,
                        )
                    }
                    Text("›", color = SoftInk, fontSize = 28.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Ink),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "第 ${profile.journeyDay} / 21 天",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("下一份礼物", color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { profile.journeyDay / 21f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Honey,
                        trackColor = Color.White.copy(alpha = 0.16f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        repeat(5) { index ->
                            Text(
                                if (index < profile.mood) "●" else "○",
                                color = if (index < profile.mood) Coral else Color.White.copy(alpha = 0.35f),
                                fontSize = 19.sp,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("心情很好", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onNight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
            ) {
                Text("进入今晚的睡眠承诺", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun NightScreen(
    profile: PetProfile,
    onCapture: () -> Unit,
    onMorning: () -> Unit,
) {
    val context = LocalContext.current
    var noiseOn by rememberSaveable { mutableStateOf(true) }
    var swipeWake by remember { mutableFloatStateOf(0f) }
    var catReminding by rememberSaveable { mutableStateOf(false) }
    val reminderProgress by animateFloatAsState(
        targetValue = if (catReminding) 1f else swipeWake,
        animationSpec = tween(180),
        label = "cat-turn-reminder",
    )
    val player = remember { RiverSoundPlayer() }

    DisposableEffect(noiseOn) {
        if (noiseOn) player.start() else player.stop()
        onDispose { player.stop() }
    }

    DisposableEffect(Unit) {
        val screenOnReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_ON) {
                    catReminding = true
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            screenOnReceiver,
            IntentFilter(Intent.ACTION_SCREEN_ON),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(screenOnReceiver) }
    }

    LaunchedEffect(catReminding) {
        if (catReminding) {
            delay(3_600)
            catReminding = false
            swipeWake = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night, NightBlue, Color(0xFF315268))))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (!catReminding) {
                            swipeWake = (swipeWake - dragAmount / 620f).coerceIn(0f, 1f)
                        }
                    },
                    onDragEnd = {
                        if (swipeWake >= 0.46f) {
                            catReminding = true
                        } else {
                            swipeWake = 0f
                        }
                    },
                    onDragCancel = { swipeWake = 0f },
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        NightFishingScene(
            type = profile.type,
            breed = profile.breed,
            reminderProgress = reminderProgress,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onMorning) {
                    Text("结束今晚", color = Color.White.copy(alpha = 0.72f))
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { noiseOn = !noiseOn },
                ) {
                    Text(
                        if (noiseOn) "〰 河水声播放中" else "▷ 开启河水声",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                "${profile.name}正在安静钓鱼",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "念头交给我，你继续睡。",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))
            AnimatedVisibility(visible = reminderProgress > 0.08f) {
                Surface(
                    color = Color(0xFFFFF7E7).copy(alpha = 0.94f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        "主人你怎么还不睡呀，\n夜深了，要睡觉啦。",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        color = Ink,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(if (reminderProgress > 0.08f) 14.dp else 0.dp))
            Text(
                "突然想到什么？",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onCapture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7E7)),
                shape = RoundedCornerShape(22.dp),
            ) {
                Text(
                    "按一下，寄存念头",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "记录后会自动回到这片夜色",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun CaptureScreen(
    onSave: (NightNote) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }
    var text by rememberSaveable { mutableStateOf("") }
    var recording by rememberSaveable { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    var recordFailed by rememberSaveable { mutableStateOf(false) }

    fun beginRecording() {
        recordFailed = false
        if (recorder.start()) {
            recording = true
            seconds = 0
        } else {
            recordFailed = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) beginRecording() else recordFailed = true
    }

    LaunchedEffect(recording) {
        while (recording) {
            delay(1_000)
            seconds += 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (recording) recorder.stop() else recorder.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("取消", color = Color.White.copy(alpha = 0.65f))
            }
            Text("寄存一个念头", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(56.dp))
        }

        Spacer(Modifier.height(34.dp))
        Box(
            modifier = Modifier
                .size(112.dp)
                .scale(if (recording) 1.06f else 1f)
                .clip(CircleShape)
                .background(if (recording) Coral else Color.White.copy(alpha = 0.12f))
                .clickable {
                    if (recording) {
                        val path = recorder.stop()
                        recording = false
                        if (path != null) {
                            onSave(
                                NightNote(
                                    text = "一段语音念头",
                                    isVoice = true,
                                    audioPath = path,
                                    durationSeconds = seconds.coerceAtLeast(1),
                                ),
                            )
                        } else {
                            recordFailed = true
                        }
                    } else {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) beginRecording()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (recording) "■" else "●", color = Color.White, fontSize = 38.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (recording) "正在听你说 · ${seconds}s（再按一次保存）" else "轻触开始语音记录",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 14.sp,
        )
        AnimatedVisibility(recordFailed) {
            Text(
                "暂时无法录音，可以先用下面的文字寄存。",
                modifier = Modifier.padding(top = 8.dp),
                color = Honey,
                fontSize = 12.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.12f)))
            Text(
                "或者写一句",
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.12f)))
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(280) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = {
                Text(
                    "例如：明天问一下那个合作的报价……",
                    color = Color.White.copy(alpha = 0.35f),
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            shape = RoundedCornerShape(22.dp),
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { if (text.isNotBlank()) onSave(NightNote(text = text.trim())) },
            enabled = text.isNotBlank() && !recording,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFF7E7),
                contentColor = Ink,
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.White.copy(alpha = 0.3f),
            ),
        ) {
            Text("寄存，然后回去睡觉", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun InboxScreen(
    modifier: Modifier,
    notes: List<NightNote>,
    onToggle: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "昨夜来信",
                color = Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "白天再决定：去做、归档，或者放心地删掉。",
                color = SoftInk,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (notes.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 38.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🌙", fontSize = 42.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("这里还没有来信", color = Ink, fontWeight = FontWeight.Bold)
                        Text("今晚有念头时，只留下一句话就好。", color = SoftInk, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.completed) Color.White.copy(alpha = 0.55f) else Color.White,
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Checkbox(
                            checked = note.completed,
                            onCheckedChange = { onToggle(note.id) },
                        )
                        Column(Modifier.padding(top = 10.dp, end = 8.dp)) {
                            Text(
                                if (note.isVoice) "🎙 ${note.text} · ${note.durationSeconds}s" else note.text,
                                color = if (note.completed) SoftInk else Ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                formatTime(note.createdAt),
                                color = SoftInk.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenScreen(modifier: Modifier, profile: PetProfile) {
    val activeBreed = catBreedOption(profile.breed)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("伙伴小院", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("不是连续打卡，是一起生活过的夜晚。", color = SoftInk, fontSize = 14.sp)
        }
        item {
            GardenPet(
                emoji = if (profile.type == "cat") "🐱" else "🐶",
                title = "${profile.name} · 正在陪伴",
                subtitle = if (profile.type == "cat") {
                    "${activeBreed.name} · 第 ${profile.journeyDay} 天"
                } else {
                    "第 ${profile.journeyDay} 天"
                },
                unlocked = true,
            )
        }
        item {
            Column {
                Text(
                    "猫咪品种图鉴",
                    color = Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "首批收录 ${CatBreedOptions.size} 种常见猫咪",
                    color = SoftInk,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CatBreedOptions) { breed ->
                        GardenBreedCard(
                            breed = breed,
                            selected = profile.type == "cat" && profile.breed == breed.id,
                        )
                    }
                }
            }
        }
        item {
            GardenPet(
                emoji = "🐰",
                title = "月光兔",
                subtitle = "坚持 21 天后可以领养",
                unlocked = false,
            )
        }
        item {
            GardenPet(
                emoji = "🦊",
                title = "晚风狐",
                subtitle = "完成第二段 21 天旅程后解锁",
                unlocked = false,
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D0)),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("下一份礼物", color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("第 21 天：新伙伴或一套夜钓装扮", color = SoftInk, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { profile.journeyDay / 21f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Honey,
                        trackColor = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun GardenBreedCard(
    breed: CatBreedOption,
    selected: Boolean,
) {
    Card(
        modifier = Modifier.width(126.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFF0D0) else Color.White,
        ),
        shape = RoundedCornerShape(20.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, Honey)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(breed.alertRes),
                contentDescription = breed.name,
                modifier = Modifier.size(92.dp),
            )
            Text(
                breed.shortName,
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (selected) "正在陪伴" else "已收录",
                color = if (selected) Moss else SoftInk,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun GardenPet(
    emoji: String,
    title: String,
    subtitle: String,
    unlocked: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (unlocked) 1f else 0.55f),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (unlocked) Color(0xFFEAF2E9) else Color(0xFFF0EEE9)),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (unlocked) emoji else "🔒", fontSize = 34.sp)
            }
            Spacer(Modifier.width(15.dp))
            Column {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SoftInk, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BottomNav(active: Screen, onSelected: (Screen) -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(66.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem("⌂", "今天", active == Screen.HOME) { onSelected(Screen.HOME) }
            NavItem("✉", "来信", active == Screen.INBOX) { onSelected(Screen.INBOX) }
            NavItem("♧", "小院", active == Screen.GARDEN) { onSelected(Screen.GARDEN) }
        }
    }
}

@Composable
private fun NavItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, color = if (selected) Ink else SoftInk, fontSize = 19.sp)
        Text(
            label,
            color = if (selected) Ink else SoftInk,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DayPetScene(type: String) {
    val transition = rememberInfiniteTransition(label = "steam")
    val steam by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "steam-rise",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(Color.White.copy(alpha = 0.55f), radius = w * 0.2f, center = Offset(w * 0.75f, h * 0.3f))
        drawOval(Color(0xFFC6D9C8), topLeft = Offset(w * 0.08f, h * 0.73f), size = Size(w * 0.84f, h * 0.16f))

        val petColor = if (type == "cat") Color(0xFFF5C76B) else Color(0xFFD59C68)
        drawOval(petColor, topLeft = Offset(w * 0.25f, h * 0.43f), size = Size(w * 0.27f, h * 0.33f))
        drawCircle(petColor, radius = w * 0.095f, center = Offset(w * 0.37f, h * 0.44f))
        if (type == "cat") {
            val leftEar = Path().apply {
                moveTo(w * 0.29f, h * 0.40f)
                lineTo(w * 0.31f, h * 0.27f)
                lineTo(w * 0.36f, h * 0.37f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(w * 0.39f, h * 0.37f)
                lineTo(w * 0.45f, h * 0.27f)
                lineTo(w * 0.45f, h * 0.41f)
                close()
            }
            drawPath(leftEar, petColor)
            drawPath(rightEar, petColor)
        } else {
            drawOval(Color(0xFF9E6F4C), Offset(w * 0.26f, h * 0.34f), Size(w * 0.08f, h * 0.17f))
            drawOval(Color(0xFF9E6F4C), Offset(w * 0.42f, h * 0.34f), Size(w * 0.08f, h * 0.17f))
        }
        drawCircle(Ink, radius = 4.dp.toPx(), center = Offset(w * 0.34f, h * 0.44f))
        drawCircle(Ink, radius = 4.dp.toPx(), center = Offset(w * 0.41f, h * 0.44f))

        drawOval(Color(0xFF596B72), Offset(w * 0.49f, h * 0.62f), Size(w * 0.3f, h * 0.13f))
        drawRect(Color(0xFF78909C), Offset(w * 0.52f, h * 0.57f), Size(w * 0.24f, h * 0.12f))
        drawLine(Color(0xFF596B72), Offset(w * 0.49f, h * 0.57f), Offset(w * 0.79f, h * 0.57f), 5.dp.toPx(), StrokeCap.Round)

        repeat(3) { index ->
            val x = w * (0.58f + index * 0.07f)
            val y = h * (0.53f - steam * 0.18f)
            drawArc(
                color = Color.White.copy(alpha = 1f - steam),
                startAngle = 80f,
                sweepAngle = 190f,
                useCenter = false,
                topLeft = Offset(x, y),
                size = Size(18.dp.toPx(), 30.dp.toPx()),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun NightFishingScene(
    type: String,
    breed: String,
    reminderProgress: Float,
) {
    val context = LocalContext.current
    val breedOption = remember(breed) { catBreedOption(breed) }
    val fishingCatImage = remember(context, breedOption.fishingRes) {
        ImageBitmap.imageResource(context.resources, breedOption.fishingRes)
    }
    val reminderCatImage = remember(context, breedOption.alertRes) {
        ImageBitmap.imageResource(context.resources, breedOption.alertRes)
    }
    val transition = rememberInfiniteTransition(label = "night-fishing")
    val waterDrift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "water-drift",
    )
    val fishClock by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fish-clock",
    )
    val breathing by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pet-breathing",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val waterTop = h * 0.56f
        val moon = Offset(w * 0.79f, h * 0.20f)

        drawCircle(
            Color(0xFFFFE9A8).copy(alpha = 0.12f),
            radius = w * 0.18f,
            center = moon,
        )
        drawCircle(Color(0xFFFFE9A8), radius = w * 0.085f, center = moon)
        drawCircle(
            color = NightBlue,
            radius = w * 0.072f,
            center = Offset(moon.x - w * 0.028f, moon.y - w * 0.018f),
        )

        repeat(20) { index ->
            val x = (0.05f + ((index * 43) % 90) / 100f) * w
            val y = (0.06f + ((index * 31) % 40) / 100f) * h
            val twinkle = 0.30f + ((index % 4) * 0.08f)
            drawCircle(
                Color.White.copy(alpha = twinkle),
                radius = (0.8f + index % 3 * 0.45f).dp.toPx(),
                center = Offset(x, y),
            )
        }

        val farHills = Path().apply {
            moveTo(0f, waterTop)
            lineTo(0f, h * 0.47f)
            cubicTo(w * 0.18f, h * 0.37f, w * 0.32f, h * 0.50f, w * 0.47f, h * 0.43f)
            cubicTo(w * 0.63f, h * 0.35f, w * 0.76f, h * 0.49f, w, h * 0.38f)
            lineTo(w, waterTop)
            close()
        }
        drawPath(farHills, Color(0xFF1D3C4B).copy(alpha = 0.82f))

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF173D54), Color(0xFF0B263B)),
                startY = waterTop,
                endY = h,
            ),
            topLeft = Offset(0f, waterTop),
            size = Size(w, h - waterTop),
        )

        repeat(5) { index ->
            val reflectionY = waterTop + (18 + index * 29).dp.toPx()
            val reflectionWidth = w * (0.055f + index * 0.018f)
            drawLine(
                Color(0xFFFFE8A0).copy(alpha = 0.08f - index * 0.008f),
                Offset(moon.x - reflectionWidth + waterDrift * 3.dp.toPx(), reflectionY),
                Offset(moon.x + reflectionWidth + waterDrift * 3.dp.toPx(), reflectionY),
                1.4.dp.toPx(),
                StrokeCap.Round,
            )
        }

        val turnStrength = reminderProgress.coerceIn(0f, 1f)

        repeat(3) { index ->
            val lane = index % 4
            val direction = if (index % 2 == 0) 1f else -1f
            val loop = (fishClock + index * 0.31f) % 1f
            val baseX = if (direction > 0f) {
                -w * 0.10f + loop * w * 1.20f
            } else {
                w * 1.10f - loop * w * 1.20f
            }
            val baseY = waterTop + (0.14f + lane * 0.18f) * (h - waterTop)
            val wave = kotlin.math.sin((fishClock * 6.28f + index) * 1.7f) * 5.dp.toPx()
            val center = Offset(
                x = baseX,
                y = baseY + wave,
            )
            drawNightFish(
                center = center,
                length = (18 + index % 4 * 3).dp.toPx(),
                color = listOf(
                    Color(0xFF9BC8C1),
                    Color(0xFFF2B76D),
                    Color(0xFF79AAB6),
                )[index % 3],
                facingRight = direction > 0f,
                alpha = 0.84f,
            )
        }

        repeat(8) { index ->
            val y = waterTop + index * 24.dp.toPx()
            val inset = if (index % 2 == 0) w * 0.06f else w * 0.18f
            drawLine(
                Color(0xFF9ED0D2).copy(alpha = 0.12f + index % 3 * 0.025f),
                Offset(inset + waterDrift * 12.dp.toPx(), y),
                Offset(w - inset + waterDrift * 12.dp.toPx(), y),
                1.5.dp.toPx(),
                StrokeCap.Round,
            )
        }

        val bank = Path().apply {
            moveTo(0f, h * 0.50f)
            cubicTo(w * 0.13f, h * 0.48f, w * 0.24f, h * 0.52f, w * 0.42f, h * 0.51f)
            lineTo(w * 0.52f, h * 0.60f)
            cubicTo(w * 0.32f, h * 0.61f, w * 0.18f, h * 0.58f, 0f, h * 0.63f)
            close()
        }
        drawPath(bank, Color(0xFF233E35))
        drawPath(
            Path().apply {
                moveTo(0f, h * 0.54f)
                cubicTo(w * 0.16f, h * 0.51f, w * 0.30f, h * 0.57f, w * 0.49f, h * 0.55f)
            },
            Color(0xFF52664D),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
        )

        repeat(5) { index ->
            val reedX = w * (0.04f + index * 0.055f)
            drawLine(
                Color(0xFF789071),
                Offset(reedX, h * 0.57f),
                Offset(reedX + waterDrift * 2.dp.toPx(), h * (0.47f - index % 2 * 0.025f)),
                2.dp.toPx(),
                StrokeCap.Round,
            )
        }

        if (type == "cat") {
            val catWidth = w * 0.44f
            val catHeight = catWidth * fishingCatImage.height / fishingCatImage.width
            val catLeft = w * 0.035f
            val catTop = h * 0.275f - breathing * 1.5.dp.toPx()
            drawOval(
                Color.Black.copy(alpha = 0.17f),
                topLeft = Offset(catLeft + catWidth * 0.08f, catTop + catHeight * 0.92f),
                size = Size(catWidth * 0.84f, catHeight * 0.10f),
            )
            drawImage(
                image = fishingCatImage,
                dstOffset = IntOffset(catLeft.toInt(), catTop.toInt()),
                dstSize = IntSize(catWidth.toInt(), catHeight.toInt()),
                alpha = 1f - turnStrength,
                filterQuality = FilterQuality.High,
            )
            drawImage(
                image = reminderCatImage,
                dstOffset = IntOffset(catLeft.toInt(), catTop.toInt()),
                dstSize = IntSize(catWidth.toInt(), catHeight.toInt()),
                alpha = turnStrength,
                filterQuality = FilterQuality.High,
            )
        } else {
            drawRefinedFishingDog(w, h, breathing)
        }

        val rodAlpha = (1f - turnStrength * 1.35f).coerceIn(0f, 1f)
        val rodStart = Offset(w * 0.38f, h * (0.408f + breathing * 0.002f))
        val rodTip = Offset(w * 0.78f, h * (0.485f + waterDrift * 0.003f))
        drawLine(
            color = Color(0xFFD2A56E).copy(alpha = rodAlpha),
            start = rodStart,
            end = rodTip,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
        val bobber = Offset(w * 0.72f, waterTop + (h - waterTop) * 0.34f)
        drawLine(
            Color(0xFFE7E2D4).copy(alpha = 0.68f * rodAlpha),
            rodTip,
            bobber,
            1.dp.toPx(),
        )
        drawCircle(Color(0xFFF4E8D1).copy(alpha = rodAlpha), radius = 4.dp.toPx(), center = bobber)
        drawCircle(Coral.copy(alpha = rodAlpha), radius = 2.4.dp.toPx(), center = Offset(bobber.x, bobber.y + 1.dp.toPx()))
        repeat(2) { index ->
            val rippleRadius = (12 + index * 10 + waterDrift * 2).dp.toPx()
            drawArc(
                color = Color(0xFFB6D7D2).copy(alpha = (0.24f - index * 0.07f) * rodAlpha),
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(bobber.x - rippleRadius, bobber.y - rippleRadius * 0.32f),
                size = Size(rippleRadius * 2f, rippleRadius * 0.64f),
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }
    }
}

private fun DrawScope.drawNightFish(
    center: Offset,
    length: Float,
    color: Color,
    facingRight: Boolean,
    alpha: Float,
) {
    val direction = if (facingRight) 1f else -1f
    val bodyWidth = length * 0.72f
    val bodyHeight = length * 0.34f
    drawOval(
        color.copy(alpha = alpha),
        topLeft = Offset(center.x - bodyWidth / 2f, center.y - bodyHeight / 2f),
        size = Size(bodyWidth, bodyHeight),
    )
    val tailRootX = center.x - direction * bodyWidth * 0.43f
    val tail = Path().apply {
        moveTo(tailRootX, center.y)
        lineTo(tailRootX - direction * length * 0.34f, center.y - bodyHeight * 0.58f)
        lineTo(tailRootX - direction * length * 0.30f, center.y + bodyHeight * 0.62f)
        close()
    }
    drawPath(tail, color.copy(alpha = alpha * 0.88f))
    drawCircle(
        Color(0xFF102C3A).copy(alpha = alpha),
        radius = length * 0.035f,
        center = Offset(center.x + direction * bodyWidth * 0.22f, center.y - bodyHeight * 0.10f),
    )
}

private fun DrawScope.drawRefinedFishingCat(
    w: Float,
    h: Float,
    breathing: Float,
) {
    val lift = breathing * 1.5.dp.toPx()
    val fur = Color(0xFFF0B95B)
    val furShadow = Color(0xFFD9923E)
    val cream = Color(0xFFFFE5B5)
    val stripe = Color(0xFF9A6237)
    val scarf = Color(0xFF70836A)
    val dark = Color(0xFF2B3134)

    drawOval(
        Color.Black.copy(alpha = 0.16f),
        Offset(w * 0.12f, h * 0.545f),
        Size(w * 0.31f, h * 0.035f),
    )

    val tail = Path().apply {
        moveTo(w * 0.20f, h * 0.52f)
        cubicTo(w * 0.09f, h * 0.49f, w * 0.08f, h * 0.58f, w * 0.18f, h * 0.565f)
        cubicTo(w * 0.24f, h * 0.555f, w * 0.24f, h * 0.53f, w * 0.22f, h * 0.515f)
    }
    drawPath(
        tail,
        furShadow,
        style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round),
    )
    drawPath(
        tail,
        fur,
        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round),
    )

    drawOval(
        furShadow,
        Offset(w * 0.19f, h * 0.405f - lift),
        Size(w * 0.19f, h * 0.16f),
    )
    drawOval(
        fur,
        Offset(w * 0.20f, h * 0.405f - lift),
        Size(w * 0.17f, h * 0.15f),
    )
    drawOval(
        cream.copy(alpha = 0.82f),
        Offset(w * 0.245f, h * 0.445f - lift),
        Size(w * 0.085f, h * 0.105f),
    )

    drawOval(
        furShadow,
        Offset(w * 0.17f, h * 0.515f),
        Size(w * 0.14f, h * 0.048f),
    )
    drawOval(
        cream,
        Offset(w * 0.29f, h * 0.520f),
        Size(w * 0.10f, h * 0.037f),
    )

    val leftEar = Path().apply {
        moveTo(w * 0.205f, h * 0.385f - lift)
        lineTo(w * 0.205f, h * 0.293f - lift)
        lineTo(w * 0.275f, h * 0.353f - lift)
        close()
    }
    val rightEar = Path().apply {
        moveTo(w * 0.300f, h * 0.345f - lift)
        lineTo(w * 0.365f, h * 0.285f - lift)
        lineTo(w * 0.370f, h * 0.387f - lift)
        close()
    }
    drawPath(leftEar, furShadow)
    drawPath(rightEar, furShadow)
    drawPath(
        Path().apply {
            moveTo(w * 0.218f, h * 0.352f - lift)
            lineTo(w * 0.218f, h * 0.318f - lift)
            lineTo(w * 0.248f, h * 0.348f - lift)
            close()
        },
        Color(0xFFE9A48F),
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.326f, h * 0.342f - lift)
            lineTo(w * 0.353f, h * 0.310f - lift)
            lineTo(w * 0.355f, h * 0.352f - lift)
            close()
        },
        Color(0xFFE9A48F),
    )

    drawOval(
        fur,
        Offset(w * 0.195f, h * 0.325f - lift),
        Size(w * 0.19f, h * 0.15f),
    )
    drawOval(
        cream,
        Offset(w * 0.265f, h * 0.400f - lift),
        Size(w * 0.095f, h * 0.055f),
    )

    repeat(3) { index ->
        val stripeX = w * (0.255f + index * 0.031f)
        drawLine(
            stripe.copy(alpha = 0.78f),
            Offset(stripeX, h * 0.334f - lift),
            Offset(stripeX + w * 0.008f, h * 0.357f - lift),
            2.dp.toPx(),
            StrokeCap.Round,
        )
    }
    drawArc(
        dark,
        startAngle = 15f,
        sweepAngle = 145f,
        useCenter = false,
        topLeft = Offset(w * 0.235f, h * 0.372f - lift),
        size = Size(w * 0.040f, h * 0.022f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
    drawArc(
        dark,
        startAngle = 15f,
        sweepAngle = 145f,
        useCenter = false,
        topLeft = Offset(w * 0.315f, h * 0.372f - lift),
        size = Size(w * 0.040f, h * 0.022f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.296f, h * 0.416f - lift)
            lineTo(w * 0.311f, h * 0.416f - lift)
            lineTo(w * 0.304f, h * 0.426f - lift)
            close()
        },
        Color(0xFF9B5B58),
    )
    drawLine(
        dark.copy(alpha = 0.75f),
        Offset(w * 0.304f, h * 0.426f - lift),
        Offset(w * 0.304f, h * 0.436f - lift),
        1.2.dp.toPx(),
    )
    repeat(3) { index ->
        val whiskerY = h * (0.420f + index * 0.010f) - lift
        drawLine(
            cream.copy(alpha = 0.75f),
            Offset(w * 0.274f, whiskerY),
            Offset(w * 0.205f, whiskerY - (index - 1) * 3.dp.toPx()),
            1.dp.toPx(),
        )
        drawLine(
            cream.copy(alpha = 0.75f),
            Offset(w * 0.335f, whiskerY),
            Offset(w * 0.398f, whiskerY - (1 - index) * 3.dp.toPx()),
            1.dp.toPx(),
        )
    }

    drawOval(
        scarf,
        Offset(w * 0.215f, h * 0.447f - lift),
        Size(w * 0.15f, h * 0.031f),
    )
    val scarfTail = Path().apply {
        moveTo(w * 0.235f, h * 0.465f - lift)
        lineTo(w * 0.205f, h * 0.520f - lift)
        lineTo(w * 0.255f, h * 0.505f - lift)
        close()
    }
    drawPath(scarfTail, Color(0xFF596C56))

    drawLine(
        furShadow,
        Offset(w * 0.31f, h * 0.47f - lift),
        Offset(w * 0.365f, h * 0.49f),
        11.dp.toPx(),
        StrokeCap.Round,
    )
    drawCircle(
        cream,
        radius = 6.dp.toPx(),
        center = Offset(w * 0.365f, h * 0.49f),
    )

    repeat(3) { index ->
        val stripeY = h * (0.455f + index * 0.025f)
        drawLine(
            stripe.copy(alpha = 0.56f),
            Offset(w * 0.205f, stripeY),
            Offset(w * 0.235f, stripeY + 3.dp.toPx()),
            2.dp.toPx(),
            StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawRefinedFishingDog(
    w: Float,
    h: Float,
    breathing: Float,
) {
    val lift = breathing * 1.5.dp.toPx()
    val fur = Color(0xFFD89D69)
    val shadow = Color(0xFFAC7048)
    val cream = Color(0xFFF5D6AD)
    val dark = Color(0xFF302D2C)

    drawOval(
        Color.Black.copy(alpha = 0.15f),
        Offset(w * 0.13f, h * 0.54f),
        Size(w * 0.30f, h * 0.035f),
    )
    drawOval(fur, Offset(w * 0.19f, h * 0.405f - lift), Size(w * 0.19f, h * 0.15f))
    drawOval(cream, Offset(w * 0.25f, h * 0.45f - lift), Size(w * 0.08f, h * 0.095f))
    drawOval(shadow, Offset(w * 0.185f, h * 0.335f - lift), Size(w * 0.065f, h * 0.13f))
    drawOval(shadow, Offset(w * 0.34f, h * 0.335f - lift), Size(w * 0.065f, h * 0.13f))
    drawOval(fur, Offset(w * 0.205f, h * 0.325f - lift), Size(w * 0.18f, h * 0.145f))
    drawOval(cream, Offset(w * 0.265f, h * 0.398f - lift), Size(w * 0.10f, h * 0.057f))
    drawCircle(dark, 2.5.dp.toPx(), Offset(w * 0.265f, h * 0.385f - lift))
    drawCircle(dark, 2.5.dp.toPx(), Offset(w * 0.335f, h * 0.385f - lift))
    drawCircle(dark, 3.dp.toPx(), Offset(w * 0.315f, h * 0.423f - lift))
    drawOval(Color(0xFF70836A), Offset(w * 0.215f, h * 0.447f - lift), Size(w * 0.15f, h * 0.031f))
    drawLine(
        shadow,
        Offset(w * 0.31f, h * 0.47f - lift),
        Offset(w * 0.365f, h * 0.49f),
        11.dp.toPx(),
        StrokeCap.Round,
    )
    drawCircle(cream, 6.dp.toPx(), Offset(w * 0.365f, h * 0.49f))
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("M月d日 HH:mm", Locale.CHINA).format(Date(timestamp))
