package com.example.dotapro.data.model

enum class Role(val label: String) {
    BUYER("Comprador"),
    SELLER("Vendedor"),
    ADMIN("Admin")
}

enum class ProductStatus(val label: String) {
    ACTIVE("Activo"),
    PENDING("Pendiente"),
    INACTIVE("Inactivo")
}

data class User(
    val id: Int,
    var name: String,
    var email: String,
    var role: Role,
    var phone: String = "",
    var address: String = "",
    var active: Boolean = true,
    var password: String = "123456"
)

data class Product(
    val id: Int,
    var name: String,
    var category: String,          // "Uniformes", "Calzado" o "EPP"
    var price: Int,                // COP
    var stock: Int,
    var emoji: String,             // marcador de posición de la imagen
    var bgColor: String,
    var sellerId: Int,
    var sellerName: String,
    var description: String,
    var sizes: List<String>,
    var status: ProductStatus,
    var warehouse: String = "",
    var imageName: String = ""     // nombre del archivo en res/drawable (sin extensión). Vacío = usa el emoji
)

data class CartItem(
    val product: Product,
    var size: String,
    var quantity: Int
)

data class Order(
    val id: Int,
    val description: String,
    val status: String,            // "Pagado" | "Enviado"
    val total: Int,
    val sellerId: Int,
    val buyerId: Int
)
