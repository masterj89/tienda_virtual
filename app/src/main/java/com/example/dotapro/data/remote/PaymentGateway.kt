package com.example.dotapro.data.remote

import android.os.Handler
import android.os.Looper

/**
 * Contrato de la pasarela de pagos.
 * TODO: crear una implementación real (Wompi, PayU, MercadoPago...) y
 * reemplazar FakePaymentGateway en CheckoutActivity.
 */
interface PaymentGateway {
    fun pay(amount: Int, method: String, onResult: (success: Boolean, reference: String) -> Unit)
}

/** Simula una pasarela: aprueba el pago luego de 1,5 segundos. */
class FakePaymentGateway : PaymentGateway {
    override fun pay(amount: Int, method: String, onResult: (Boolean, String) -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            onResult(true, "DP-" + System.currentTimeMillis().toString().takeLast(8))
        }, 1500)
    }
}
