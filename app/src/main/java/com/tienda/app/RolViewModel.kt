package com.tienda.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RolUsuario { CLIENTE, ADMIN }

class RolViewModel : ViewModel() {
    private val _rol = MutableStateFlow<RolUsuario?>(null)
    val rol: StateFlow<RolUsuario?> = _rol.asStateFlow()

    fun seleccionarRol(rol: RolUsuario) {
        _rol.value = rol
    }

    fun cerrarSesion() {
        _rol.value = null
    }
} 