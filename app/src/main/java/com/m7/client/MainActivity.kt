package com.m7.client

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val M7Bg = Color(0xFF09060F)
private val M7Panel = Color(0xFF12101A)
private val M7Panel2 = Color(0xFF181322)
private val M7Purple = Color(0xFF8B5CF6)
private val M7PurpleBright = Color(0xFFB794F6)
private val M7Text = Color(0xFFF6F0FF)
private val M7Muted = Color(0xFFA79EB5)
private val M7Green = Color(0xFF64D98B)
private val M7Amber = Color(0xFFF2C66D)

private const val PREFS = "m7_client_preferences"
private const val MC_OFFICIAL = "com.mojang.minecraftpe"
private const val MC_LEGACY_M7 = "com.clientm7.mcpe"

private data class ClientModule(
    val id: String,
    val name: String,
    val description: String,
    val category: ModuleCategory,
    val badge: String? = null,
    val sandboxOnly: Boolean = false
)

private enum class ModuleCategory(val title: String) {
    HUD("HUD"), VISUAL("Visual"), PERFORMANCE("Performance"), LABS("M7 Labs")
}

private data class M7Profile(
    val name: String,
    val subtitle: String,
    val tag: String,
    val modules: Set<String>
)

private val modules = listOf(
    ClientModule("fps", "FPS Counter", "Contador compacto de FPS para o HUD M7.", ModuleCategory.HUD),
    ClientModule("coords", "Coordinates", "Coordenadas XYZ em layout otimizado para mobile.", ModuleCategory.HUD),
    ClientModule("direction", "Direction", "Direção e bússola minimalista.", ModuleCategory.HUD),
    ClientModule("armor", "Armor HUD", "Estado visual da armadura e durabilidade.", ModuleCategory.HUD),
    ClientModule("durability", "Durability", "Avisa antes de ferramentas e armadura quebrarem.", ModuleCategory.HUD),
    ClientModule("cps", "CPS Counter", "Mostra a taxa de toques/cliques sem automatizar ações.", ModuleCategory.HUD),
    ClientModule("clock", "Session Clock", "Tempo da sessão atual no canto do HUD.", ModuleCategory.HUD),

    ClientModule("crosshair", "Crosshair Studio", "Perfis de mira e tamanho/espessura configuráveis.", ModuleCategory.VISUAL, "M7"),
    ClientModule("zoom", "Zoom", "Perfil visual de aproximação para uso permitido.", ModuleCategory.VISUAL),
    ClientModule("night", "Night Visibility", "Preset visual por resource pack para melhorar legibilidade.", ModuleCategory.VISUAL),
    ClientModule("minimal", "Minimal HUD", "Remove distrações e prioriza gameplay/gravação.", ModuleCategory.VISUAL),

    ClientModule("low_particles", "Low Particles", "Preset para reduzir elementos visuais pesados.", ModuleCategory.PERFORMANCE),
    ClientModule("clean_ui", "Clean UI", "Interface enxuta para telas pequenas.", ModuleCategory.PERFORMANCE),
    ClientModule("recording", "Recording Mode", "Layout preparado para gravação sem poluir a tela.", ModuleCategory.PERFORMANCE),
    ClientModule("battery", "Battery Saver", "Perfil de launcher para sessões longas no celular.", ModuleCategory.PERFORMANCE),

    ClientModule("xray_lab", "X-Ray", "Módulo reservado ao sandbox/treino local.", ModuleCategory.LABS, "SOLO", true),
    ClientModule("scaffold_lab", "Scaffold", "Módulo reservado ao sandbox/treino local.", ModuleCategory.LABS, "SOLO", true),
    ClientModule("autoclick_lab", "AutoClick", "Módulo de laboratório para treino/testes locais.", ModuleCategory.LABS, "TRAINING", true),
    ClientModule("fastdrop_lab", "FastDrop", "Módulo de laboratório para treino/testes locais.", ModuleCategory.LABS, "SOLO", true)
)

