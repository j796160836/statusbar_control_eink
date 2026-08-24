package com.johnny.statusbar_control_eink

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.johnny.statusbar_control_eink.audio.RingerModeController
import com.johnny.statusbar_control_eink.audio.RingerToggleResult
import com.johnny.statusbar_control_eink.audio.SystemAudioBroadcasts
import com.johnny.statusbar_control_eink.audio.VolumeController
import com.johnny.statusbar_control_eink.audio.VolumeStream
import com.johnny.statusbar_control_eink.notification.EinkNotificationBuilder
import com.johnny.statusbar_control_eink.notification.NotificationLayoutStyle
import com.johnny.statusbar_control_eink.notification.StatusBarControlService
import com.johnny.statusbar_control_eink.prefs.SettingsPrefs
import com.johnny.statusbar_control_eink.screen.ScreenTimeoutController
import com.johnny.statusbar_control_eink.screen.ScreenTimeoutResult
import com.johnny.statusbar_control_eink.ui.components.EinkOutlinedSlider
import com.johnny.statusbar_control_eink.ui.components.EinkRadioOption
import com.johnny.statusbar_control_eink.ui.components.EinkToggleSwitch
import com.johnny.statusbar_control_eink.ui.theme.Statusbar_control_einkTheme

class MainActivity : ComponentActivity() {

    private lateinit var volumeController: VolumeController
    private lateinit var ringerModeController: RingerModeController
    private lateinit var screenTimeoutController: ScreenTimeoutController
    private lateinit var prefs: SettingsPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        volumeController = VolumeController(this)
        ringerModeController = RingerModeController(this)
        screenTimeoutController = ScreenTimeoutController(this)
        prefs = SettingsPrefs(this)

        setContent {
            Statusbar_control_einkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Dashboard(
                        modifier = Modifier.padding(innerPadding),
                        volumeController = volumeController,
                        ringerModeController = ringerModeController,
                        screenTimeoutController = screenTimeoutController,
                        prefs = prefs
                    )
                }
            }
        }
    }
}

