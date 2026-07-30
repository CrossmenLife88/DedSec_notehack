package com.example.dedsec.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private val fakeFilePaths = listOf(
    "/usr/local/dedsec/exploit_ctos_v3.py",
    "/home/root/tools/backdoor_gen.sh",
    "/opt/net/scan_targets.cfg",
    "/var/tmp/session_9f21ac.log",
    "/usr/share/dedsec/payload_stage2.bin",
    "/root/.hidden/keychain_dump.db"
)

private val fakeTerminalBodies = listOf(
    """
    [*] Loading module: net.intercept.core
    [*] Target subnet resolved: 192.168.0.0/24
    [*] Handshake protocol: READY
    [*] Encryption layer: AES-256 bypass loaded
    [ OK ] Dependencies satisfied
    """.trimIndent(),
    """
    import socket, sys
    from ctos import proxy_chain

    def init_session(target):
        sock = socket.socket()
        sock.connect((target, 31337))
        return proxy_chain.wrap(sock)

    # status: idle, awaiting trigger
    """.trimIndent(),
    """
    root@dedsec:~${'$'} ls -la /opt/payloads/
    -rwxr-xr-x  stage1_loader.bin
    -rwxr-xr-x  stage2_inject.bin
    -rw-r--r--  keymap_override.cfg
    root@dedsec:~${'$'} _
    """.trimIndent(),
    """
    [NETWORK SCANNER]
    Interface: wlan0
    Nodes discovered: 14
    Vulnerable: 3
    Status: STANDBY
    """.trimIndent(),
    """
    ctOS bridge handshake...
    Cert chain: unverified (expected)
    Fallback: local emulation mode
    Ready: TRUE
    """.trimIndent(),
    """
    def dump_keys(path="/root/.hidden/keychain_dump.db"):
        # placeholder, no real op
        pass

    # last run: never
    """.trimIndent()
)

private val fakeHackWords = listOf(
    "scanning port", "handshake ack", "buffer loaded", "route trace complete",
    "checksum verified", "injecting stub", "decrypting block", "socket bound",
    "auth token cycling", "memory map updated", "payload staged", "trace hidden",
    "node response 200", "cache flushed", "key rotation ok", "firmware probe",
    "shell spawned", "proxy chained", "kernel hook set", "syscall intercepted",
    "packet crafted", "dns spoof armed", "session forked", "entropy pool refilled",
    "certificate pinned", "heap sprayed", "stack canary bypassed", "ASLR mapped",
    "binary patched", "signature stripped", "log rotated", "config parsed",
    "thread spawned", "lock acquired", "queue drained", "mutex released",
    "index rebuilt", "cluster synced", "node elected", "shard balanced",
    "token refreshed", "vault unsealed", "policy evaluated", "rule matched",
    "anomaly flagged", "baseline updated", "snapshot taken", "rollback ready",
    "watchdog armed", "beacon sent"
)

private val narrativeLines = listOf(
    "Connecting to mirror.dedsec.io...",
    "Resolving dependencies...",
    "Authenticating handshake...",
    "Welcome to DedSec Network.",
    "Bypassing firewall rules...",
    "Mounting virtual filesystem...",
    "Spoofing MAC address...",
    "Establishing encrypted tunnel...",
    "Verifying signature chain...",
    "Loading kernel module: ctos_bridge",
    "Cleaning cache directory...",
    "Rebuilding local index...",
    "Synchronizing clock with node cluster...",
    "Reading manifest.json...",
    "Parsing configuration...",
    "Applying patch set 04...",
    "Restarting service watchdog...",
    "Finalizing session token...",
    "ctOS handshake accepted.",
    "Uplink stable. Latency: 12ms",
    "Rotating relay nodes...",
    "Access level: elevated (simulated)",
    "Local cache purged.",
    "Fingerprint masked.",
    "Injecting shellcode stub...",
    "Verifying checksum... OK",
    "Requesting elevated token...",
    "Deploying relay agent...",
    "Listening on port 4444...",
    "Session ID assigned: 0x7fa3",
    "Compressing payload...",
    "Flushing DNS cache...",
    "Rewriting routing table...",
    "Handshake retry 1/3...",
    "Trust chain validated.",
    "Disabling logging subsystem...",
    "Cloning remote repo (mirror)...",
    "Building local index...",
    "Patch applied successfully."
)

private val fetchTargets = listOf(
    "stage2_payload.bin", "keymap_override.cfg", "proxy_chain.pkg",
    "net_scan_module.so", "backdoor_gen.sh", "session_token.dat",
    "exploit_ctos_v3.py", "firmware_dump.img", "cert_bundle.pem",
    "handshake_proto.json", "route_table.cfg", "relay_agent.bin",
    "shellcode_stub.o", "wordlist_v2.txt", "socket_wrapper.so",
    "auth_bypass.patch", "kernel_hook.ko", "trace_masker.py",
    "vault_unlock.key", "cache_index.db"
)

