package com.example.dotapro.ui.buyer

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dotapro.R
import com.example.dotapro.data.remote.FakePaymentGateway
import com.example.dotapro.data.remote.PaymentGateway
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityCheckoutBinding
import com.example.dotapro.util.LocationHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toCop
import com.example.dotapro.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckoutActivity : AppCompatActivity() {

    private lateinit var b: ActivityCheckoutBinding

    // TODO: reemplazar por la implementación real de la pasarela (Wompi, PayU...)
    private val gateway: PaymentGateway = FakePaymentGateway()

    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) useLocation() else toast("Permiso de ubicación denegado")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        if (AppRepository.cart.isEmpty()) {
            toast("Tu carrito está vacío")
            finish()
            return
        }
        b = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setup(this, "Pago", showBack = true)

        b.tvAddress.text = user.address.ifBlank { "Toca \"Usar mi ubicación\"" }
        b.tvTotal.text = AppRepository.total().toCop()
        b.tvItemsInfo.text = "${AppRepository.cartCount()} productos · envío incluido"

        b.tvUseLocation.setOnClickListener {
            if (LocationHelper.hasPermission(this)) useLocation()
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        b.btnPay.setOnClickListener { pay() }
    }

    private fun useLocation() {
        LocationHelper.fetchAddress(this) { address ->
            if (address == null) toast("No se pudo obtener la ubicación. Activa el GPS")
            else b.tvAddress.text = address
        }
    }

    private fun pay() {
        val method = when (b.rgPayment.checkedRadioButtonId) {
            R.id.rbPse -> "PSE"
            R.id.rbNequi -> "Nequi"
            else -> "Tarjeta"
        }
        val buyer = requireUser() ?: return
        val total = AppRepository.total()

        b.btnPay.isEnabled = false
        b.btnPay.text = "Procesando..."

        gateway.pay(total, method) { success, reference ->
            if (success) {
                AppRepository.placeOrder(buyer)
                MaterialAlertDialogBuilder(this)
                    .setTitle("¡Pago exitoso!")
                    .setMessage("Referencia: $reference\nTotal: ${total.toCop()}\nMétodo: $method")
                    .setCancelable(false)
                    .setPositiveButton("Volver al inicio") { _, _ ->
                        startActivity(
                            Intent(this, HomeActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                        finish()
                    }
                    .show()
            } else {
                b.btnPay.isEnabled = true
                b.btnPay.text = "Pagar ahora"
                toast("El pago fue rechazado. Intenta de nuevo")
            }
        }
    }
}
