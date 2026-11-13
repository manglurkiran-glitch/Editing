package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionRecord
import com.example.payment.PaymentGatewayManager
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen

@Composable
fun TransactionReceiptContent(
    transaction: TransactionRecord,
    onDone: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("transaction_receipt_content"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Icon
        Surface(
            shape = CircleShape,
            color = EmeraldGreen.copy(alpha = 0.15f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Recharge Successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldGreen
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = PaymentGatewayManager.formatCurrency(transaction.finalAmountPaid),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = PaymentGatewayManager.formatTimestamp(transaction.timestamp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Receipt Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ReceiptRow(
                    label = "Mobile Number",
                    value = "+91 ${transaction.phoneNumber}"
                )
                ReceiptRow(
                    label = "Operator & Network",
                    value = transaction.operatorName
                )
                ReceiptRow(
                    label = "Plan Validity",
                    value = transaction.validity
                )
                ReceiptRow(
                    label = "Payment Method",
                    value = transaction.paymentMethodName
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                ReceiptRow(
                    label = "Operator Ref ID",
                    value = transaction.operatorRefId,
                    isMonospace = true,
                    onCopy = { clipboardManager.setText(AnnotatedString(transaction.operatorRefId)) }
                )
                ReceiptRow(
                    label = "Transaction ID",
                    value = transaction.transactionId,
                    isMonospace = true,
                    onCopy = { clipboardManager.setText(AnnotatedString(transaction.transactionId)) }
                )

                if (transaction.discountAmount > 0) {
                    ReceiptRow(
                        label = "Cashback / Discount",
                        value = "-${PaymentGatewayManager.formatCurrency(transaction.discountAmount)}",
                        highlightColor = EmeraldGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Plan Benefit: ${transaction.planDetails}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions: Share Receipt, Repeat Recharge, Done
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val shareText = """
                        RechargePay Receipt
                        --------------------
                        Mobile: ${transaction.phoneNumber}
                        Operator: ${transaction.operatorName}
                        Amount: ${PaymentGatewayManager.formatCurrency(transaction.finalAmountPaid)}
                        Operator Ref: ${transaction.operatorRefId}
                        Txn ID: ${transaction.transactionId}
                        Status: SUCCESS
                    """.trimIndent()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Recharge Receipt"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("share_receipt_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            OutlinedButton(
                onClick = onRepeat,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("repeat_recharge_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Repeat")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("receipt_done_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    highlightColor: Color? = null,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    color = highlightColor ?: MaterialTheme.colorScheme.onSurface
                )
            )
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(
                        modifier = Modifier.background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(14.dp)
                                .testTag("copy_${label.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    }
}
