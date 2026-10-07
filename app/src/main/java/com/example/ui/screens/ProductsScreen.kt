package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PosViewModel

@Composable
fun ProductsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("Semua", "Sembako", "Minuman", "Mie & Makanan", "Camilan / Snack", "Kebutuhan Rumah")

    val filteredProducts = allProducts.filter { product ->
        val matchQuery = searchQuery.isBlank() ||
            product.name.contains(searchQuery, ignoreCase = true) ||
            product.barcode.contains(searchQuery, ignoreCase = true)
        val matchCategory = selectedCategory == "Semua" || product.category.equals(selectedCategory, ignoreCase = true)
        matchQuery && matchCategory
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("product_manager_search"),
                placeholder = { Text("Cari produk atau SKU...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Ringkasan Jumlah Barang & Low Stock
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color(0xFFF1F8E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: ${allProducts.size} Produk Katalog",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    val lowCount = allProducts.count { it.isLowStock }
                    if (lowCount > 0) {
                        Text(
                            text = "$lowCount Stok Menipis",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // List Produk
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductManagementCard(
                        product = product,
                        onEdit = {
                            productToEdit = product
                            showAddEditDialog = true
                        },
                        onDelete = { productToDelete = product },
                        onQuickAddStock = { qty -> viewModel.quickAddStock(product.id, qty) }
                    )
                }
            }
        }

        // FAB Tambah Produk Baru
        FloatingActionButton(
            onClick = {
                productToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = EmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Produk Baru")
        }

        // Add / Edit Dialog
        if (showAddEditDialog) {
            ProductFormDialog(
                product = productToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { updatedProduct ->
                    viewModel.saveProduct(updatedProduct)
                    showAddEditDialog = false
                }
            )
        }

        // Delete Confirmation Dialog
        productToDelete?.let { prod ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
                title = { Text("Hapus Produk?") },
                text = { Text("Apakah Anda yakin ingin menghapus '${prod.name}' dari database toko Ashabi Waroeng?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(prod)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { productToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductManagementCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickAddStock: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manage_product_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Info & Kategori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(product.category, fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (product.barcode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = product.barcode,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nama Produk
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Harga & Laba
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Harga Modal", fontSize = 11.sp, color = Color.Gray)
                    Text(CurrencyUtils.formatRupiah(product.costPrice), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Column {
                    Text("Harga Jual", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        CurrencyUtils.formatRupiah(product.sellingPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                Column {
                    val margin = product.sellingPrice - product.costPrice
                    Text("Laba/Untung", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        CurrencyUtils.formatRupiah(margin),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Sisa Stok", fontSize = 11.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val stockColor = when {
                            product.stock <= 0 -> Color.Red
                            product.isLowStock -> AmberSecondary
                            else -> EmeraldPrimary
                        }
                        Text(
                            text = "${product.stock} ${product.unit}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = stockColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stock Actions (+5, +10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text("Tambah Stok Cepat:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = { onQuickAddStock(5) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = { onQuickAddStock(10) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val isEdit = product != null

    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Sembako") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }
    var costPrice by remember { mutableStateOf(if (product != null) product.costPrice.toLong().toString() else "") }
    var sellingPrice by remember { mutableStateOf(if (product != null) product.sellingPrice.toLong().toString() else "") }
    var stock by remember { mutableStateOf(if (product != null) product.stock.toString() else "10") }
    var minStock by remember { mutableStateOf(if (product != null) product.minStock.toString() else "5") }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Sembako", "Minuman", "Mie & Makanan", "Camilan / Snack", "Kebutuhan Rumah", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Produk" else "Tambah Produk Baru",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nama Produk
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk / Barang") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )

                // Barcode / SKU
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode / SKU") },
                    modifier = Modifier.fillMaxWidth().testTag("product_barcode_input"),
                    singleLine = true
                )

                // Kategori Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Satuan
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Satuan (pcs, bks, kg, botol, dll)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Harga Modal & Harga Jual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Modal") },
                        modifier = Modifier.weight(1f).testTag("product_cost_price_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Jual") },
                        modifier = Modifier.weight(1f).testTag("product_selling_price_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Stok & Min Stok
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Stok") },
                        modifier = Modifier.weight(1f).testTag("product_stock_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min. Stok Peringatan") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                    val cPrice = costPrice.toDoubleOrNull() ?: 0.0
                    val sCount = stock.toIntOrNull() ?: 0
                    val mCount = minStock.toIntOrNull() ?: 5

                    val newProd = Product(
                        id = product?.id ?: 0L,
                        name = name.trim(),
                        barcode = barcode.trim(),
                        category = category,
                        unit = unit.trim().ifEmpty { "pcs" },
                        costPrice = cPrice,
                        sellingPrice = sPrice,
                        stock = sCount,
                        minStock = mCount
                    )
                    onSave(newProd)
                },
                enabled = name.isNotBlank() && sellingPrice.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Tambah Produk")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
