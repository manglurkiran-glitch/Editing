package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TelecomRepository
import com.example.ui.components.CategorizedPlansExplorer
import com.example.ui.components.OperatorBadge
import com.example.ui.theme.ElectricBlue
import com.example.viewmodel.RechargeViewModel

@Composable
fun BrowsePlansScreen(
    viewModel: RechargeViewModel,
    modifier: Modifier = Modifier
) {
    val selectedOperator by viewModel.selectedOperator.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val searchQuery by viewModel.planSearchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val allOperatorPlans = remember(selectedOperator.id) {
        TelecomRepository.getPlansForOperator(selectedOperator.id)
    }

    val plansToDisplay = remember(allOperatorPlans, searchQuery) {
        if (searchQuery.isBlank()) {
            allOperatorPlans
        } else {
            allOperatorPlans.filter { plan ->
                plan.data.contains(searchQuery, ignoreCase = true) ||
                plan.voice.contains(searchQuery, ignoreCase = true) ||
                plan.description.contains(searchQuery, ignoreCase = true) ||
                plan.validity.contains(searchQuery, ignoreCase = true) ||
                plan.price.toString().contains(searchQuery) ||
                plan.ottPerks.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("browse_plans_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Browse Plans",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Categorized by Data, Talktime, and Unlimited with pricing and validity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setPlanSearchQuery(it) },
                    placeholder = { Text("Search by OTT, validity or price...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                    viewModel.setPlanSearchQuery("")
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plans_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Operator selector pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelecomRepository.operators.forEach { op ->
                        val isSelected = selectedOperator.id == op.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                viewModel.selectOperator(op)
                            },
                            label = { Text(op.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                OperatorBadge(operator = op, size = 18)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("operator_filter_${op.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Categorized Plans Explorer Component
                CategorizedPlansExplorer(
                    operator = selectedOperator,
                    plans = plansToDisplay,
                    selectedPlan = selectedPlan,
                    onPlanSelected = { plan ->
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        viewModel.selectPlan(plan)
                        viewModel.openCheckout()
                    }
                )
            }
        }
    }
}
