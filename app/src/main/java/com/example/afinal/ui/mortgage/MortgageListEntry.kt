package com.example.afinal.ui.mortgage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.afinal.viewmodel.mortgage.MortgageViewModel

@Composable
fun MortgageListEntry(
    navController: NavController,
    viewModel: MortgageViewModel,
    modifier: Modifier = Modifier,
    currentAccountId: String?
) {
    // Mỗi khi tab này được active hoặc quay lại từ backstack
    LaunchedEffect(currentAccountId) {
        if (currentAccountId != null) {
            println("🔁 Reload mortgages for account $currentAccountId")
            viewModel.loadMortgagesForUser(currentAccountId)
        }
    }

    MortgageListScreen(
        viewModel = viewModel,
        onSelect = { id -> navController.navigate("mortgage_detail/$id") },
        modifier = modifier
    )
}

