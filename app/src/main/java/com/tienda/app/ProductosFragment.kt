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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.FragmentManager
import androidx.compose.material3.Icon

class ProductosFragment : Fragment() {
    private val carritoViewModel: CarritoViewModel by activityViewModels()
    private val rolViewModel: RolViewModel by activityViewModels()
    private val productosViewModel: ProductosViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val rol by rolViewModel.rol.collectAsState()
                    val productos by productosViewModel.productos.collectAsState()
                    ProductosLista(
                        productos = productos,
                        onAgregarProducto = { productosViewModel.agregarProducto(it) },
                        onEliminarProducto = { productosViewModel.eliminarProducto(it) },
                        onEditarPrecio = { id, nuevoPrecio -> productosViewModel.editarPrecio(id, nuevoPrecio) },
                        carritoViewModel = carritoViewModel,
                        onVerCarrito = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, CarritoFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onCerrarSesion = {
                            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, SeleccionRolFragment())
                                .commit()
                        },
                        esAdmin = rol == RolUsuario.ADMIN
                    )
                }
            }
        }
    }
}

@Composable
fun ProductosLista(
    productos: List<Producto>,
    onAgregarProducto: (Producto) -> Unit,
    onEliminarProducto: (Producto) -> Unit,
    onEditarPrecio: (Int, Double) -> Unit,
    carritoViewModel: CarritoViewModel,
    onVerCarrito: () -> Unit,
    onCerrarSesion: () -> Unit,
    esAdmin: Boolean
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Productos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVerCarrito, modifier = Modifier.fillMaxWidth()) {
            Text("Ver carrito")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
        if (esAdmin) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar producto")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(productos) { producto ->
                ProductoItem(
                    producto = producto,
                    onAgregar = { carritoViewModel.agregarProducto(producto) },
                    esAdmin = esAdmin,
                    onEliminar = { productoAEliminar = producto },
                    onEditarPrecio = { showEditDialog = producto }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Diálogo para agregar producto
    if (showAddDialog) {
        var nombre by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar producto") },
            text = {
                Column {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val id = (productos.maxOfOrNull { it.id } ?: 0) + 1
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    onAgregarProducto(Producto(id, nombre, precioDouble, descripcion))
                    showAddDialog = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para editar precio
    showEditDialog?.let { producto ->
        var nuevoPrecio by remember { mutableStateOf(producto.precio.toString()) }
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Editar precio") },
            text = {
                OutlinedTextField(value = nuevoPrecio, onValueChange = { nuevoPrecio = it }, label = { Text("Nuevo precio") })
            },
            confirmButton = {
                Button(onClick = {
                    onEditarPrecio(producto.id, nuevoPrecio.toDoubleOrNull() ?: producto.precio)
                    showEditDialog = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de confirmación para eliminar
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que deseas eliminar '${producto.nombre}'?") },
            confirmButton = {
                Button(onClick = {
                    onEliminarProducto(producto)
                    productoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                Button(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ProductoItem(
    producto: Producto,
    onAgregar: () -> Unit,
    esAdmin: Boolean,
    onEliminar: () -> Unit,
    onEditarPrecio: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("$${"%.2f".format(producto.precio)}", style = MaterialTheme.typography.bodyLarge)
                Text(producto.descripcion, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = onAgregar) {
                        Text("Agregar al carrito")
                    }
                    if (esAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onEditarPrecio) {
                            Text("Editar precio")
                        }
                    }
                }
            }
            if (esAdmin) {
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar producto")
                }
            }
        }
    }
} 