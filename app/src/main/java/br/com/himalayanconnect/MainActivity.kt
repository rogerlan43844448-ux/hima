package br.com.himalayanconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

private val Background = Color(0xFF101214)
private val SurfaceDark = Color(0xFF1B1F22)
private val Orange = Color(0xFFFF6A00)
private val Muted = Color(0xFF9AA3A9)
private val White = Color(0xFFF4F5F6)

class MainActivity : ComponentActivity(), LocationListener, SensorEventListener {
    private var currentLocation by mutableStateOf<Location?>(null)
    private var heading by mutableFloatStateOf(0f)
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Background, surface = SurfaceDark, primary = Orange,
                onBackground = White, onSurface = White
            )) {
                HimalayanApp(
                    location = currentLocation,
                    heading = heading,
                    onRequestLocation = { requestLocation() }
                )
            }
        }
    }

    private fun requestLocation() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1500L, 1f, this, Looper.getMainLooper()
            )
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { currentLocation = it }
        } catch (_: SecurityException) { }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        if (::locationManager.isInitialized) locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) { currentLocation = location }
    @Deprecated("Deprecated in Android")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val matrix = FloatArray(9)
            val orientation = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(matrix, event.values)
            SensorManager.getOrientation(matrix, orientation)
            heading = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

private enum class Tab { HOME, NAV, TRIP, MEDIA }

@Composable
private fun HimalayanApp(location: Location?, heading: Float, onRequestLocation: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.HOME) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) onRequestLocation()
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark, contentColor = White) {
                NavItem("Início", Icons.Default.Home, tab == Tab.HOME) { tab = Tab.HOME }
                NavItem("Mapa", Icons.Default.Directions, tab == Tab.NAV) { tab = Tab.NAV }
                NavItem("Viagem", Icons.Default.Route, tab == Tab.TRIP) { tab = Tab.TRIP }
                NavItem("Música", Icons.Default.MusicNote, tab == Tab.MEDIA) { tab = Tab.MEDIA }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Header()
            when (tab) {
                Tab.HOME -> HomeScreen(location, heading) {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
                Tab.NAV -> NavigationScreen(location, heading)
                Tab.TRIP -> TripScreen(location, heading)
                Tab.MEDIA -> MediaScreen()
            }
            Text(
                "HC 0.1.0 • MODO DE DEMONSTRAÇÃO",
                color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                    selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected, onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Orange, selectedTextColor = Orange,
            indicatorColor = Color(0xFF35251B), unselectedIconColor = Muted,
            unselectedTextColor = Muted
        )
    )
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("HIMALAYAN", color = White, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("CONNECT", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        }
        Box(Modifier.size(42.dp).border(1.dp, Orange, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Explore, contentDescription = "Bússola", tint = Orange)
        }
    }
}

@Composable
private fun HomeScreen(location: Location?, heading: Float, onEnableGps: () -> Unit) {
    Text("CENTRAL DE PILOTAGEM", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard("VELOCIDADE GPS", location?.let { "${it.speed.times(3.6f).toInt()}" } ?: "--", "km/h", Modifier.weight(1f))
        MetricCard("DIREÇÃO", heading.toInt().toString(), "graus", Modifier.weight(1f))
    }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("CONEXÃO COM O TRIPPER", color = White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(Orange, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("Aguardando validação junto à motocicleta", color = Muted, fontSize = 12.sp)
            }
            Text("O módulo de comunicação será ativado após identificarmos o protocolo do seu painel.",
                color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("LOCALIZAÇÃO", color = White, fontWeight = FontWeight.Bold)
            Text(
                location?.let { "Lat ${"%.5f".format(it.latitude)}  •  Lon ${"%.5f".format(it.longitude)}" }
                    ?: "GPS ainda não ativado",
                color = Muted, fontSize = 12.sp
            )
            Button(onClick = onEnableGps, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
                Text("ATIVAR GPS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text("Desenvolvido para uso particular • Royal Enfield Himalayan 450",
        color = Muted, fontSize = 10.sp)
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.width(5.dp))
                Text(unit, color = Orange, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun NavigationScreen(location: Location?, heading: Float) {
    Text("NAVEGAÇÃO", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(360.dp)) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            // Placeholder cartográfico: não representa ruas reais.
            Canvas(Modifier.fillMaxSize()) {
                val step = 54f
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(Color(0xFF30383D), Offset(x * step, 0f), Offset(x * step, size.height), 2f)
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(Color(0xFF30383D), Offset(0f, y * step), Offset(size.width, y * step), 2f)
                }
                val c = Offset(size.width * .5f, size.height * .55f)
                drawLine(Orange, c, Offset(c.x + sin(Math.toRadians(heading.toDouble())).toFloat() * 70f,
                    c.y - cos(Math.toRadians(heading.toDouble())).toFloat() * 70f), 7f, cap = StrokeCap.Round)
                drawCircle(Orange, 11f, c)
            }
            Column(Modifier.align(Alignment.TopStart)) {
                Text("MAPA DE TESTE", color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Sem rota ativa", color = White, fontSize = 14.sp)
            }
            Text(
                location?.let { "GPS: %.4f, %.4f".format(it.latitude, it.longitude) } ?: "Aguardando GPS",
                color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
    Text("A visualização atual é ilustrativa. O mapa rodoviário e o cálculo de rotas serão integrados em uma próxima etapa.",
        color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
private fun TripScreen(location: Location?, heading: Float) {
    Text("DADOS DA VIAGEM", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
    MetricCard("VELOCIDADE GPS", location?.let { (it.speed * 3.6f).toInt().toString() } ?: "--", "km/h")
    MetricCard("ALTITUDE GPS", location?.let { it.altitude.toInt().toString() } ?: "--", "metros")
    MetricCard("RUMO", heading.toInt().toString(), "graus")
    Text("Os dados dependem dos sensores e da qualidade do sinal GPS do celular.",
        color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
private fun MediaScreen() {
    Text("MULTIMÍDIA", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Orange,
                modifier = Modifier.size(48.dp))
            Text("CONTROLES DE MÍDIA", color = White, fontWeight = FontWeight.Bold)
            Text("A integração com os controles de reprodução do Android será adicionada após a validação da interface e da conexão com o painel.",
                color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
