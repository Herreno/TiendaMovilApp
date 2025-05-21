package com.tienda.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarritoViewModel : ViewModel() {
    private val _carrito = MutableStateFlow<List<Producto>>(emptyList())
    val carrito: StateFlow<List<Producto>> = _carrito.asStateFlow()

    fun agregarProducto(producto: Producto) {
        _carrito.value = _carrito.value + producto
    }

    fun quitarProducto(producto: Producto) {
        _carrito.value = _carrito.value - producto
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }
} 