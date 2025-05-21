package com.tienda.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class SeleccionRolFragment : Fragment() {
    private val rolViewModel: RolViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SeleccionRolScreen { rol ->
                        rolViewModel.seleccionarRol(rol)
                        // Navegar a productos
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ProductosFragment())
                            .commit()
                    }
                }
            }
        }
    }
}

@Composable
fun SeleccionRolScreen(onRolSeleccionado: (RolUsuario) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Selecciona tu rol", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onRolSeleccionado(RolUsuario.CLIENTE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar como Cliente")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRolSeleccionado(RolUsuario.ADMIN) }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar como Administrador")
        }
    }
} 