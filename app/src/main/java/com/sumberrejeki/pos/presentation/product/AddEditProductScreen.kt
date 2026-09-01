package com.sumberrejeki.pos.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: ProductViewModel,
    productId: Long,
    prefilledBarcode: String?,
    userRole: UserRole?,
    onNavigateBack: () -> Unit
) {
    val isEditMode = productId > 0

    var barcode by remember { mutableStateOf(prefilledBarcode ?: "") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sembako") }
    var sellPriceStr by remember { mutableStateOf("") }
    var costPriceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("10") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (isEditMode) {
            isLoading = true
            val product = viewModel.getProductById(productId)
            if (product != null) {
                barcode = product.barcode
                name = product.name
                category = product.category
                sellPriceStr = product.sellPrice.toLong().toString()
                costPriceStr = product.costPrice.toLong().toString()
                stockStr = product.stock.toString()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Ubah Produk" else "Tambah Produk Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (isEditMode && userRole == UserRole.ADMIN) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Produk",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Barcode Input
                OutlinedTextField(
                    value = barcode,
                    onValueChange = {
                        barcode = it
                        errorMessage = null
                    },
                    label = { Text("Kode Barcode (SKU)") },
                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nama Produk") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        errorMessage = null
                    },
                    label = { Text("Kategori (misal: Sembako, Minuman, Snack)") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selling Price & Cost Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                sellPriceStr = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Harga Jual (Rp)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                costPriceStr = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Harga Modal (Rp)") },
                        leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Stock
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            stockStr = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Jumlah Stok") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        val sPrice = sellPriceStr.toDoubleOrNull() ?: 0.0
                        val cPrice = costPriceStr.toDoubleOrNull() ?: 0.0
                        val stock = stockStr.toIntOrNull() ?: 0

                        if (barcode.isBlank()) {
                            errorMessage = "Barcode tidak boleh kosong"
                        } else if (name.isBlank()) {
                            errorMessage = "Nama produk tidak boleh kosong"
                        } else if (sPrice <= 0) {
                            errorMessage = "Harga jual harus lebih dari 0"
                        } else if (userRole == null) {
                            errorMessage = "Sesi tidak valid"
                        } else {
                            val product = Product(
                                productId = if (isEditMode) productId else 0L,
                                barcode = barcode.trim(),
                                name = name.trim(),
                                category = category.trim(),
                                sellPrice = sPrice,
                                costPrice = cPrice,
                                stock = stock
                            )
                            viewModel.saveProduct(product, userRole) { result ->
                                result.onSuccess {
                                    onNavigateBack()
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Gagal menyimpan produk"
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan" else "Tambah Produk",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Produk") },
            text = { Text("Apakah Anda yakin ingin menghapus produk '$name' dari database?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        if (userRole != null) {
                            viewModel.deleteProduct(productId, userRole) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
