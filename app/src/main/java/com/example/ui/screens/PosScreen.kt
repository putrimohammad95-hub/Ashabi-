package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val totalItemsCount by viewModel.totalItemsCount.collectAsStateWithLifecycle()

    val showPaymentDialog by viewModel.showPaymentDialog.collectAsStateWithLifecycle()
    val paidAmountInput by viewModel.paidAmountInput.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showCartBottomSheet by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var barcodeInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("Semua", "Sembako", "Minuman", "Mie & Makanan", "Camilan / Snack", "Kebutuhan Rumah")

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pencarian dan Tombol Scan Barcode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_input"),
                    placeholder = { Text("Cari barang atau scan...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Barcode Scanner Action Button
                Button(
                    onClick = { showBarcodeDialog = true },
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("scan_barcode_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_category_$category")
                    )
                }
            }

            // Products Grid
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Produk tidak ditemukan" else "Belum ada produk",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val cartItem = cartItems.find { it.product.id == product.id }
                        val inCartQty = cartItem?.quantity ?: 0

                        ProductGridCard(
                            product = product,
                            inCartQty = inCartQty,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Banner (Sticky bar)
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartBottomSheet = true }
                    .testTag("floating_cart_bar"),
                color = EmeraldPrimary,
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = AmberSecondary) {
                                    Text("$totalItemsCount", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Keranjang",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Keranjang Belanja",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(cartTotal),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Button(
                        onClick = { showCartBottomSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("view_cart_button")
                    ) {
                        Text(
                            text = "Buka Kasir",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Cart Modal Bottom Sheet
        if (showCartBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartBottomSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                CartSheetContent(
                    cartItems = cartItems,
                    subtotal = cartSubtotal,
                    discount = discountAmount,
                    total = cartTotal,
                    onIncrease = { viewModel.addToCart(it) },
                    onDecrease = { viewModel.decreaseCartItem(it) },
                    onRemove = { viewModel.removeFromCart(it) },
                    onClear = {
                        viewModel.clearCart()
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showCartBottomSheet = false
                        }
                    },
                    onSetDiscount = { viewModel.setDiscount(it) },
                    onPay = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showCartBottomSheet = false
                            viewModel.openPaymentDialog()
                        }
                    }
                )
            }
        }

        // Payment Dialog
        if (showPaymentDialog) {
            PaymentModalDialog(
                totalAmount = cartTotal,
                paidAmountInput = paidAmountInput,
                paymentMethod = paymentMethod,
                onPaidAmountChange = { viewModel.setPaidAmount(it) },
                onQuickCashClick = { viewModel.setQuickCash(it) },
                onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                onConfirm = { viewModel.processPayment() },
                onDismiss = { viewModel.closePaymentDialog() }
            )
        }

        // Barcode Simulation Dialog
        if (showBarcodeDialog) {
            BarcodeSimulatorDialog(
                sampleProducts = allProducts.take(6),
                currentInput = barcodeInput,
                onInputChange = { barcodeInput = it },
                onScan = { barcode ->
                    viewModel.simulateBarcodeScan(barcode)
                    showBarcodeDialog = false
                    barcodeInput = ""
                },
                onDismiss = { showBarcodeDialog = false }
            )
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    inCartQty: Int,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category & Stock Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.category,
                        color = EmeraldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Stock indicator badge
                val stockBg = when {
                    product.stock <= 0 -> Color(0xFFFFEBEE)
                    product.isLowStock -> Color(0xFFFFF3E0)
                    else -> Color(0xFFF1F8E9)
                }
                val stockText = when {
                    product.stock <= 0 -> Color(0xFFC62828)
                    product.isLowStock -> Color(0xFFE65100)
                    else -> Color(0xFF2E7D32)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(stockBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (product.stock <= 0) "Habis" else "Stok: ${product.stock}",
                        color = stockText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama Produk
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1E201E)
            )

            // Barcode kecil
            if (product.barcode.isNotBlank()) {
                Text(
                    text = "SKU: ${product.barcode}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Harga dan Tombol Tambah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CurrencyUtils.formatRupiah(product.sellingPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "/${product.unit}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (product.stock > 0) {
                    Button(
                        onClick = onAddToCart,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("add_to_cart_${product.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inCartQty > 0) AmberSecondary else EmeraldPrimary
                        )
                    ) {
                        if (inCartQty > 0) {
                            Text(
                                text = "$inCartQty",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Habis",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    total: Double,
    onIncrease: (Product) -> Unit,
    onDecrease: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onClear: () -> Unit,
    onSetDiscount: (Double) -> Unit,
    onPay: () -> Unit
) {
    var discountInput by remember { mutableStateOf(if (discount > 0) discount.toLong().toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("cart_sheet_content")
    ) {
        // Header Keranjang
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keranjang Kasir Ashabi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onClear,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("clear_cart_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kosongkan", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daftar Item Keranjang
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(240.dp)
        ) {
            items(cartItems, key = { it.product.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${CurrencyUtils.formatRupiah(item.product.sellingPrice)} / ${item.product.unit}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Qty Controls (- Qty +)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onDecrease(item.product) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = EmeraldPrimary)
                        }

                        Text(
                            text = "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { onIncrease(item.product) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = EmeraldPrimary)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = CurrencyUtils.formatRupiah(item.subtotal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
                Divider(color = Color(0xFFEEEEEE))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Diskon Cepat
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Diskon Belanja (Rp):", fontSize = 13.sp, color = Color.DarkGray)
            OutlinedTextField(
                value = discountInput,
                onValueChange = {
                    discountInput = it.filter { ch -> ch.isDigit() }
                    val d = discountInput.toDoubleOrNull() ?: 0.0
                    onSetDiscount(d)
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(48.dp)
                    .testTag("discount_input"),
                placeholder = { Text("0", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider()
        Spacer(modifier = Modifier.height(10.dp))

        // Total Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", color = Color.Gray, fontSize = 13.sp)
            Text(CurrencyUtils.formatRupiah(subtotal), fontSize = 13.sp)
        }

        if (discount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Diskon", color = Color.Red, fontSize = 13.sp)
                Text("-${CurrencyUtils.formatRupiah(discount)}", color = Color.Red, fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL AKHIR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                CurrencyUtils.formatRupiah(total),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = EmeraldPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Bayar
        Button(
            onClick = onPay,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("checkout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Payment, contentDescription = "Bayar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lanjut ke Pembayaran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PaymentModalDialog(
    totalAmount: Double,
    paidAmountInput: String,
    paymentMethod: String,
    onPaidAmountChange: (String) -> Unit,
    onQuickCashClick: (Double) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val paidDouble = paidAmountInput.toDoubleOrNull() ?: 0.0
    val change = paidDouble - totalAmount
    val isEnough = paidDouble >= totalAmount

    val quickDenominations = listOf(
        totalAmount, // Uang Pas
        10000.0,
        20000.0,
        50000.0,
        100000.0,
        200000.0
    ).filter { it >= totalAmount || it == totalAmount }.distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pembayaran Kasir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Ashabi Waroeng",
                    color = EmeraldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Total Tagihan Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Tagihan", fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            text = CurrencyUtils.formatRupiah(totalAmount),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pilihan Metode Bayar
                Text("Metode Pembayaran:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Tunai", "QRIS", "Transfer").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onPaymentMethodChange(method) }
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = { onPaymentMethodChange(method) }
                            )
                            Text(method, fontSize = 13.sp)
                        }
                    }
                }

                if (paymentMethod == "Tunai") {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortcut Nominal Uang
                    Text("Pilihan Cepat Nominal:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickDenominations) { denom ->
                            val label = if (denom == totalAmount) "Uang Pas" else CurrencyUtils.formatRupiah(denom)
                            OutlinedButton(
                                onClick = { onQuickCashClick(denom) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Uang Diterima
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = onPaidAmountChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_input_field"),
                        label = { Text("Uang Tunai Diterima (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Informasi Kembalian
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isEnough) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEnough) "Kembalian:" else "Uang Kurang:",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEnough) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(if (isEnough) change else (totalAmount - paidDouble)),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isEnough) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pembayaran non-tunai ($paymentMethod) otomatis tercatat lunas tanpa kembalian.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isEnough || paymentMethod != "Tunai",
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Konfirmasi & Struk")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun BarcodeSimulatorDialog(
    sampleProducts: List<Product>,
    currentInput: String,
    onInputChange: (String) -> Unit,
    onScan: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulasi Scan Barcode", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ketik barcode atau pilih salah satu produk contoh di bawah untuk mensimulasikan scan barcode kasir:",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_scanner_input"),
                    placeholder = { Text("Contoh: 89999990001") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Barcode Cepat (Klik untuk Scan):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                sampleProducts.filter { it.barcode.isNotBlank() }.forEach { p ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onScan(p.barcode) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.name, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            Text(p.barcode, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = EmeraldPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (currentInput.isNotBlank()) onScan(currentInput) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Scan Kode Ini")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
