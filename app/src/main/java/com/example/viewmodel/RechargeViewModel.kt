package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TelecomRepository
import com.example.model.Bank
import com.example.model.Circle
import com.example.model.Coupon
import com.example.model.DigitalWallet
import com.example.model.Operator
import com.example.model.PaymentType
import com.example.model.PlanCategory
import com.example.model.RechargePlan
import com.example.model.RecentContact
import com.example.model.SavedCard
import com.example.model.TransactionRecord
import com.example.payment.CardDetails
import com.example.payment.PaymentGatewayManager
import com.example.payment.PaymentResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    RECHARGE,
    PLANS,
    HISTORY,
    WALLET
}

sealed interface PaymentProcessState {
    object Idle : PaymentProcessState
    data class Tokenizing(val message: String = "Connecting to Secure Gateway...") : PaymentProcessState
    data class OtpChallenge(
        val expectedOtp: String,
        val maskedPhone: String,
        val secondsRemaining: Int
    ) : PaymentProcessState
    data class Authorizing(val message: String = "Authorizing with Acquirer Bank...") : PaymentProcessState
    data class Success(val transaction: TransactionRecord) : PaymentProcessState
    data class Failed(val error: String) : PaymentProcessState
}

class RechargeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val transactionDao = db.transactionDao()

    // Navigation & Primary Tab
    private val _currentTab = MutableStateFlow(AppTab.RECHARGE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Form inputs
    private val _phoneNumber = MutableStateFlow("9876543210")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _selectedOperator = MutableStateFlow(TelecomRepository.operators.first())
    val selectedOperator: StateFlow<Operator> = _selectedOperator.asStateFlow()

    private val _selectedCircle = MutableStateFlow(TelecomRepository.circles.first())
    val selectedCircle: StateFlow<Circle> = _selectedCircle.asStateFlow()

    private val _isPostpaid = MutableStateFlow(false)
    val isPostpaid: StateFlow<Boolean> = _isPostpaid.asStateFlow()

    private val _selectedPlan = MutableStateFlow<RechargePlan?>(null)
    val selectedPlan: StateFlow<RechargePlan?> = _selectedPlan.asStateFlow()

    private val _customAmount = MutableStateFlow("349")
    val customAmount: StateFlow<String> = _customAmount.asStateFlow()

    private val _selectedPlanCategory = MutableStateFlow(PlanCategory.UNLIMITED)
    val selectedPlanCategory: StateFlow<PlanCategory> = _selectedPlanCategory.asStateFlow()

    // Plan search filter in Plans tab
    private val _planSearchQuery = MutableStateFlow("")
    val planSearchQuery: StateFlow<String> = _planSearchQuery.asStateFlow()

    // Discounts & Coupons
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage: StateFlow<String?> = _couponMessage.asStateFlow()

    // Checkout & Payment Gateway States
    private val _isCheckoutOpen = MutableStateFlow(false)
    val isCheckoutOpen: StateFlow<Boolean> = _isCheckoutOpen.asStateFlow()

    private val _selectedPaymentType = MutableStateFlow(PaymentType.CARD)
    val selectedPaymentType: StateFlow<PaymentType> = _selectedPaymentType.asStateFlow()

    private val _savedCards = MutableStateFlow(TelecomRepository.savedCards)
    val savedCards: StateFlow<List<SavedCard>> = _savedCards.asStateFlow()

    private val _selectedSavedCard = MutableStateFlow<SavedCard?>(TelecomRepository.savedCards.firstOrNull())
    val selectedSavedCard: StateFlow<SavedCard?> = _selectedSavedCard.asStateFlow()

    private val _useNewCard = MutableStateFlow(false)
    val useNewCard: StateFlow<Boolean> = _useNewCard.asStateFlow()

    private val _newCardDetails = MutableStateFlow(
        CardDetails(
            cardNumber = "4242 4242 4242 4242",
            expiryMonthYear = "12/28",
            cvv = "842",
            cardholderName = "Alex Rivera",
            saveCard = true
        )
    )
    val newCardDetails: StateFlow<CardDetails> = _newCardDetails.asStateFlow()

    private val _selectedWallet = MutableStateFlow(TelecomRepository.digitalWallets.first())
    val selectedWallet: StateFlow<DigitalWallet> = _selectedWallet.asStateFlow()

    private val _upiIdInput = MutableStateFlow("alex@okhdfcbank")
    val upiIdInput: StateFlow<String> = _upiIdInput.asStateFlow()

    private val _selectedBank = MutableStateFlow(TelecomRepository.popularBanks.first())
    val selectedBank: StateFlow<Bank> = _selectedBank.asStateFlow()

    private val _walletBalance = MutableStateFlow(450.00)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _cardError = MutableStateFlow<String?>(null)
    val cardError: StateFlow<String?> = _cardError.asStateFlow()

    // Payment Processing Lifecycle
    private val _paymentState = MutableStateFlow<PaymentProcessState>(PaymentProcessState.Idle)
    val paymentState: StateFlow<PaymentProcessState> = _paymentState.asStateFlow()

    // Simulation toggle to demonstrate test failure / decline handling
    private val _simulateFailure = MutableStateFlow(false)
    val simulateFailure: StateFlow<Boolean> = _simulateFailure.asStateFlow()

    // Room DB Transactions Flow
    val allTransactions: StateFlow<List<TransactionRecord>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active OTP timer job
    private var otpTimerJob: Job? = null
    private var activeSessionToken: String = ""
    private var currentExpectedOtp: String = ""

    init {
        // Set default plan to first recommended plan
        val initialPlans = TelecomRepository.getPlansForOperator(_selectedOperator.value.id)
        _selectedPlan.value = initialPlans.firstOrNull()
        _selectedPlan.value?.let { _customAmount.value = it.price.toString() }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun onPhoneNumberChanged(number: String) {
        val clean = number.filter { it.isDigit() }.take(10)
        _phoneNumber.value = clean
        if (clean.length >= 2) {
            val detected = TelecomRepository.detectOperator(clean)
            if (detected.id != _selectedOperator.value.id) {
                _selectedOperator.value = detected
                // Update selected plan to match operator
                val plans = TelecomRepository.getPlansForOperator(detected.id)
                _selectedPlan.value = plans.firstOrNull()
                _selectedPlan.value?.let { _customAmount.value = it.price.toString() }
            }
        }
    }

    fun selectOperator(operator: Operator) {
        _selectedOperator.value = operator
        val plans = TelecomRepository.getPlansForOperator(operator.id)
        _selectedPlan.value = plans.firstOrNull()
        _selectedPlan.value?.let { _customAmount.value = it.price.toString() }
    }

    fun selectCircle(circle: Circle) {
        _selectedCircle.value = circle
    }

    fun setPostpaid(postpaid: Boolean) {
        _isPostpaid.value = postpaid
    }

    fun selectPlan(plan: RechargePlan) {
        _selectedPlan.value = plan
        _customAmount.value = plan.price.toString()
    }

    fun setCustomAmount(amount: String) {
        val clean = amount.filter { it.isDigit() || it == '.' }
        _customAmount.value = clean
        // If matches a known plan, select it, otherwise clear selected plan
        val matchedPlan = TelecomRepository.getPlansForOperator(_selectedOperator.value.id)
            .firstOrNull { it.price.toString() == clean }
        _selectedPlan.value = matchedPlan
    }

    fun selectPlanCategory(category: PlanCategory) {
        _selectedPlanCategory.value = category
    }

    fun setPlanSearchQuery(query: String) {
        _planSearchQuery.value = query
    }

    fun selectContact(contact: RecentContact) {
        _phoneNumber.value = contact.phoneNumber
        TelecomRepository.operators.firstOrNull { it.id == contact.operatorId }?.let {
            selectOperator(it)
        }
    }

    fun applyCoupon(code: String) {
        val found = TelecomRepository.coupons.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
        val currentTotal = getRawTotalAmount()
        if (found == null) {
            _couponMessage.value = "Invalid coupon code '$code'"
            _appliedCoupon.value = null
        } else if (currentTotal < found.minOrder) {
            _couponMessage.value = "Coupon requires minimum recharge of ${PaymentGatewayManager.formatCurrency(found.minOrder)}"
            _appliedCoupon.value = null
        } else {
            _appliedCoupon.value = found
            _couponMessage.value = "Coupon applied! ${PaymentGatewayManager.formatCurrency(found.discount)} discount"
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponMessage.value = null
    }

    fun getRawTotalAmount(): Double {
        return _selectedPlan.value?.price ?: _customAmount.value.toDoubleOrNull() ?: 349.0
    }

    fun getFinalPayableAmount(): Double {
        val raw = getRawTotalAmount()
        val discount = _appliedCoupon.value?.discount ?: 0.0
        return maxOf(10.0, raw - discount)
    }

    // Checkout Modal controls
    fun openCheckout() {
        if (_phoneNumber.value.length < 10) {
            // Fill default if user didn't type complete
            _phoneNumber.value = "9876543210"
        }
        _cardError.value = null
        _paymentState.value = PaymentProcessState.Idle
        _isCheckoutOpen.value = true
    }

    fun closeCheckout() {
        _isCheckoutOpen.value = false
        _paymentState.value = PaymentProcessState.Idle
        otpTimerJob?.cancel()
    }

    fun selectPaymentType(type: PaymentType) {
        _selectedPaymentType.value = type
        _cardError.value = null
    }

    fun selectSavedCard(card: SavedCard) {
        _selectedSavedCard.value = card
        _useNewCard.value = false
        _cardError.value = null
    }

    fun setUseNewCard(use: Boolean) {
        _useNewCard.value = use
        if (use) _selectedSavedCard.value = null
        _cardError.value = null
    }

    fun updateNewCardDetails(details: CardDetails) {
        _newCardDetails.value = details
        _cardError.value = null
    }

    fun selectWallet(wallet: DigitalWallet) {
        _selectedWallet.value = wallet
    }

    fun setUpiId(id: String) {
        _upiIdInput.value = id
    }

    fun selectBank(bank: Bank) {
        _selectedBank.value = bank
    }

    fun toggleSimulateFailure(fail: Boolean) {
        _simulateFailure.value = fail
    }

    // Start payment processing
    fun startPayment() {
        val finalAmount = getFinalPayableAmount()

        // Validation based on payment method
        when (_selectedPaymentType.value) {
            PaymentType.CARD -> {
                if (_useNewCard.value || _selectedSavedCard.value == null) {
                    val error = PaymentGatewayManager.validateCard(_newCardDetails.value)
                    if (error != null) {
                        _cardError.value = error
                        return
                    }
                }
            }
            PaymentType.UPI_WALLET -> {
                if (!_upiIdInput.value.contains("@") && _selectedWallet.value.iconType == "UPI") {
                    _cardError.value = "Please enter a valid UPI ID (e.g. name@bank)"
                    return
                }
            }
            PaymentType.APP_WALLET -> {
                if (_walletBalance.value < finalAmount) {
                    _cardError.value = "Insufficient wallet balance (${PaymentGatewayManager.formatCurrency(_walletBalance.value)}). Please select another payment method or add funds."
                    return
                }
            }
            PaymentType.NET_BANKING -> {
                // Bank selected is always valid
            }
        }

        viewModelScope.launch {
            _paymentState.value = PaymentProcessState.Tokenizing("Encrypting & Initializing 256-Bit Handshake...")
            activeSessionToken = PaymentGatewayManager.performHandshake(finalAmount)

            // Generate 3D Secure OTP
            currentExpectedOtp = PaymentGatewayManager.generateSecureOtp()
            startOtpChallenge(currentExpectedOtp)
        }
    }

    private fun startOtpChallenge(otp: String) {
        val maskedPhone = "•••• ••" + _phoneNumber.value.takeLast(4)
        _paymentState.value = PaymentProcessState.OtpChallenge(
            expectedOtp = otp,
            maskedPhone = maskedPhone,
            secondsRemaining = 30
        )

        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            var timeLeft = 30
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
                val current = _paymentState.value
                if (current is PaymentProcessState.OtpChallenge) {
                    _paymentState.value = current.copy(secondsRemaining = timeLeft)
                }
            }
        }
    }

    fun resendOtp() {
        currentExpectedOtp = PaymentGatewayManager.generateSecureOtp()
        startOtpChallenge(currentExpectedOtp)
    }

    fun submitOtp(enteredOtp: String) {
        otpTimerJob?.cancel()
        val finalAmount = getFinalPayableAmount()
        val methodTitle = when (_selectedPaymentType.value) {
            PaymentType.CARD -> {
                if (_useNewCard.value || _selectedSavedCard.value == null) {
                    "${PaymentGatewayManager.detectCardNetwork(_newCardDetails.value.cardNumber)} •••• ${_newCardDetails.value.cardNumber.takeLast(4)}"
                } else {
                    "${_selectedSavedCard.value?.network} ${_selectedSavedCard.value?.maskedNumber}"
                }
            }
            PaymentType.UPI_WALLET -> _selectedWallet.value.name
            PaymentType.APP_WALLET -> "RechargePay Wallet"
            PaymentType.NET_BANKING -> "${_selectedBank.value.name} Net Banking"
        }

        viewModelScope.launch {
            _paymentState.value = PaymentProcessState.Authorizing("Verifying with Bank & Confirming Operator...")
            val result = PaymentGatewayManager.verifyAndAuthorize(
                token = activeSessionToken,
                enteredOtp = enteredOtp,
                expectedOtp = currentExpectedOtp,
                amount = finalAmount,
                methodTitle = methodTitle,
                forceFail = _simulateFailure.value
            )

            when (result) {
                is PaymentResult.Success -> {
                    // If user requested to save the card
                    if (_selectedPaymentType.value == PaymentType.CARD && _useNewCard.value && _newCardDetails.value.saveCard) {
                        val network = PaymentGatewayManager.detectCardNetwork(_newCardDetails.value.cardNumber)
                        val masked = "•••• •••• •••• ${_newCardDetails.value.cardNumber.takeLast(4)}"
                        val newCard = SavedCard(
                            id = "card_${System.currentTimeMillis()}",
                            cardholderName = _newCardDetails.value.cardholderName,
                            maskedNumber = masked,
                            network = network,
                            expiry = _newCardDetails.value.expiryMonthYear,
                            isDefault = false
                        )
                        _savedCards.value = _savedCards.value + newCard
                    }

                    // Deduct from app wallet if wallet payment
                    if (_selectedPaymentType.value == PaymentType.APP_WALLET) {
                        _walletBalance.value = maxOf(0.0, _walletBalance.value - finalAmount)
                    }

                    // 5% cashback reward to wallet
                    val cashbackReward = (finalAmount * 0.05).coerceAtLeast(10.0)
                    _walletBalance.value += cashbackReward

                    val planDesc = _selectedPlan.value?.description
                        ?: "Custom recharge pack (${_selectedOperator.value.name})"
                    val validity = _selectedPlan.value?.validity ?: "28 Days"

                    val transaction = TransactionRecord(
                        transactionId = result.transactionId,
                        operatorRefId = result.operatorRefId,
                        phoneNumber = _phoneNumber.value,
                        operatorName = _selectedOperator.value.name,
                        planPrice = getRawTotalAmount(),
                        discountAmount = _appliedCoupon.value?.discount ?: 0.0,
                        finalAmountPaid = finalAmount,
                        paymentMethodName = methodTitle,
                        paymentType = _selectedPaymentType.value.name,
                        status = "SUCCESS",
                        timestamp = result.timestamp,
                        planDetails = planDesc,
                        validity = validity
                    )

                    transactionDao.insertTransaction(transaction)
                    _paymentState.value = PaymentProcessState.Success(transaction)
                }

                is PaymentResult.Failure -> {
                    _paymentState.value = PaymentProcessState.Failed(result.reason)
                }
            }
        }
    }

    fun resetPaymentToIdle() {
        _paymentState.value = PaymentProcessState.Idle
    }

    fun repeatRecharge(transaction: TransactionRecord) {
        _phoneNumber.value = transaction.phoneNumber
        TelecomRepository.operators.firstOrNull { it.name.contains(transaction.operatorName, ignoreCase = true) }?.let {
            _selectedOperator.value = it
        }
        _customAmount.value = transaction.planPrice.toString()
        val matched = TelecomRepository.getPlansForOperator(_selectedOperator.value.id)
            .firstOrNull { it.price == transaction.planPrice }
        _selectedPlan.value = matched
        _currentTab.value = AppTab.RECHARGE
        openCheckout()
    }

    fun addMoneyToWallet(amount: Double) {
        _walletBalance.value += amount
    }
}
