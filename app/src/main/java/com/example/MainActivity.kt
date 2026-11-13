package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BrowsePlansScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PaymentGatewaySheet
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.RechargePayTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.RechargeViewModel

class MainActivity : ComponentActivity() {

    private val rechargeViewModel: RechargeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RechargePayTheme {
                RechargePayApp(viewModel = rechargeViewModel)
            }
        }
    }
}

@Composable
fun RechargePayApp(viewModel: RechargeViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isCheckoutOpen by viewModel.isCheckoutOpen.collectAsState()

    // Handle back button navigation
    BackHandler(enabled = isCheckoutOpen || currentTab != AppTab.RECHARGE) {
        if (isCheckoutOpen) {
            viewModel.closeCheckout()
        } else if (currentTab != AppTab.RECHARGE) {
            viewModel.setTab(AppTab.RECHARGE)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.RECHARGE,
                    onClick = { viewModel.setTab(AppTab.RECHARGE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.RECHARGE) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                            contentDescription = "Recharge",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Recharge", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_recharge")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PLANS,
                    onClick = { viewModel.setTab(AppTab.PLANS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.PLANS) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.AutoMirrored.Outlined.FormatListBulleted,
                            contentDescription = "Plans",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Plans", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_plans")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("History", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.WALLET,
                    onClick = { viewModel.setTab(AppTab.WALLET) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Wallet", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricBlue,
                        indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_wallet")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.RECHARGE -> HomeScreen(viewModel = viewModel)
                AppTab.PLANS -> BrowsePlansScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AppTab.WALLET -> WalletScreen(viewModel = viewModel)
            }

            // Secure Payment Gateway Bottom Sheet
            if (isCheckoutOpen) {
                PaymentGatewaySheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeCheckout() }
                )
            }
        }
    }
}
