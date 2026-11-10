package com.example.dotapro.data.repository

import com.example.dotapro.data.model.CartItem
import com.example.dotapro.data.model.Order
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.model.ProductStatus
import com.example.dotapro.data.model.Role
import com.example.dotapro.data.model.User
import kotlin.math.roundToInt

/**
 * Repositorio EN MEMORIA con datos de ejemplo (los datos se pierden al cerrar la app).
 * TODO: reemplazar por Room / API REST / Firebase manteniendo estas mismas funciones.
 */
object AppRepository {

    private var nextUserId = 7
    private var nextProductId = 9
    private var nextOrderId = 1043

    // ------------------------------------------------------------ Usuarios
    private val users = mutableListOf(
        User(1, "Laura Gómez", "laura@correo.com", Role.BUYER, "+57 300 123 4567", "Cra 15 # 80-20, Bogotá"),
        User(2, "TextilPro S.A.S.", "ventas@textilpro.co", Role.SELLER, "+57 310 555 0101", "Cra 30 # 10-15, Bogotá"),
        User(3, "Carlos Ruiz", "carlos@correo.com", Role.BUYER, active = false),
        User(4, "Andrea Mora", "admin@dotapro.co", Role.ADMIN),
        User(5, "ProSeguridad", "ventas@proseguridad.co", Role.SELLER),
        User(6, "MedUniformes", "ventas@meduniformes.co", Role.SELLER)
    )

    fun login(email: String, password: String): User? =
        users.firstOrNull { it.email.equals(email, true) && it.password == password }

    fun findByEmail(email: String): User? = users.firstOrNull { it.email.equals(email, true) }

    fun searchUsers(query: String = "", role: Role? = null): List<User> = users.filter {
        (role == null || it.role == role) &&
            (query.isBlank() || it.name.contains(query, true) || it.email.contains(query, true))
    }

    /** Devuelve null si el correo ya existe. */
    fun addUser(name: String, email: String, role: Role, password: String): User? {
        if (findByEmail(email) != null) return null
        val user = User(nextUserId++, name, email, role, password = password)
        users.add(user)
        return user
    }

    fun updateUser(id: Int, name: String, email: String, role: Role): Boolean {
        val user = users.firstOrNull { it.id == id } ?: return false
        if (users.any { it.id != id && it.email.equals(email, true) }) return false
        user.name = name; user.email = email; user.role = role
        return true
    }

    fun updateProfile(user: User, name: String, phone: String, address: String) {
        user.name = name; user.phone = phone; user.address = address
    }

    fun setUserActive(id: Int, active: Boolean) {
        users.firstOrNull { it.id == id }?.active = active
    }

    fun deleteUser(id: Int) {
        users.removeAll { it.id == id }
    }

    // ------------------------------------------------------------ Productos
    private val products = mutableListOf(
        Product(1, "Camisa industrial manga larga", "Uniformes", 59900, 24, "👕", "#DCE6F5", 2, "TextilPro S.A.S.",
            "Tela drill 100% algodón, cierre reforzado, bolsillos frontales y reflectivos.",
            listOf("S", "M", "L", "XL"), ProductStatus.ACTIVE, "Cra 30 # 10-15"),
        Product(2, "Botas dieléctricas", "Calzado", 129900, 12, "🥾", "#FDE6CF", 2, "TextilPro S.A.S.",
            "Bota de seguridad dieléctrica con puntera reforzada y suela antideslizante.",
            listOf("39", "40", "41", "42", "43"), ProductStatus.ACTIVE, "Cra 30 # 10-15"),
        Product(3, "Casco de seguridad", "EPP", 42500, 3, "⛑️", "#FDE6CF", 2, "TextilPro S.A.S.",
            "Casco industrial de polietileno con suspensión de 4 puntos y ajuste de matraca.",
            listOf("Única"), ProductStatus.ACTIVE, "Cra 30 # 10-15"),
        Product(4, "Bata hospitalaria", "Uniformes", 68000, 40, "🥼", "#D9F0E3", 2, "TextilPro S.A.S.",
            "Bata antifluido para personal de salud, lavable y de secado rápido.",
            listOf("S", "M", "L", "XL"), ProductStatus.ACTIVE, "Cra 30 # 10-15"),
        Product(5, "Guantes nitrilo x100", "EPP", 38000, 200, "🧤", "#D9F0E3", 5, "ProSeguridad",
            "Caja de 100 guantes de nitrilo sin polvo, uso médico e industrial.",
            listOf("S", "M", "L"), ProductStatus.PENDING, "Calle 80 # 20-10"),
        Product(6, "Overol antifluido", "Uniformes", 85000, 35, "👕", "#DCE6F5", 6, "MedUniformes",
            "Overol de una pieza con tela antifluido y costuras selladas.",
            listOf("M", "L", "XL"), ProductStatus.PENDING, "Av. 68 # 45-12"),
        Product(7, "Chaleco reflectivo", "EPP", 29000, 60, "🦺", "#FDE6CF", 2, "TextilPro S.A.S.",
            "Chaleco de alta visibilidad con cintas reflectivas certificadas.",
            listOf("M", "L", "XL"), ProductStatus.PENDING, "Cra 30 # 10-15"),
        Product(8, "Zapato antideslizante", "Calzado", 95000, 18, "👞", "#FDE6CF", 6, "MedUniformes",
            "Zapato clínico antideslizante, liviano y fácil de limpiar.",
            listOf("38", "39", "40", "41"), ProductStatus.INACTIVE, "Av. 68 # 45-12")
    )

