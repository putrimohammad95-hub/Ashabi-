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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionWithItems
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PosViewModel

@Composable
fun ReportsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val totalProfit by viewModel.totalProfit.collectAsStateWithLifecycle()
    val transactionCount by viewModel.transactionCount.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ringkasan & Omzet", "Riwayat Nota Transaksi", "Barang Terlaris")

    // Rekap Barang Terjual
    val soldItemsSummary = remember(transactions) {
        val map = mutableMapOf<String, Pair<Int, Double>>() // Name -> Pair(Quantity, TotalSubtotal)
        transactions.flatMap { it.items }.forEach { item ->
            val prev = map.getOrDefault(item.productName, Pair(0, 0.0))
            map[item.productName] = Pair(prev.first + item.quantity, prev.second + item.subtotal)
        }
        map.toList().sortedByDescending { it.second.first }
    }

    val totalItemsSold = remember(transactions) {
        transactions.flatMap { it.items }.sumOf { it.quantity }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> SummaryTabContent(
                totalRevenue = totalRevenue ?: 0.0,
                totalProfit = totalProfit ?: 0.0,
                transactionCount = transactionCount,
                totalItemsSold = totalItemsSold,
                recentTransactions = transactions.take(5),
                onViewReceipt = { viewModel.viewReceipt(it) }
            )
            1 -> TransactionsHistoryContent(
                transactions = transactions,
                onViewReceipt = { viewModel.viewReceipt(it) }
            )
            2 -> BestSellersContent(soldItems = soldItemsSummary)
        }
    }
}

@Composable
fun SummaryTabContent(
    totalRevenue: Double,
    totalProfit: Double,
    transactionCount: Int,
    totalItemsSold: Int,
    recentTransactions: List<TransactionWithItems>,
    onViewReceipt: (TransactionWithItems) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Greeting Warung
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ashabi Waroeng • Laporan Penjualan",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Omzet Keseluruhan",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(totalRevenue),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 3 KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Estimasi Untung",
                    value = CurrencyUtils.formatRupiah(totalProfit),
                    icon = Icons.Default.TrendingUp,
                    containerColor = Color(0xFFE8F5E9),
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Transaksi",
                    value = "$transactionCount Nota",
                    icon = Icons.Default.ReceiptLong,
                    containerColor = Color(0xFFFFF3E0),
                    accentColor = AmberSecondary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Barang Laku",
                    value = "$totalItemsSold Item",
                    icon = Icons.Default.ShoppingBasket,
                    containerColor = Color(0xFFE0F2F1),
                    accentColor = Color(0xFF00695C),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Transaksi Terkini Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terkini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Klik nota untuk cetak struk",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada transaksi penjualan tercatat",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Buka menu Kasir POS untuk mulai melayani transaksi pembeli",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.transaction.id }) { txWithItems ->
                TransactionListItem(
                    txWithItems = txWithItems,
                    onClick = { onViewReceipt(txWithItems) }
                )
            }
        }
    }
}

@Composable
fun TransactionsHistoryContent(
    transactions: List<TransactionWithItems>,
    onViewReceipt: (TransactionWithItems) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Belum ada riwayat transaksi", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions, key = { it.transaction.id }) { txWithItems ->
                TransactionListItem(
                    txWithItems = txWithItems,
                    onClick = { onViewReceipt(txWithItems) }
                )
            }
        }
    }
}

@Composable
fun BestSellersContent(
    soldItems: List<Pair<String, Pair<Int, Double>>>
) {
    if (soldItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada data barang terjual", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(soldItems) { (productName, stats) ->
                val (qty, totalSubtotal) = stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Terjual: $qty item",
                                fontSize = 12.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Penjualan",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(totalSubtotal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TransactionListItem(
    txWithItems: TransactionWithItems,
    onClick: () -> Unit
) {
    val tx = txWithItems.transaction
    val items = txWithItems.items

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.invoiceNumber,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.paymentMethod,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${CurrencyUtils.formatDateTime(tx.timestamp)} • ${items.size} jenis barang",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatRupiah(tx.totalAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Lihat Struk",
                        fontSize = 11.sp,
                        color = AmberSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}
