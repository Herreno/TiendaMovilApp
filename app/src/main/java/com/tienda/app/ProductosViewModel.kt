package com.tienda.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductosViewModel : ViewModel() {
    private val _productos = MutableStateFlow(
        listOf(
            Producto(1, "Camiseta", 19.99, "Camiseta 100% algodón"),
            Producto(2, "Pantalón", 29.99, "Pantalón de mezclilla"),
            Producto(3, "Zapatos", 49.99, "Zapatos de cuero"),
            Producto(4, "Gorra", 9.99, "Gorra deportiva")
        )
    )
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    fun agregarProducto(producto: Producto) {
        _productos.value = _productos.value + producto
    }

    fun eliminarProducto(producto: Producto) {
        _productos.value = _productos.value.filter { it.id != producto.id }
    }

    fun editarPrecio(productoId: Int, nuevoPrecio: Double) {
        _productos.value = _productos.value.map {
            if (it.id == productoId) it.copy(precio = nuevoPrecio) else it
        }
    }
} 