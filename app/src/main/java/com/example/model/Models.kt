package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a telecom mobile network operator.
 */
data class Operator(
    val id: String,
    val name: String,
    val code: String,
    val brandColorHex: Long,
    val circleName: String = "National",
    val samplePrefixes: List<String> = emptyList()
)

/**
 * Represents a telecom circle / region.
 */
data class Circle(
    val id: String,
    val name: String
)

/**
 * Categories of mobile recharge plans: Data, Talktime, and Unlimited.
 */
enum class PlanCategory(val displayName: String) {
    DATA("Data"),
    TALKTIME("Talktime"),
    UNLIMITED("Unlimited")
}

/**
 * Mobile recharge plan details with pricing, validity, data, talktime, and benefits.
 */
data class RechargePlan(
    val id: String,
    val operatorId: String,
    val category: PlanCategory,
    val price: Double,
    val validity: String,
    val data: String,
    val voice: String,
    val sms: String,
    val description: String,
    val talktimeValue: String = "",
    val pricePerDay: String = "",
    val ottPerks: List<String> = emptyList(),
    val isPopular: Boolean = false,
    val badgeTag: String = "",
    val speedTier: String = "5G / 4G"
)

/**
 * Frequent contact for 1-tap recharge.
 */
data class RecentContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val operatorId: String,
    val relationTag: String
)

/**
 * Payment method types supported by the payment gateway.
 */
enum class PaymentType {
    CARD,
    UPI_WALLET,
    APP_WALLET,
    NET_BANKING
}

/**
 * Saved card for 1-tap secure checkout.
 */
data class SavedCard(
    val id: String,
    val cardholderName: String,
    val maskedNumber: String,
    val network: String, // VISA, MASTERCARD, AMEX, RUPAY, DISCOVER
    val expiry: String,
    val isDefault: Boolean = false
)

/**
 * Digital Wallet / Instant Payment Option.
 */
data class DigitalWallet(
    val id: String,
    val name: String,
    val subtext: String,
    val iconType: String
)

/**
 * Net Banking bank option.
 */
data class Bank(
    val id: String,
    val name: String,
    val code: String
)

/**
 * Promotional coupon code.
 */
data class Coupon(
    val code: String,
    val discount: Double,
    val minOrder: Double,
    val description: String
)

/**
 * Room Database entity for transaction history & passbook.
 */
@Entity(tableName = "recharge_transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: String,
    val operatorRefId: String,
    val phoneNumber: String,
    val operatorName: String,
    val planPrice: Double,
    val discountAmount: Double,
    val finalAmountPaid: Double,
    val paymentMethodName: String,
    val paymentType: String,
    val status: String, // "SUCCESS", "FAILED", "PROCESSING"
    val timestamp: Long,
    val planDetails: String,
    val validity: String
)
