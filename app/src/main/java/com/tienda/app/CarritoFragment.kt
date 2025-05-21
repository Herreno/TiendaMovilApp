package com.tienda.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.compose.ui.window.Dialog

class CarritoFragment : Fragment() {
    private val carritoViewModel: CarritoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CarritoScreen(carritoViewModel)
                }
            }
        }
    }
}

@Composable
fun CarritoScreen(carritoViewModel: CarritoViewModel) {
    val productos by carritoViewModel.carrito.collectAsState()
    val total = productos.sumOf { it.precio }
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showOrderAccepted by remember { mutableStateOf(false) }
    var showUbicacion by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Carrito de compras", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(productos) { producto ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${producto.nombre} - $${"%.2f".format(producto.precio)}")
                    Button(onClick = { carritoViewModel.quitarProducto(producto) }) {
                        Text("Quitar")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Total: $${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { showConfirmDialog = true }) {
                Text("Finalizar compra")
            }
            IconButton(onClick = { showUbicacion = true }) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ver ubicación")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Volver a productos (popBackStack)
            val activity = (context as? androidx.fragment.app.FragmentActivity)
            activity?.supportFragmentManager?.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a productos")
        }
    }

    // Diálogo de confirmación de compra
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar compra") },
            text = {
                Text("¿Deseas confirmar la compra?\nEstás adquiriendo ${productos.size} productos y el precio total sería $${"%.2f".format(total)}")
            },
            confirmButton = {
                Button(onClick = {
                    carritoViewModel.limpiarCarrito()
                    showConfirmDialog = false
                    showOrderAccepted = true
                }) { Text("Aceptar") }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de orden aceptada
    if (showOrderAccepted) {
        AlertDialog(
            onDismissRequest = { showOrderAccepted = false },
            title = { Text("Orden aceptada") },
            text = { Text("¡Tu compra ha sido realizada con éxito!") },
            confirmButton = {
                Button(onClick = { showOrderAccepted = false }) { Text("OK") }
            }
        )
    }

    // Modal de ubicación
    if (showUbicacion) {
        UbicacionDialog(context = context, onDismiss = { showUbicacion = false })
    }
}

@Composable
fun UbicacionDialog(context: android.content.Context, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            UbicacionScreen(context = context, onClose = onDismiss)
        }
    }
}

@Composable
fun UbicacionScreen(context: android.content.Context, onClose: () -> Unit) {
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Obteniendo ubicación...") }
    var latLon by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val permissionState = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
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
    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation(context, fusedLocationClient) {
                locationText = it
                latLon = parseLatLon(it)
            }
        } else {
            permissionState.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ubicación actual:")
        Spacer(Modifier.height(10.dp))
        Text(locationText)
        latLon?.let { (lat, lon) ->
            Spacer(Modifier.height(16.dp))
            MapaUbicacion(lat, lon)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClose) { Text("Cerrar") }
    }
} 