package com.tienda.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                                rolViewModel.seleccionarRol(rol)
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
    Column(
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