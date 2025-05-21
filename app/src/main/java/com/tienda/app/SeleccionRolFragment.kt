package com.tienda.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

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
                    var mostrarCamara = remember { mutableStateOf(false) }
                    if (mostrarCamara.value) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { mostrarCamara.value = false },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Volver")
                            }
                            PantallaCamaraInApp()
                        }
                    } else {
                        SeleccionRolScreen(
                            onRolSeleccionado = { rol ->
                                rolViewModel.seleccionarRol(rol)
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, ProductosFragment())
                                    .commit()
                            },
                            onAbrirCamara = { mostrarCamara.value = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeleccionRolScreen(
    onRolSeleccionado: (RolUsuario) -> Unit,
    onAbrirCamara: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAbrirCamara, modifier = Modifier.fillMaxWidth()) {
            Text("Abrir cámara")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            // Cerrar sesión de Firebase y Google y lanzar LoginActivity
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Cambiar de usuario")
        }
    }
} 