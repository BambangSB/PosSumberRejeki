package com.sumberrejeki.pos.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.UserRole

@Composable
fun ProductCatalogScreen(
    viewModel: ProductViewModel,
    userRole: UserRole?,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val licenseInfo by viewModel.licenseInfo.collectAsState()

    var showProductLimitDialog by remember { mutableStateOf(false) }
    var showEditBlockedDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val gridColumns = if (configuration.screenWidthDp >= 600) 3 else 2

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Cari nama produk atau barcode...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Products Grid
            if (products.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tidak ada produk ditemukan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Coba kata kunci pencarian atau kategori lain",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.productId }) { product ->
                        ProductGridItem(
                            product = product,
                            onAddToCart = { viewModel.addToCart(it) },
                            onClick = {
                                if (userRole == UserRole.ADMIN) {
                                    if (!licenseInfo.canEditProductDetails) {
                                        showEditBlockedDialog = true
                                    } else {
                                        onNavigateToEditProduct(product.productId)
                                    }
                                } else {
                                    viewModel.addToCart(product)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add Product Floating Button for ADMIN
        if (userRole == UserRole.ADMIN) {
            FloatingActionButton(
                onClick = {
                    if (!licenseInfo.canAddProduct(products.size)) {
                        showProductLimitDialog = true
                    } else {
                        onNavigateToAddProduct()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Produk Baru"
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // 30 Product Limit Dialog
        if (showProductLimitDialog) {
            AlertDialog(
                onDismissRequest = { showProductLimitDialog = false },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706)) },
                title = { Text("Batas 30 Produk Tercapai", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Akun versi Gratis dibatasi maksimal 30 produk. Upgrade ke paket Pro untuk menambah produk tanpa batasan dan kelola ribuan stok toko Anda!")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProductLimitDialog = false
                            onNavigateToSubscription()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Upgrade ke Pro", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProductLimitDialog = false }) {
                        Text("Nanti Saja")
                    }
                }
            )
        }

        // Edit Product Blocked Dialog
        if (showEditBlockedDialog) {
            AlertDialog(
                onDismissRequest = { showEditBlockedDialog = false },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706)) },
                title = { Text("Edit Produk Terkunci", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Fitur mengedit detail produk yang sudah tersimpan hanya tersedia untuk pengguna paket Pro. Upgrade sekarang mulai dari Rp 2.000/hari!")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEditBlockedDialog = false
                            onNavigateToSubscription()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Upgrade ke Pro", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditBlockedDialog = false }) {
                        Text("Nanti Saja")
                    }
                }
            )
        }
    }
}
