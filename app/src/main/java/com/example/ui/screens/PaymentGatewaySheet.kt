package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TelecomRepository
import com.example.model.PaymentType
import com.example.payment.PaymentGatewayManager
import com.example.ui.components.OperatorBadge
import com.example.ui.components.SecurityBadgeHeader
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.viewmodel.PaymentProcessState
import com.example.viewmodel.RechargeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewaySheet(
    viewModel: RechargeViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paymentState by viewModel.paymentState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val safeDismiss = remember(sheetState, coroutineScope) {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            coroutineScope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("payment_gateway_bottom_sheet")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            when (val state = paymentState) {
                is PaymentProcessState.Idle -> {
                    GatewayCheckoutContent(viewModel = viewModel, onClose = { safeDismiss() })
                }
                is PaymentProcessState.Tokenizing -> {
                    ProcessingGatewayState(
                        title = "Connecting to Secure Gateway",
                        subtitle = state.message
                    )
                }
                is PaymentProcessState.OtpChallenge -> {
                    OtpChallengeView(
                        state = state,
                        onVerify = { enteredOtp ->
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.submitOtp(enteredOtp)
                        },
                        onResend = { viewModel.resendOtp() },
                        onCancel = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.resetPaymentToIdle()
                        }
                    )
                }
                is PaymentProcessState.Authorizing -> {
                    ProcessingGatewayState(
                        title = "Bank 3D-Secure Authorization",
                        subtitle = state.message
                    )
                }
                is PaymentProcessState.Failed -> {
                    PaymentFailedView(
                        errorMessage = state.error,
                        onRetry = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.resetPaymentToIdle()
                        }
                    )
                }
                is PaymentProcessState.Success -> {
                    // Success is rendered full-screen or dialog
                    TransactionReceiptContent(
                        transaction = state.transaction,
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.closeCheckout()
                            viewModel.setTab(com.example.viewmodel.AppTab.HISTORY)
                        },
                        onRepeat = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.repeatRecharge(state.transaction)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GatewayCheckoutContent(
    viewModel: RechargeViewModel,
    onClose: () -> Unit
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val operator by viewModel.selectedOperator.collectAsState()
    val plan by viewModel.selectedPlan.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponMessage by viewModel.couponMessage.collectAsState()
    val selectedPaymentType by viewModel.selectedPaymentType.collectAsState()
    val savedCards by viewModel.savedCards.collectAsState()
    val selectedSavedCard by viewModel.selectedSavedCard.collectAsState()
    val useNewCard by viewModel.useNewCard.collectAsState()
    val newCardDetails by viewModel.newCardDetails.collectAsState()
    val cardError by viewModel.cardError.collectAsState()
    val selectedWallet by viewModel.selectedWallet.collectAsState()
    val upiIdInput by viewModel.upiIdInput.collectAsState()
    val selectedBank by viewModel.selectedBank.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val simulateFailure by viewModel.simulateFailure.collectAsState()

    var couponInput by remember { mutableStateOf("") }
    val rawAmount = viewModel.getRawTotalAmount()
    val finalAmount = viewModel.getFinalPayableAmount()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Secure Payment Gateway",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "256-Bit End-to-End Encrypted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    onClose()
                },
                modifier = Modifier.testTag("close_checkout_btn")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        SecurityBadgeHeader()
        Spacer(modifier = Modifier.height(16.dp))

        // Order Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_summary_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OperatorBadge(operator = operator, size = 32)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${operator.name} Mobile",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+91 $phoneNumber",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = PaymentGatewayManager.formatCurrency(rawAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val currentPlan = plan
                if (currentPlan != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentPlan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Cost Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Recharge Plan", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = PaymentGatewayManager.formatCurrency(rawAmount),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gateway / Convenience Fee", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }

                if (appliedCoupon != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Promo Discount (${appliedCoupon?.code})",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "-${PaymentGatewayManager.formatCurrency(appliedCoupon?.discount ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Payable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = PaymentGatewayManager.formatCurrency(finalAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Coupon section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = couponInput,
                onValueChange = { couponInput = it.uppercase() },
                label = { Text("Coupon Code (Try RECHARGE50)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("coupon_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            if (appliedCoupon != null) {
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        viewModel.removeCoupon()
                        couponInput = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("remove_coupon_btn")
                ) {
                    Text("Remove")
                }
            } else {
                Button(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        viewModel.applyCoupon(couponInput)
                    },
                    enabled = couponInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("apply_coupon_btn")
                ) {
                    Text("Apply")
                }
            }
        }

        if (couponMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = couponMessage.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = if (appliedCoupon != null) EmeraldGreen else CoralRed,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Payment Method Selector Tabs
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedPaymentType.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .testTag("payment_methods_tabs")
        ) {
            Tab(
                selected = selectedPaymentType == PaymentType.CARD,
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    viewModel.selectPaymentType(PaymentType.CARD)
                },
                text = { Text("Cards", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPaymentType == PaymentType.UPI_WALLET,
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    viewModel.selectPaymentType(PaymentType.UPI_WALLET)
                },
                text = { Text("Wallets", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPaymentType == PaymentType.APP_WALLET,
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    viewModel.selectPaymentType(PaymentType.APP_WALLET)
                },
                text = { Text("Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPaymentType == PaymentType.NET_BANKING,
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    viewModel.selectPaymentType(PaymentType.NET_BANKING)
                },
                text = { Text("NetBank", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Form based on selected payment method
        when (selectedPaymentType) {
            PaymentType.CARD -> {
                CardPaymentForm(
                    savedCards = savedCards,
                    selectedSavedCard = selectedSavedCard,
                    useNewCard = useNewCard,
                    newCard = newCardDetails,
                    onSelectSavedCard = { viewModel.selectSavedCard(it) },
                    onUseNewCard = { viewModel.setUseNewCard(it) },
                    onUpdateNewCard = { viewModel.updateNewCardDetails(it) }
                )
            }
            PaymentType.UPI_WALLET -> {
                DigitalWalletsForm(
                    wallets = TelecomRepository.digitalWallets,
                    selectedWallet = selectedWallet,
                    upiId = upiIdInput,
                    onSelectWallet = { viewModel.selectWallet(it) },
                    onUpiChange = { viewModel.setUpiId(it) }
                )
            }
            PaymentType.APP_WALLET -> {
                AppWalletForm(
                    balance = walletBalance,
                    requiredAmount = finalAmount
                )
            }
            PaymentType.NET_BANKING -> {
                NetBankingForm(
                    banks = TelecomRepository.popularBanks,
                    selectedBank = selectedBank,
                    onSelectBank = { viewModel.selectBank(it) }
                )
            }
        }

        // Card / Input error display
        if (cardError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CoralRed.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = CoralRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cardError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulation Mode Switch for Test Review
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simulate Bank Decline (Test Gateway Error)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Switch(
                    checked = simulateFailure,
                    onCheckedChange = { viewModel.toggleSimulateFailure(it) },
                    modifier = Modifier.testTag("simulate_failure_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pay Securely Button
        Button(
            onClick = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                viewModel.startPayment()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("pay_securely_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pay Securely ${PaymentGatewayManager.formatCurrency(finalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Protected by 3D-Secure 2.0 & Tokenized Vault",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CardPaymentForm(
    savedCards: List<com.example.model.SavedCard>,
    selectedSavedCard: com.example.model.SavedCard?,
    useNewCard: Boolean,
    newCard: com.example.payment.CardDetails,
    onSelectSavedCard: (com.example.model.SavedCard) -> Unit,
    onUseNewCard: (Boolean) -> Unit,
    onUpdateNewCard: (com.example.payment.CardDetails) -> Unit
) {
    Column {
        if (savedCards.isNotEmpty()) {
            Text(
                text = "Saved Cards (1-Tap Checkout)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            savedCards.forEach { card ->
                val isSelected = !useNewCard && selectedSavedCard?.id == card.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ElectricBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSavedCard(card) }
                        .testTag("saved_card_${card.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = card.network,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = card.maskedNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Expires ${card.expiry} • ${card.cardholderName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onUseNewCard(!useNewCard) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_new_card_toggle")
            ) {
                Icon(
                    imageVector = if (useNewCard) Icons.Default.CreditCard else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (useNewCard) "Use Saved Card" else "Pay with New Debit/Credit Card")
            }
        }

        if (useNewCard || savedCards.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enter Card Details",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Card Number Input with detected network badge
            val detectedNetwork = PaymentGatewayManager.detectCardNetwork(newCard.cardNumber)
            OutlinedTextField(
                value = newCard.cardNumber,
                onValueChange = {
                    val formatted = PaymentGatewayManager.formatCardNumber(it)
                    onUpdateNewCard(newCard.copy(cardNumber = formatted))
                },
                label = { Text("Card Number") },
                placeholder = { Text("4532 •••• •••• 4242") },
                trailingIcon = {
                    if (detectedNetwork.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = detectedNetwork,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_number_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Expiry & CVV Row
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newCard.expiryMonthYear,
                    onValueChange = {
                        val formatted = PaymentGatewayManager.formatExpiryDate(it)
                        onUpdateNewCard(newCard.copy(expiryMonthYear = formatted))
                    },
                    label = { Text("Expiry (MM/YY)") },
                    placeholder = { Text("12/28") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_expiry_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = newCard.cvv,
                    onValueChange = {
                        val clean = it.filter { ch -> ch.isDigit() }.take(4)
                        onUpdateNewCard(newCard.copy(cvv = clean))
                    },
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_cvv_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cardholder Name
            OutlinedTextField(
                value = newCard.cardholderName,
                onValueChange = { onUpdateNewCard(newCard.copy(cardholderName = it)) },
                label = { Text("Name on Card") },
                placeholder = { Text("Alex Rivera") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_name_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Save card checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUpdateNewCard(newCard.copy(saveCard = !newCard.saveCard)) }
            ) {
                Checkbox(
                    checked = newCard.saveCard,
                    onCheckedChange = { onUpdateNewCard(newCard.copy(saveCard = it)) },
                    modifier = Modifier.testTag("save_card_checkbox")
                )
                Text(
                    text = "Save card securely for 1-tap checkout (PCI-DSS tokenized)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DigitalWalletsForm(
    wallets: List<com.example.model.DigitalWallet>,
    selectedWallet: com.example.model.DigitalWallet,
    upiId: String,
    onSelectWallet: (com.example.model.DigitalWallet) -> Unit,
    onUpiChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Supported Instant Wallets",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        wallets.forEach { wallet ->
            val isSelected = selectedWallet.id == wallet.id
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) ElectricBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectWallet(wallet) }
                    .testTag("wallet_option_${wallet.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = wallet.subtext,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = upiId,
            onValueChange = onUpiChange,
            label = { Text("UPI Virtual Payment Address (VPA)") },
            placeholder = { Text("username@okhdfcbank or phone@upi") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upi_id_input")
        )
    }
}

@Composable
fun AppWalletForm(
    balance: Double,
    requiredAmount: Double
) {
    val isSufficient = balance >= requiredAmount
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_wallet_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSufficient) EmeraldGreen.copy(alpha = 0.1f) else CoralRed.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isSufficient) EmeraldGreen else CoralRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RechargePay Wallet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Available Balance: ${PaymentGatewayManager.formatCurrency(balance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSufficient) EmeraldGreen else CoralRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (isSufficient) {
                Text(
                    text = "Sufficient balance. Payment will be deducted instantly with zero gateway latency and earn 5% cashback reward!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Insufficient balance. Required: ${PaymentGatewayManager.formatCurrency(requiredAmount)}. Please select a Card or Digital Wallet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralRed
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NetBankingForm(
    banks: List<com.example.model.Bank>,
    selectedBank: com.example.model.Bank,
    onSelectBank: (com.example.model.Bank) -> Unit
) {
    Column {
        Text(
            text = "Select Your Bank",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            banks.forEach { bank ->
                val isSelected = selectedBank.id == bank.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { onSelectBank(bank) }
                        .testTag("bank_option_${bank.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bank.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingGatewayState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("processing_gateway_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(54.dp),
            color = ElectricBlue,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        SecurityBadgeHeader()
    }
}

@Composable
fun OtpChallengeView(
    state: PaymentProcessState.OtpChallenge,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onCancel: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("otp_challenge_view")
    ) {
        // Bank 3D Secure Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ElectricBlue.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "3D Secure 2.0 Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verified by Visa / Mastercard ID Check",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    onCancel()
                }
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated SMS notification box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BANK SMS NOTIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your OTP for mobile recharge transaction is ${state.expectedOtp}. Valid for 5 minutes. Do not share with anyone.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            otpInput = state.expectedOtp
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("autofill_otp_btn")
                    ) {
                        Text("Auto-fill OTP (${state.expectedOtp})", color = Color(0xFF0F1A2C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter 6-digit One-Time Password sent to registered phone ${state.maskedPhone}:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = otpInput,
            onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) otpInput = it },
            label = { Text("6-Digit OTP") },
            placeholder = { Text("• • • • • •") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Resend Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.secondsRemaining > 0) {
                Text(
                    text = "Resend OTP in ${state.secondsRemaining}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TextButton(
                    onClick = onResend,
                    modifier = Modifier.testTag("resend_otp_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resend OTP")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                onVerify(otpInput)
            },
            enabled = otpInput.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_otp_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify & Authorize", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentFailedView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp)
            .testTag("payment_failed_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = CoralRed.copy(alpha = 0.15f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = CoralRed,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Transaction Declined",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = CoralRed
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                onRetry()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("retry_payment_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Alternative Payment Method", fontWeight = FontWeight.Bold)
        }
    }
}