@Composable
private fun Dashboard(
    modifier: Modifier = Modifier,
    volumeController: VolumeController,
    ringerModeController: RingerModeController,
    screenTimeoutController: ScreenTimeoutController,
    prefs: SettingsPrefs
) {
    val context = LocalContext.current
    var refreshTick by remember { mutableIntStateOf(0) }

    // Reflect changes made outside this screen (hardware volume rocker, tiles,
    // the persistent notification's own buttons).
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                refreshTick++
            }
        }
        val filter = IntentFilter().apply {
            addAction(SystemAudioBroadcasts.VOLUME_CHANGED_ACTION)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    var notificationEnabled by remember { mutableStateOf(prefs.notificationEnabled) }
    var resumeOnBoot by remember { mutableStateOf(prefs.resumeOnBoot) }
    var permissionDeniedMessage by remember { mutableStateOf(false) }
    var notificationLayoutStyle by remember { mutableStateOf(prefs.notificationLayoutStyle) }
    var neverLockScreen by remember { mutableStateOf(screenTimeoutController.isNeverLock()) }
    var writeSettingsPermissionDeniedMessage by remember { mutableStateOf(false) }

    fun setNotificationLayoutStyle(style: NotificationLayoutStyle) {
        notificationLayoutStyle = style
        prefs.notificationLayoutStyle = style
        if (notificationEnabled) EinkNotificationBuilder.refresh(context)
    }

    fun setNeverLockScreen(enabled: Boolean) {
        when (screenTimeoutController.setNeverLock(enabled, prefs)) {
            is ScreenTimeoutResult.Success -> {
                neverLockScreen = enabled
                writeSettingsPermissionDeniedMessage = false
            }
            is ScreenTimeoutResult.NeedsWriteSettingsPermission -> {
                writeSettingsPermissionDeniedMessage = true
                context.startActivity(
                    screenTimeoutController.writeSettingsPermissionIntent()
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notificationEnabled = true
            prefs.notificationEnabled = true
            StatusBarControlService.start(context)
            permissionDeniedMessage = false
        } else {
            notificationEnabled = false
            prefs.notificationEnabled = false
            permissionDeniedMessage = true
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        if (enabled) {
            val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            val alreadyGranted = !needsRuntimePermission || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                notificationEnabled = true
                prefs.notificationEnabled = true
                permissionDeniedMessage = false
                StatusBarControlService.start(context)
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            notificationEnabled = false
            prefs.notificationEnabled = false
            StatusBarControlService.stop(context)
        }
    }

    fun setResumeOnBoot(enabled: Boolean) {
        resumeOnBoot = enabled
        prefs.resumeOnBoot = enabled
    }

    val mediaRange = remember {
        volumeController.getMinVolume(VolumeStream.MEDIA)..volumeController.getMaxVolume(VolumeStream.MEDIA)
    }
    val ringRange = remember {
        volumeController.getMinVolume(VolumeStream.RING)..volumeController.getMaxVolume(VolumeStream.RING)
    }

    var mediaValue by remember { mutableIntStateOf(volumeController.getVolume(VolumeStream.MEDIA)) }
    var ringValue by remember { mutableIntStateOf(volumeController.getVolume(VolumeStream.RING)) }
    var isVibrate by remember { mutableStateOf(ringerModeController.isVibrate()) }

    // Resync from external changes (hardware rocker, tiles, notification buttons).
    LaunchedEffect(refreshTick) {
        mediaValue = volumeController.getVolume(VolumeStream.MEDIA)
        ringValue = volumeController.getVolume(VolumeStream.RING)
        isVibrate = ringerModeController.isVibrate()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            fontWeight = FontWeight.Bold
        )

        Text(text = stringResource(R.string.dashboard_media_volume), fontWeight = FontWeight.Bold)
        EinkOutlinedSlider(
            value = mediaValue,
            valueRange = mediaRange,
            onValueChange = {
                mediaValue = it
                volumeController.setVolume(VolumeStream.MEDIA, it)
            }
        )

        Text(text = stringResource(R.string.dashboard_ring_volume), fontWeight = FontWeight.Bold)
        EinkOutlinedSlider(
            value = ringValue,
            valueRange = ringRange,
            onValueChange = {
                ringValue = it
                volumeController.setVolume(VolumeStream.RING, it)
            }
        )

        Text(text = stringResource(R.string.dashboard_ringer_mode), fontWeight = FontWeight.Bold)
        EinkToggleSwitch(
            isOn = isVibrate,
            onLabel = stringResource(R.string.state_vibrate),
            offLabel = stringResource(R.string.state_normal),
            onToggle = {
                when (ringerModeController.toggle()) {
                    is RingerToggleResult.Success -> isVibrate = ringerModeController.isVibrate()
                    is RingerToggleResult.NeedsNotificationPolicyAccess -> {
                        context.startActivity(
                            ringerModeController.policyAccessSettingsIntent()
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
        )

        EinkToggleSwitch(
            isOn = notificationEnabled,
            onLabel = stringResource(R.string.dashboard_persistent_notification),
            offLabel = "Off",
            onToggle = { setNotificationEnabled(!notificationEnabled) }
        )

        if (permissionDeniedMessage) {
            Text(text = stringResource(R.string.needs_notification_policy_access))
        }

        if (notificationEnabled) {
            EinkToggleSwitch(
                isOn = resumeOnBoot,
                onLabel = stringResource(R.string.dashboard_resume_on_boot),
                offLabel = "Off",
                onToggle = { setResumeOnBoot(!resumeOnBoot) }
            )
        }

        Text(text = stringResource(R.string.dashboard_notification_style), fontWeight = FontWeight.Bold)
        NotificationLayoutStyle.entries.forEach { style ->
            EinkRadioOption(
                selected = notificationLayoutStyle == style,
                label = stringResource(style.displayNameRes),
                onClick = { setNotificationLayoutStyle(style) }
            )
        }

        Text(text = stringResource(R.string.dashboard_screen_lock_section), fontWeight = FontWeight.Bold)
        EinkToggleSwitch(
            isOn = neverLockScreen,
            onLabel = stringResource(R.string.dashboard_screen_lock_never),
            offLabel = stringResource(R.string.dashboard_screen_lock_normal),
            onToggle = { setNeverLockScreen(!neverLockScreen) }
        )

        if (writeSettingsPermissionDeniedMessage) {
            Text(text = stringResource(R.string.needs_write_settings_permission))
        }
    }
}