    /**
     * Imagen local de cada producto: nombre del archivo en res/drawable (sin extensión).
     * Si el archivo no existe, la app muestra el emoji del producto.
     */
    private val defaultImages = mapOf(
        1 to "camisa_industrial",
        2 to "botas_dielectricas",
        3 to "casco_seguridad",
        4 to "bata_hospitalaria",
        5 to "guantes_nitrilo",
        6 to "overol_antifluido",
        7 to "chaleco_reflectivo",
        8 to "zapato_antideslizante"
    )

    init {
        products.forEach { it.imageName = defaultImages[it.id] ?: "" }
    }

    fun activeProducts(query: String = "", category: String = "Todos"): List<Product> = products.filter {
        it.status == ProductStatus.ACTIVE &&
            (category == "Todos" || it.category == category) &&
            (query.isBlank() || it.name.contains(query, true) || it.category.contains(query, true))
    }

    fun productById(id: Int): Product? = products.firstOrNull { it.id == id }

    fun productsBySeller(sellerId: Int, query: String = ""): List<Product> = products.filter {
        it.sellerId == sellerId && (query.isBlank() || it.name.contains(query, true))
    }

    fun productsByStatus(status: ProductStatus): List<Product> = products.filter { it.status == status }

    fun newProductId(): Int = nextProductId++

    /** Crea el producto si no existe, o lo reemplaza si ya existe. */
    fun saveProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) products[index] = product else products.add(product)
    }

    fun deleteProduct(id: Int) {
        products.removeAll { it.id == id }
        cart.removeAll { it.product.id == id }
    }

    fun setProductStatus(id: Int, status: ProductStatus) {
        productById(id)?.status = status
    }

    // ------------------------------------------------------------ Carrito
    val cart = mutableListOf<CartItem>()

    fun addToCart(product: Product, size: String, quantity: Int) {
        val existing = cart.firstOrNull { it.product.id == product.id && it.size == size }
        if (existing != null) {
            existing.quantity = (existing.quantity + quantity).coerceAtMost(product.stock)
        } else {
            cart.add(CartItem(product, size, quantity.coerceAtMost(product.stock)))
        }
    }

    fun removeFromCart(item: CartItem) { cart.remove(item) }
    fun clearCart() { cart.clear() }
    fun cartCount(): Int = cart.sumOf { it.quantity }

    fun subtotal(): Int = cart.sumOf { it.product.price * it.quantity }
    fun iva(): Int = (subtotal() * 0.19).roundToInt()
    fun shipping(): Int = if (cart.isEmpty()) 0 else 12000
    fun total(): Int = subtotal() + iva() + shipping()

    // ------------------------------------------------------------ Pedidos
    private val orders = mutableListOf(
        Order(1042, "Camisa industrial x2", "Pagado", 119800, 2, 1),
        Order(1041, "Botas dieléctricas", "Enviado", 129900, 2, 1),
        Order(1040, "Casco de seguridad x4", "Pagado", 170000, 2, 1),
        Order(1039, "Bata hospitalaria x10", "Enviado", 680000, 2, 1),
        Order(1038, "Camisa industrial x20", "Pagado", 1198000, 2, 1),
        Order(1037, "Botas dieléctricas x8", "Enviado", 1039200, 2, 1)
    )

    fun placeOrder(buyer: User) {
        cart.forEach { item ->
            item.product.stock = (item.product.stock - item.quantity).coerceAtLeast(0)
            orders.add(
                Order(nextOrderId++, "${item.product.name} x${item.quantity}", "Pagado",
                    item.product.price * item.quantity, item.product.sellerId, buyer.id)
            )
        }
        cart.clear()
    }

    fun recentOrders(sellerId: Int, limit: Int = 3): List<Order> =
        orders.filter { it.sellerId == sellerId }.sortedByDescending { it.id }.take(limit)

    // ------------------------------------------------------------ Estadísticas
    fun sellerSales(sellerId: Int): Int = orders.filter { it.sellerId == sellerId }.sumOf { it.total }
    fun sellerOrderCount(sellerId: Int): Int = orders.count { it.sellerId == sellerId }
    fun lowStockCount(sellerId: Int): Int = productsBySeller(sellerId).count { it.stock <= 5 }
    fun weeklySales(): List<Int> = listOf(12, 19, 15, 26, 22, 42)       // datos de ejemplo

    fun userCount(): Int = users.size
    fun sellerCount(): Int = users.count { it.role == Role.SELLER }
    fun pendingCount(): Int = products.count { it.status == ProductStatus.PENDING }

    /** Porcentaje (0-100) de ventas por categoría. Datos de ejemplo. */
    fun salesByCategory(): LinkedHashMap<String, Int> =
        linkedMapOf("Uniformes" to 80, "Calzado" to 55, "EPP" to 40, "Hospitalario" to 30)
}
