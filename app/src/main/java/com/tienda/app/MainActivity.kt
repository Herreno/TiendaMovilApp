package com.tienda.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tienda.app.ui.theme.TiendaMovilAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.window.Dialog
import android.widget.Button
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Mostrar SeleccionRolFragment al iniciar
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SeleccionRolFragment())
                .commit()
        }

        enableEdgeToEdge()
    }

    private fun fetchLocation(onLocationResult: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationResult("Permiso de ubicación no concedido")
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onLocationResult("Lat: ${it.latitude}, Lon: ${it.longitude}")
                } ?: onLocationResult("Ubicación no disponible")
            }
            .addOnFailureListener {
                onLocationResult("Error obteniendo ubicación")
            }
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permiso concedido, refrescar la UI
            setContent {
                TiendaMovilAppTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        var currentLocation by remember { mutableStateOf("Ubicación no disponible") }
                        LaunchedEffect(Unit) {
                            fetchLocation { loc ->
                                currentLocation = loc
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Greeting(name = "Android")
                            Text(
                                text = currentLocation,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }) {
                                Text("Cerrar sesión")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TiendaMovilAppTheme {
        Greeting("Android")
    }
}

@Composable
fun MapaUbicacion(lat: Double, lon: Double) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lon), 15f)
    }
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = LatLng(lat, lon)),
            title = "Tu ubicación"
        )
    }
}

@Composable
fun MainScreen(context: Context) {
    var showDialog by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("Obteniendo ubicación...") }
    var latLon by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val permissionState = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation(context, fusedLocationClient) {
                locationText = it
                latLon = parseLatLon(it)
            }
        } else {
            locationText = "Permiso denegado"
            latLon = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocation(context, fusedLocationClient) {
                    locationText = it
                    latLon = parseLatLon(it)
                }
                showDialog = true
            } else {
                permissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }) {
            Text("Ver Ubicación")
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Coordenadas:")
                    Spacer(Modifier.height(10.dp))
                    Text(locationText)
                    latLon?.let { (lat, lon) ->
                        Spacer(Modifier.height(16.dp))
                        MapaUbicacion(lat, lon)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { showDialog = false }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

fun parseLatLon(locationText: String): Pair<Double, Double>? {
    return if (locationText.startsWith("Lat:")) {
        val latLon = locationText
            .removePrefix("Lat: ")
            .split(", Lon: ")
            .map { it.toDoubleOrNull() }
        if (latLon.size == 2 && latLon[0] != null && latLon[1] != null) {
            Pair(latLon[0]!!, latLon[1]!!)
        } else null
    } else null
}

fun fetchLocation(
    context: Context,
    client: FusedLocationProviderClient,
    onResult: (String) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onResult("Permiso no concedido")
        return
    }

    // Crear un LocationRequest para obtener una sola actualización rápida
    val locationRequest = LocationRequest.Builder(1000)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdates(1)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                onResult("Lat: ${location.latitude}, Lon: ${location.longitude}")
            } else {
                onResult("Ubicación no disponible")
            }
            // Detener actualizaciones después de la primera
            client.removeLocationUpdates(this)
        }
    }

    client.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}