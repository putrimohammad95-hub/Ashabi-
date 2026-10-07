package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionWithItems
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ReceiptDialog(
    transactionWithItems: TransactionWithItems,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tx = transactionWithItems.transaction
    val items = transactionWithItems.items

    val receiptText = buildReceiptText(transactionWithItems)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("receipt_dialog_surface"),
            color = Color(0xFFFAFAFA),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sukses",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transaksi Berhasil!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Realistic Paper Receipt Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD6D6D6), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Struk Ashabi Waroeng
                        Text(
                            text = "ASHABI WAROENG",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Minimarket & Warung Sembako Terpercaya",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Jl. Raya Warung No. 16, Kota Baru",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Telp/WA: 0812-3456-7890",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        ReceiptDashedDivider()

                        // Info Nota & Kasir
                        ReceiptRow(label = "No. Nota", value = tx.invoiceNumber)
                        ReceiptRow(label = "Tanggal", value = CurrencyUtils.formatDateTime(tx.timestamp))
                        ReceiptRow(label = "Kasir", value = tx.cashierName)
                        ReceiptRow(label = "Metode", value = tx.paymentMethod)

                        ReceiptDashedDivider()

                        // Daftar Barang Belanjaan
                        items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = item.productName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity} x ${CurrencyUtils.formatRupiah(item.price)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRupiah(item.subtotal),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        ReceiptDashedDivider()

                        // Ringkasan Pembayaran
                        ReceiptRow(label = "Subtotal", value = CurrencyUtils.formatRupiah(tx.subtotalAmount))
                        if (tx.discountAmount > 0) {
                            ReceiptRow(
                                label = "Diskon",
                                value = "-${CurrencyUtils.formatRupiah(tx.discountAmount)}",
                                isDiscount = true
                            )
                        }
                        ReceiptRow(
                            label = "TOTAL BELANJA",
                            value = CurrencyUtils.formatRupiah(tx.totalAmount),
                            isBold = true,
                            fontSize = 14.sp
                        )
                        ReceiptRow(label = "Bayar (${tx.paymentMethod})", value = CurrencyUtils.formatRupiah(tx.paidAmount))
                        ReceiptRow(
                            label = "Kembalian",
                            value = CurrencyUtils.formatRupiah(tx.changeAmount),
                            isBold = true
                        )

                        ReceiptDashedDivider()

                        // Footer Ucapan Terima Kasih
                        Text(
                            text = "Terima Kasih Telah Berbelanja di",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ASHABI WAROENG",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Barang yang sudah dibeli tidak dapat ditukar/dikembalikan.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Semoga Berkah & Selamat Datang Kembali!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { shareReceipt(context, receiptText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Struk",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Selesai",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Selesai")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDashedDivider() {
    Text(
        text = "------------------------------------------",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = Color.LightGray,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isDiscount: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) Color(0xFFC62828) else Color.DarkGray
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) Color(0xFFC62828) else Color.Black
        )
    }
}

fun buildReceiptText(txWithItems: TransactionWithItems): String {
    val tx = txWithItems.transaction
    val items = txWithItems.items
    val sb = StringBuilder()
    sb.appendLine("================================")
    sb.appendLine("        ASHABI WAROENG          ")
    sb.appendLine("   Minimarket & Sembako Murah   ")
    sb.appendLine(" Jl. Raya Warung No. 16, Kota Baru")
    sb.appendLine("      WA: 0812-3456-7890        ")
    sb.appendLine("================================")
    sb.appendLine("No. Nota : ${tx.invoiceNumber}")
    sb.appendLine("Waktu    : ${CurrencyUtils.formatDateTime(tx.timestamp)}")
    sb.appendLine("Kasir    : ${tx.cashierName}")
    sb.appendLine("Metode   : ${tx.paymentMethod}")
    sb.appendLine("--------------------------------")
    items.forEach { item ->
        sb.appendLine(item.productName)
        val line = "  ${item.quantity} x ${CurrencyUtils.formatRupiah(item.price)}"
        val sub = CurrencyUtils.formatRupiah(item.subtotal)
        val spaces = " ".repeat((32 - line.length - sub.length).coerceAtLeast(1))
        sb.appendLine("$line$spaces$sub")
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal : ${CurrencyUtils.formatRupiah(tx.subtotalAmount)}")
    if (tx.discountAmount > 0) {
        sb.appendLine("Diskon   : -${CurrencyUtils.formatRupiah(tx.discountAmount)}")
    }
    sb.appendLine("TOTAL    : ${CurrencyUtils.formatRupiah(tx.totalAmount)}")
    sb.appendLine("Bayar    : ${CurrencyUtils.formatRupiah(tx.paidAmount)}")
    sb.appendLine("Kembali  : ${CurrencyUtils.formatRupiah(tx.changeAmount)}")
    sb.appendLine("================================")
    sb.appendLine(" Terima Kasih Telah Berbelanja  ")
    sb.appendLine("       di ASHABI WAROENG        ")
    sb.appendLine("   Semoga Berkah & Kembali Lagi ")
    sb.appendLine("================================")
    return sb.toString()
}

fun shareReceipt(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Ashabi Waroeng")
    context.startActivity(shareIntent)
}