@Composable
fun HackVisualScreen(onClose: () -> Unit) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val original = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = original ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var isHacking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHacking = true
                        tryAwaitRelease()
                        isHacking = false
                    }
                )
            }
    ) {
        if (isHacking) {
            HackingRunningView()
        } else {
            ScatteredTerminalWindows()
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(12.dp)
                .border(1.dp, Color.White)
                .clickable { onClose() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("[X]", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ScatteredTerminalWindows() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = maxWidth
        val maxH = maxHeight

        fakeTerminalBodies.forEachIndexed { index, body ->
            val windowWidth = remember(index) { Random.nextInt(220, 320).dp }
            val availableW = (maxW - windowWidth).value.toInt().coerceAtLeast(1)
            val availableH = (maxH - 140.dp).value.toInt().coerceAtLeast(1)
            val offsetX = remember(index) { Random.nextInt(0, availableW).dp }
            val offsetY = remember(index) { Random.nextInt(0, availableH).dp }

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .width(windowWidth)
            ) {
                FakeTerminalWindow(
                    title = fakeFilePaths.getOrElse(index) { "/tmp/session.log" },
                    body = body
                )
            }
        }
    }
}

@Composable
private fun FakeTerminalWindow(title: String, body: String) {
    Column(modifier = Modifier.border(1.dp, Color.White).background(Color(0xFF050505))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Row {
                Text("—", color = Color.White, modifier = Modifier.padding(horizontal = 5.dp))
                Text("□", color = Color.White, modifier = Modifier.padding(horizontal = 5.dp))
                Text("×", color = Color.White, modifier = Modifier.padding(horizontal = 5.dp))
            }
        }
        Text(body, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
    }
}

@Composable
private fun HackingRunningView() {
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = false
        delay(Random.nextLong(500, 1100))
        contentVisible = true
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp)) {
        if (contentVisible) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Зона у фронтальной камеры — терминал-текст
                TerminalNarrativePanel(modifier = Modifier.weight(1f).fillMaxHeight())

                Spacer(Modifier.width(16.dp))

                // Средняя зона — делится по вертикали: загрузки сверху, дерево снизу
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ProgressBarsPanel(modifier = Modifier.weight(1f).fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    TreeTerminalPanel(modifier = Modifier.weight(1f).fillMaxWidth())
                }

                Spacer(Modifier.width(16.dp))

                // Зона у динамиков/зарядки — увеличенная IP-сетка
                NetworkDiagramPanel(modifier = Modifier.weight(1.3f).fillMaxHeight())
            }
        }
    }
}

// Дерево, средняя скорость
@Composable
private fun TreeTerminalPanel(modifier: Modifier = Modifier) {
    var lines by remember { mutableStateOf(List(20) { "" }) }
    LaunchedEffect(Unit) {
        while (true) {
            val depth = Random.nextInt(0, 6)
            val branch = when (Random.nextInt(0, 3)) {
                0 -> "├─ "
                1 -> "└─ "
                else -> "│  "
            }
            val indent = "  ".repeat(depth) + branch
            val word = fakeHackWords.random()
            val hex = (0..3).joinToString("") { Random.nextInt(0, 16).toString(16) }
            lines = (lines.drop(1) + "$indent$word $hex")
            delay(Random.nextLong(30, 65))
        }
    }
    Column(modifier = modifier) {
        lines.forEach { Text(it, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp, maxLines = 1) }
    }
}

// Только загрузки: имя файла на одной строке, полоса # на следующей
@Composable
private fun ProgressBarsPanel(modifier: Modifier = Modifier) {
    val barCount = 4
    data class DownloadState(var name: String, var bar: String)
    val downloads = remember { List(barCount) { mutableStateOf(DownloadState("", "")) } }

    downloads.forEach { state ->
        LaunchedEffect(state) {
            while (true) {
                val target = fetchTargets.random()
                val fast = Random.nextBoolean()
                val stepDelay = if (fast) 10L..25L else 50L..120L
                var value = 0
                val total = 20
                while (value < total) {
                    delay(Random.nextLong(stepDelay.first, stepDelay.last))
                    value++
                    val bar = "#".repeat(value) + "-".repeat(total - value)
                    state.value = DownloadState(target, "[$bar] ${(value * 100 / total)}%")
                }
                state.value = DownloadState(target, "done.")
                delay(Random.nextLong(400, 1200))
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        downloads.forEach { state ->
            Text(state.value.name, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp, maxLines = 1)
            Text(state.value.bar, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp, maxLines = 1)
        }
    }
}

// Только бегущий текст терминала, самая быстрая скорость
@Composable
private fun TerminalNarrativePanel(modifier: Modifier = Modifier) {
    var narrative by remember { mutableStateOf(List(45) { "" }) }

    LaunchedEffect(Unit) {
        while (true) {
            narrative = (narrative.drop(1) + narrativeLines.random())
            delay(Random.nextLong(2, 8))
        }
    }

    Column(modifier = modifier) {
        narrative.forEach {
            Text(it, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NetworkDiagramPanel(modifier: Modifier = Modifier) {
    var nodeIps by remember { mutableStateOf(generateFakeIps(6)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1000, 2200))
            nodeIps = generateFakeIps(Random.nextInt(4, 8))
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val centerX = maxWidth / 2
        val centerY = maxHeight / 2
        val radius = (minOf(maxWidth, maxHeight) / 2) - 24.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = (minOf(size.width, size.height) / 2f) - 44f
            nodeIps.indices.forEach { index ->
                val angle = 2 * Math.PI * index / nodeIps.size
                val x = cx + r * cos(angle).toFloat()
                val y = cy + r * sin(angle).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(cx, cy),
                    end = Offset(x, y),
                    strokeWidth = 1.5f
                )
            }
        }

        Text(
            "HOST\n192.168.0.14",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        nodeIps.forEachIndexed { index, ip ->
            val angle = 2 * Math.PI * index / nodeIps.size
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            Text(
                ip,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                modifier = Modifier.offset(x - 26.dp, y - 6.dp)
            )
        }
    }
}

private fun generateFakeIps(count: Int): List<String> =
    List(count) { "192.168.${Random.nextInt(0, 255)}.${Random.nextInt(1, 254)}" }