package com.thedone.finflow_client.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    //düzenelenecekse değeri tut yoksa ekle
    var transactionEdit by remember { mutableStateOf<Transaction?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    //  kaydedilen yer
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        // Kullanıcı bir dosya yolu seçtiğinde burası tetiklenir
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    // ViewModel'den CSV metnini al ve dosyaya yaz!
                    val csvData = viewModel.generateExportData()
                    outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                }
                coroutineScope.launch { snackbarHostState.showSnackbar("Rapor başarıyla kaydedildi! 📄") }
            } catch (e: Exception) {
                coroutineScope.launch { snackbarHostState.showSnackbar("Rapor kaydedilemedi!") }
            }
        }
    }
    LaunchedEffect(state.error) {
        if (state.error.isNotBlank()) {
            snackbarHostState.showSnackbar(state.error)
            viewModel.clearMessages() // Mesajı gösterdikten sonra sıfırla ki tekrar çıkmasın
        }
    }

    // Başarı mesajı varsa Snackbar'da göster
    LaunchedEffect(state.successMessage) {
        if (state.successMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(state.successMessage)
            viewModel.clearMessages()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("FinFlow Cüzdan", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("FinFlow_Rapor.csv") }) {
                        Icon(Icons.Default.Share, contentDescription = "Dışa Aktar")
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış Yap")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ekle",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("İşlem ara (Örn: Market, Maaş)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedFilter == "ALL",
                    onClick = { viewModel.onFilterChange("ALL") },
                    label = { Text("Hepsi") }
                )
                FilterChip(
                    selected = state.selectedFilter == "INCOME",
                    onClick = { viewModel.onFilterChange("INCOME") },
                    label = { Text("Gelir") }
                )
                FilterChip(
                    selected = state.selectedFilter == "EXPENSE",
                    onClick = { viewModel.onFilterChange("EXPENSE") },
                    label = { Text("Gider") }
                )
            }

            //Bakiye Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Toplam Bakiye",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₺${"%.2f".format(state.balance)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // grafik

            if (state.groupedTransactions.isEmpty()) {
                FinancePieChart(income = state.totalIncome, expense = state.totalExpense)
                Spacer(modifier = Modifier.height(16.dp))
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading && state.groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Henüz bir işlem yok. Hemen bir tane ekle!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    state.groupedTransactions.forEach { (dateHeader, transactionsForDate) ->

                        // yapışkan header
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = dateHeader,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // o tarihe ait işlemler
                        items(transactionsForDate, key = { it.id }) { transaction ->
                            var isDeleteTriggered by remember { mutableStateOf(false) }

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        if (!isDeleteTriggered) {
                                            isDeleteTriggered = true
                                            viewModel.deleteTransaction(transaction.id)
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) Color.Transparent else Color.Red
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 8.dp)
                                            .background(color, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = Color.White
                                        )
                                    }
                                },
                                content = {
                                    TransactionItem(
                                        description = transaction.description,
                                        amount = transaction.amount,
                                        type = transaction.type,
                                        onClick = {
                                            transactionEdit = transaction
                                            showDialog = true
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        TransactionFormDialog(
            transaction = transactionEdit,
            onDismiss = { showDialog = false },
            onConfirm = { id, type, amount, desc ->
                if (id == null) {
                    viewModel.addTransaction(type, amount, desc)
                } else {
                    viewModel.updateTransaction(id, type, amount, desc)
                }
                showDialog = false
                transactionEdit = null
            }
        )
    }
}

@Composable
fun FinancePieChart(income: Double, expense: Double) {
    val total = income + expense
    // Veri yoksa veya sıfırsa grafiği eşit böl (Gri görünebilir veya 50-50 olabilir)
    val incomePercentage = if (total > 0) (income / total).toFloat() else 0.5f
    val expensePercentage = if (total > 0) (expense / total).toFloat() else 0.5f

    val incomeSweep = incomePercentage * 360f
    val expenseSweep = expensePercentage * 360f

    val incomeColor = Color(0xFF4CAF50)
    val expenseColor = Color(0xFFE53935)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Çizim Alanı
        Canvas(modifier = Modifier.size(100.dp)) {
            // Gelir Yayı (Yeşil)
            drawArc(
                color = incomeColor,
                startAngle = -90f,
                sweepAngle = incomeSweep,
                useCenter = false, // True yaparsak Pac-Man gibi pasta dilimi olur, False yaparsak yüzük/halka olur
                style = Stroke(width = 30f) // Halkanın kalınlığı
            )
            // Gider Yayı (Kırmızı)
            drawArc(
                color = expenseColor,
                startAngle = -90f + incomeSweep,
                sweepAngle = expenseSweep,
                useCenter = false,
                style = Stroke(width = 30f)
            )
        }

        // Renklerin Açıklaması (Legend)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(incomeColor, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gelir: ₺$income", fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(expenseColor, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gider: ₺$expense", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TransactionItem(
    description: String,
    amount: Double,
    type: TransactionType,
    onClick: () -> Unit,
) {
    val isIncome = type == TransactionType.INCOME
    val color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(text = description, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Düzenlemek için tıkla", fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                text = "$sign ₺${"%.2f".format(amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TransactionFormDialog(
    transaction: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (id: Int?, TransactionType, Double, String) -> Unit,
) {

    val isEditMode = transaction != null

    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var isIncome by remember { mutableStateOf(transaction?.type == TransactionType.INCOME || transaction == null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "İşlemi Düzenle" else "Yeni İşlem Ekle") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = isIncome,
                        onClick = { isIncome = true },
                        label = { Text("Gelir") })
                    FilterChip(
                        selected = !isIncome,
                        onClick = { isIncome = false },
                        label = { Text("Gider") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (Maaş, Market vb.)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Tutar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                onConfirm(transaction?.id, type, amountDouble, description)
            }) { Text(if (isEditMode) "Güncelle" else "Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}