private val profiles = listOf(
    M7Profile(
        "Survival",
        "Exploração limpa e informações úteis.",
        "BALANCED",
        setOf("fps", "coords", "direction", "durability", "clock", "night")
    ),
    M7Profile(
        "PvP",
        "HUD compacto e leitura rápida.",
        "COMPETITIVE",
        setOf("fps", "cps", "armor", "durability", "crosshair", "minimal")
    ),
    M7Profile(
        "Creator",
        "Tela limpa para vídeos e screenshots.",
        "RECORDING",
        setOf("fps", "recording", "clean_ui", "minimal", "crosshair")
    ),
    M7Profile(
        "Battery",
        "Configuração leve para sessões longas.",
        "LOW POWER",
        setOf("fps", "low_particles", "clean_ui", "battery")
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = M7Purple,
                    secondary = M7PurpleBright,
                    background = M7Bg,
                    surface = M7Panel,
                    onPrimary = Color.White,
                    onBackground = M7Text,
                    onSurface = M7Text
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = M7Bg) {
                    M7ClientApp()
                }
            }
        }
    }
}

@Composable
private fun M7ClientApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val moduleState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            modules.forEach { put(it.id, prefs.getBoolean("module_${it.id}", false)) }
        }
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeProfile by remember { mutableStateOf(prefs.getString("active_profile", "Survival") ?: "Survival") }
    var trainingMode by remember { mutableStateOf(prefs.getBoolean("training_mode", false)) }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importIntoMinecraft(context, uri)
    }

    Scaffold(
        containerColor = M7Bg,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0E0A15),
                modifier = Modifier.navigationBarsPadding()
            ) {
                val tabs = listOf("Home", "Profiles", "Modules", "Packs", "Settings")
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { NavGlyph(label, selectedTab == index) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = M7PurpleBright,
                            indicatorColor = M7Purple.copy(alpha = .25f),
                            unselectedIconColor = M7Muted,
                            unselectedTextColor = M7Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF120822), M7Bg, M7Bg),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        ) {
            when (selectedTab) {
                0 -> HomeScreen(context, activeProfile, moduleState.count { it.value }, onPlay = { openMinecraft(context) }, onModules = { selectedTab = 2 })
                1 -> ProfilesScreen(activeProfile) { profile ->
                    activeProfile = profile.name
                    prefs.edit().putString("active_profile", profile.name).apply()
                    modules.forEach { m ->
                        val enabled = profile.modules.contains(m.id)
                        moduleState[m.id] = enabled
                        prefs.edit().putBoolean("module_${m.id}", enabled).apply()
                    }
                    Toast.makeText(context, "Perfil ${profile.name} aplicado", Toast.LENGTH_SHORT).show()
                }
                2 -> ModulesScreen(
                    state = moduleState,
                    trainingMode = trainingMode,
                    onTrainingMode = {
                        trainingMode = it
                        prefs.edit().putBoolean("training_mode", it).apply()
                    },
                    onToggle = { module, enabled ->
                        if (module.sandboxOnly && !trainingMode && enabled) {
                            Toast.makeText(context, "Ative o M7 Training Mode para módulos Labs.", Toast.LENGTH_SHORT).show()
                        } else {
                            moduleState[module.id] = enabled
                            prefs.edit().putBoolean("module_${module.id}", enabled).apply()
                        }
                    }
                )
                3 -> PacksScreen(
                    onImport = { importer.launch(arrayOf("*/*")) },
                    onOpenMinecraft = { openMinecraft(context) }
                )
                else -> SettingsScreen(
                    activeProfile = activeProfile,
                    trainingMode = trainingMode,
                    onExport = { shareConfig(context, activeProfile, trainingMode, moduleState) },
                    onReset = {
                        modules.forEach { moduleState[it.id] = false }
                        trainingMode = false
                        activeProfile = "Survival"
                        prefs.edit().clear().apply()
                        Toast.makeText(context, "M7 Client resetado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun Header(eyebrow: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            M7Logo(46.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(eyebrow.uppercase(), color = M7PurpleBright, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text(title, color = M7Text, fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = M7Muted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun HomeScreen(
    context: Context,
    activeProfile: String,
    enabledModules: Int,
    onPlay: () -> Unit,
    onModules: () -> Unit
) {
    val officialInstalled = remember { isPackageInstalled(context, MC_OFFICIAL) }
    val legacyInstalled = remember { isPackageInstalled(context, MC_LEGACY_M7) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Header("M7 System", "M7 Client", "Bedrock Companion • Mobile-first • v${BuildConfig.VERSION_NAME}")
        }
        item {
            HeroCard(
                installed = officialInstalled || legacyInstalled,
                activeProfile = activeProfile,
                onPlay = onPlay
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("PROFILE", activeProfile, Modifier.weight(1f))
                StatCard("MODULES", enabledModules.toString(), Modifier.weight(1f))
                StatCard("MCPE", if (officialInstalled) "READY" else if (legacyInstalled) "LEGACY" else "OFF", Modifier.weight(1f))
            }
        }
        item {
            SectionTitle("Quick actions", "SEU SETUP EM 1 TOQUE")
            ActionCard("Module Center", "Configure HUD, visual, performance e M7 Labs.", "OPEN", onModules)
            ActionCard("Performance profile", "Use Battery/Creator/PvP sem reconstruir o APK.", activeProfile.uppercase(), {})
            ActionCard("Independent client", "Minecraft não fica dentro do APK do M7.", "LIGHT", {})
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroCard(installed: Boolean, activeProfile: String, onPlay: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(26.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF5A2DA8), Color(0xFF24123F), Color(0xFF15101E))
                    )
                )
                .border(1.dp, M7PurpleBright.copy(alpha = .25f), RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column {
                StatusPill(if (installed) "MINECRAFT DETECTED" else "MINECRAFT NOT FOUND", installed)
                Spacer(Modifier.height(18.dp))
                Text("Seu Minecraft.\nSeu setup M7.", color = Color.White, fontSize = 30.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Perfil ativo: $activeProfile", color = Color(0xFFE2D7F4), fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF26123F)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (installed) "JOGAR MINECRAFT" else "PROCURAR MINECRAFT", fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfilesScreen(activeProfile: String, onApply: (M7Profile) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header("Profiles", "Escolha seu modo", "Troque o conjunto de módulos sem configurar tudo novamente.") }
        items(profiles) { profile ->
            val active = profile.name == activeProfile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onApply(profile) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFF24173A) else M7Panel)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (active) M7Purple else M7Panel2),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(profile.name, color = M7Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(Modifier.width(8.dp))
                            MiniBadge(if (active) "ACTIVE" else profile.tag)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(profile.subtitle, color = M7Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("${profile.modules.size} modules", color = M7PurpleBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("›", color = M7Muted, fontSize = 26.sp)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ModulesScreen(
    state: Map<String, Boolean>,
    trainingMode: Boolean,
    onTrainingMode: (Boolean) -> Unit,
    onToggle: (ClientModule, Boolean) -> Unit
) {
    var category by remember { mutableStateOf(ModuleCategory.HUD) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header("Module Center", "Controle total", "Módulos organizados para tela pequena e configuração rápida.") }
        item {
            CategoryRow(category) { category = it }
        }
        if (category == ModuleCategory.LABS) {
            item {
                TrainingModeCard(trainingMode, onTrainingMode)
            }
        }
        items(modules.filter { it.category == category }) { module ->
            ModuleCard(module, state[module.id] == true, trainingMode, onToggle)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CategoryRow(selected: ModuleCategory, onSelect: (ModuleCategory) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ModuleCategory.entries.forEach { category ->
            val active = category == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) M7Purple else M7Panel)
                    .clickable { onSelect(category) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (category) {
                        ModuleCategory.PERFORMANCE -> "Perf"
                        ModuleCategory.LABS -> "Labs"
                        else -> category.title
                    },
                    color = if (active) Color.White else M7Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TrainingModeCard(enabled: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162B)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("M7 Training Mode", color = M7Text, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    MiniBadge("SANDBOX")
                }
                Spacer(Modifier.height(4.dp))
                Text("Libera configurações Labs somente para ambientes locais/permitidos.", color = M7Muted, fontSize = 11.sp, lineHeight = 16.sp)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = M7Purple)
            )
        }
    }
}

@Composable
private fun ModuleCard(module: ClientModule, enabled: Boolean, trainingMode: Boolean, onToggle: (ClientModule, Boolean) -> Unit) {
    val scale by animateFloatAsState(if (enabled) 1f else .985f, label = "moduleScale")
    val available = !module.sandboxOnly || trainingMode
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .scale(scale)
            .alpha(if (available) 1f else .72f),
        colors = CardDefaults.cardColors(containerColor = if (enabled) Color(0xFF201632) else M7Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (enabled) M7Purple.copy(alpha = .28f) else M7Panel2),
                contentAlignment = Alignment.Center
            ) {
                Text(module.name.take(1), color = if (enabled) M7PurpleBright else M7Muted, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(module.name, color = M7Text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (module.badge != null) {
                        Spacer(Modifier.width(7.dp))
                        MiniBadge(module.badge)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(module.description, color = M7Muted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (module.sandboxOnly) {
                    Spacer(Modifier.height(4.dp))
                    Text(if (trainingMode) "Training mode ready • integration bridge pending" else "Ative Training Mode", color = if (trainingMode) M7Green else M7Amber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle(module, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = M7Purple)
            )
        }
    }
}

@Composable
private fun PacksScreen(onImport: () -> Unit, onOpenMinecraft: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header("Pack Center", "Importe. Organize. Jogue.", "Envie packs, addons, mundos e templates diretamente para o Minecraft instalado.") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = M7Panel),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("IMPORT CENTER", color = M7PurpleBright, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(7.dp))
                    Text(".mcpack  •  .mcaddon  •  .mcworld", color = M7Text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("O M7 usa o seletor de arquivos do Android e entrega o arquivo ao Minecraft por URI segura.", color = M7Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("IMPORTAR ARQUIVO", onImport)
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("ABRIR MINECRAFT", onOpenMinecraft)
                }
            }
        }
        item {
            SectionTitle("Recommended workflow", "SEM APK GIGANTE")
            StepCard("01", "Baixe ou crie seu pack", "Mantenha resource packs e addons separados do client.")
            StepCard("02", "Importe pelo M7", "Escolha o arquivo no Pack Center.")
            StepCard("03", "Abra o Minecraft", "O Minecraft continua independente e atualizável.")
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsScreen(
    activeProfile: String,
    trainingMode: Boolean,
    onExport: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header("Settings", "M7 Client", "Configuração, diagnóstico e identidade do projeto.") }
        item {
            SettingsBlock("CLIENT") {
                SettingLine("Version", BuildConfig.VERSION_NAME)
                SettingLine("Package", BuildConfig.APPLICATION_ID)
                SettingLine("Profile", activeProfile)
                SettingLine("Training Mode", if (trainingMode) "ON" else "OFF")
            }
        }
        item {
            SettingsBlock("TOOLS") {
                SettingsAction("Export configuration", "Compartilhe um snapshot JSON dos seus módulos.", onExport)
                HorizontalDivider(color = Color.White.copy(alpha = .06f))
                SettingsAction("Reset M7 Client", "Volta perfis e módulos ao estado inicial.", onReset)
            }
        }
        item {
            SettingsBlock("ABOUT") {
                SettingLine("Project", "M7 / Marcus")
                SettingLine("Architecture", "Independent Bedrock companion")
                Text("Minecraft permanece instalado separadamente. O M7 Client não inclui arquivos do jogo.", color = M7Muted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 10.dp))
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SettingsBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp),
        colors = CardDefaults.cardColors(containerColor = M7Panel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = M7PurpleBright, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ColumnScope.SettingLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = M7Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = M7Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ColumnScope.SettingsAction(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = M7Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = M7Muted, fontSize = 11.sp)
        }
        Text("›", color = M7PurpleBright, fontSize = 24.sp)
    }
}

@Composable
private fun M7Logo(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(Brush.linearGradient(listOf(Color(0xFF9B6DFF), Color(0xFF5B2AA8)))),
        contentAlignment = Alignment.Center
    ) {
        Text("M7", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * .36f).sp)
    }
}

@Composable
private fun NavGlyph(label: String, selected: Boolean) {
    val glyph = when (label) {
        "Home" -> "M7"
        "Profiles" -> "P"
        "Modules" -> "M"
        "Packs" -> "PK"
        else -> "S"
    }
    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) M7Purple.copy(alpha = .38f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = if (glyph.length > 1) 8.sp else 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatusPill(text: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = .24f))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (ok) M7Green else M7Amber))
        Spacer(Modifier.width(7.dp))
        Text(text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp)
    }
}

@Composable
private fun MiniBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(M7Purple.copy(alpha = .18f))
            .border(1.dp, M7Purple.copy(alpha = .23f), RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, color = M7PurpleBright, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = M7Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = M7Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = M7Text, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionTitle(title: String, eyebrow: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 15.dp)) {
        Text(eyebrow, color = M7PurpleBright, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        Text(title, color = M7Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, badge: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(M7Panel)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(M7Panel2), contentAlignment = Alignment.Center) {
            Text(title.take(1), color = M7PurpleBright, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = M7Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = M7Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        MiniBadge(badge)
    }
}

@Composable
private fun StepCard(number: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(M7Panel)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, color = M7PurpleBright, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.width(38.dp))
        Column {
            Text(title, color = M7Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = M7Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = M7Purple, contentColor = Color.White),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .border(1.dp, M7Purple.copy(alpha = .35f), RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = M7PurpleBright, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

private fun isPackageInstalled(context: Context, packageName: String): Boolean = try {
    context.packageManager.getPackageInfo(packageName, 0)
    true
} catch (_: PackageManager.NameNotFoundException) {
    false
}

private fun openMinecraft(context: Context) {
    val target = when {
        isPackageInstalled(context, MC_OFFICIAL) -> MC_OFFICIAL
        isPackageInstalled(context, MC_LEGACY_M7) -> MC_LEGACY_M7
        else -> null
    }
    if (target == null) {
        Toast.makeText(context, "Minecraft Bedrock não foi encontrado neste aparelho.", Toast.LENGTH_LONG).show()
        return
    }
    val intent = context.packageManager.getLaunchIntentForPackage(target)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Não foi possível abrir o Minecraft.", Toast.LENGTH_LONG).show()
    }
}

private fun importIntoMinecraft(context: Context, uri: Uri) {
    val target = when {
        isPackageInstalled(context, MC_OFFICIAL) -> MC_OFFICIAL
        isPackageInstalled(context, MC_LEGACY_M7) -> MC_LEGACY_M7
        else -> null
    }
    if (target == null) {
        Toast.makeText(context, "Instale o Minecraft Bedrock antes de importar.", Toast.LENGTH_LONG).show()
        return
    }
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {
        // Some providers do not grant persistable permissions; temporary grant below is enough.
    }
    val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        setPackage(target)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
        Toast.makeText(context, "Enviando arquivo para o Minecraft…", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        val fallback = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(fallback, "Abrir com"))
        } catch (_: Exception) {
            Toast.makeText(context, "Nenhum app conseguiu abrir este arquivo.", Toast.LENGTH_LONG).show()
        }
    }
}

private fun shareConfig(
    context: Context,
    activeProfile: String,
    trainingMode: Boolean,
    moduleState: Map<String, Boolean>
) {
    val enabled = modules.filter { moduleState[it.id] == true }.joinToString(",") { "\"${it.id}\"" }
    val json = """{
  "client": "M7 Client",
  "version": "${BuildConfig.VERSION_NAME}",
  "profile": "$activeProfile",
  "trainingMode": $trainingMode,
  "enabledModules": [$enabled]
}""".trimIndent()

    val share = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_SUBJECT, "M7 Client config")
        putExtra(Intent.EXTRA_TEXT, json)
    }
    context.startActivity(Intent.createChooser(share, "Exportar configuração M7"))
}
