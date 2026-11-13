package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TelecomRepository
import com.example.payment.CardDetails
import com.example.payment.PaymentGatewayManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RechargePay", appName)
  }

  @Test
  fun `detect card network correctly`() {
    val visa = PaymentGatewayManager.detectCardNetwork("4532758920194242")
    assertEquals("VISA", visa)

    val mastercard = PaymentGatewayManager.detectCardNetwork("5214567890123456")
    assertEquals("MASTERCARD", mastercard)
  }

  @Test
  fun `validate valid card details`() {
    val card = CardDetails(
      cardNumber = "4242424242424242",
      expiryMonthYear = "12/28",
      cvv = "842",
      cardholderName = "Alex Rivera",
      saveCard = true
    )
    val validationError = PaymentGatewayManager.validateCard(card)
    assertNull(validationError)
  }

  @Test
  fun `detect operator by mobile number prefix`() {
    val jioOp = TelecomRepository.detectOperator("9876543210")
    assertEquals("jio", jioOp.id)
  }

  @Test
  fun `categorized plans contain Data, Talktime, and Unlimited categories with pricing and validity`() {
    val plans = TelecomRepository.getPlansForOperator("jio")
    val dataPlans = plans.filter { it.category == com.example.model.PlanCategory.DATA }
    val talktimePlans = plans.filter { it.category == com.example.model.PlanCategory.TALKTIME }
    val unlimitedPlans = plans.filter { it.category == com.example.model.PlanCategory.UNLIMITED }

    org.junit.Assert.assertTrue("Should have Data plans", dataPlans.isNotEmpty())
    org.junit.Assert.assertTrue("Should have Talktime plans", talktimePlans.isNotEmpty())
    org.junit.Assert.assertTrue("Should have Unlimited plans", unlimitedPlans.isNotEmpty())

    // Check all plans have valid pricing > 0 and non-empty validity
    plans.forEach { plan ->
      org.junit.Assert.assertTrue("Price must be > 0", plan.price > 0.0)
      org.junit.Assert.assertTrue("Validity must not be empty", plan.validity.isNotBlank())
    }
  }

  @Test
  fun `format currency in Indian Rupee INR`() {
    val formattedWhole = PaymentGatewayManager.formatCurrency(299.0)
    assertEquals("₹299", formattedWhole)

    val formattedDecimal = PaymentGatewayManager.formatCurrency(39.37)
    assertEquals("₹39.37", formattedDecimal)
  }
}
