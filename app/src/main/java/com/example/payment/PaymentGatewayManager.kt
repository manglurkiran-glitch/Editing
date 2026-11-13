package com.example.payment

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Result of a payment transaction attempt.
 */
sealed class PaymentResult {
    data class Success(
        val transactionId: String,
        val operatorRefId: String,
        val amount: Double,
        val paymentMethodTitle: String,
        val timestamp: Long
    ) : PaymentResult()

    data class Failure(
        val reason: String,
        val errorCode: String = "GATEWAY_DECLINED"
    ) : PaymentResult()
}

/**
 * Card details provided for direct card checkout.
 */
data class CardDetails(
    val cardNumber: String,
    val expiryMonthYear: String, // MM/YY
    val cvv: String,
    val cardholderName: String,
    val saveCard: Boolean = true
)

object PaymentGatewayManager {

    /**
     * Determines card network brand from leading digits.
     */
    fun detectCardNetwork(number: String): String {
        val clean = number.filter { it.isDigit() }
        return when {
            clean.startsWith("4") -> "VISA"
            clean.matches(Regex("^(5[1-5]|2[2-7]).*")) -> "MASTERCARD"
            clean.startsWith("34") || clean.startsWith("37") -> "AMEX"
            clean.matches(Regex("^(60|65|81|82).*")) -> "RUPAY"
            clean.startsWith("6011") || clean.startsWith("65") -> "DISCOVER"
            clean.isNotEmpty() -> "CARD"
            else -> ""
        }
    }

    /**
     * Formats card digits with spacing (e.g. "4532 1234 5678 9010").
     */
    fun formatCardNumber(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() }.take(16)
        val sb = StringBuilder()
        for (i in digitsOnly.indices) {
            sb.append(digitsOnly[i])
            if ((i + 1) % 4 == 0 && i + 1 != digitsOnly.length) {
                sb.append(" ")
            }
        }
        return sb.toString()
    }

    /**
     * Formats MM/YY expiry input.
     */
    fun formatExpiryDate(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() }.take(4)
        return when {
            digitsOnly.length > 2 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"
            else -> digitsOnly
        }
    }

    /**
     * Validates card details using format checks and Luhn algorithm.
     */
    fun validateCard(card: CardDetails): String? {
        val cleanNumber = card.cardNumber.filter { it.isDigit() }
        if (cleanNumber.length < 13 || cleanNumber.length > 19) {
            return "Please enter a valid 16-digit card number"
        }
        if (!luhnCheck(cleanNumber)) {
            return "Invalid card number checksum (Luhn check failed)"
        }

        val cleanExpiry = card.expiryMonthYear.filter { it.isDigit() }
        if (cleanExpiry.length != 4) {
            return "Please enter expiry in MM/YY format"
        }
        val month = cleanExpiry.take(2).toIntOrNull() ?: 0
        val year = cleanExpiry.drop(2).toIntOrNull() ?: 0
        if (month !in 1..12) {
            return "Invalid expiry month (01-12)"
        }
        val currentYearShort = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        if (year < currentYearShort || (year == currentYearShort && month < currentMonth)) {
            return "Card has expired"
        }

        val cleanCvv = card.cvv.filter { it.isDigit() }
        if (cleanCvv.length !in 3..4) {
            return "CVV must be 3 or 4 digits"
        }

        if (card.cardholderName.trim().length < 2) {
            return "Please enter cardholder name as on card"
        }

        return null // Valid
    }

    /**
     * Standard Luhn algorithm implementation.
     */
    private fun luhnCheck(number: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var n = number[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    /**
     * Generates a 6-digit 3D-Secure simulation OTP.
     */
    fun generateSecureOtp(): String {
        val randomNum = (100000..999999).random()
        return randomNum.toString()
    }

    /**
     * Simulates Payment Gateway tokenization handshake.
     */
    suspend fun performHandshake(amount: Double): String {
        delay(900)
        return "tok_sec_${UUID.randomUUID().toString().take(12)}"
    }

    /**
     * Simulates 3D-Secure verification and banking authorization.
     */
    suspend fun verifyAndAuthorize(
        token: String,
        enteredOtp: String,
        expectedOtp: String,
        amount: Double,
        methodTitle: String,
        forceFail: Boolean = false
    ): PaymentResult {
        delay(1200)

        if (forceFail) {
            return PaymentResult.Failure(
                reason = "Bank declined transaction: Insufficient funds or card disabled for online transactions."
            )
        }

        if (enteredOtp.trim() != expectedOtp.trim()) {
            return PaymentResult.Failure(
                reason = "Invalid 3D-Secure OTP. Verification failed.",
                errorCode = "INVALID_OTP"
            )
        }

        val now = System.currentTimeMillis()
        val randomNum = (1000000..9999999).random()
        val txnId = "TXN${System.currentTimeMillis().toString().takeLast(6)}$randomNum"
        val oprRef = "OPR-${(10000000..99999999).random()}"

        return PaymentResult.Success(
            transactionId = txnId,
            operatorRefId = oprRef,
            amount = amount,
            paymentMethodTitle = methodTitle,
            timestamp = now
        )
    }

    fun formatCurrency(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale("en", "IN"), "₹%.0f", amount)
        } else {
            String.format(Locale("en", "IN"), "₹%.2f", amount)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
