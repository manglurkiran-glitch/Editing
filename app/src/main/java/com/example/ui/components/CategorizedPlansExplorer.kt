package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Operator
import com.example.model.PlanCategory
import com.example.model.RechargePlan
import com.example.payment.PaymentGatewayManager
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen

/**
 * Reusable UI component to display mobile recharge plans categorized by:
 * - 'Data'
 * - 'Talktime'
 * - 'Unlimited'
 * with detailed pricing, validity, allowances, perks, and direct 1-tap checkout.
 */
@Composable
fun CategorizedPlansExplorer(
    operator: Operator,
    plans: List<RechargePlan>,
    selectedPlan: RechargePlan?,
    onPlanSelected: (RechargePlan) -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: PlanCategory = PlanCategory.UNLIMITED
) {
    var activeCategory by remember { mutableStateOf(initialCategory) }
    var activeSubFilter by remember { mutableStateOf("ALL") } // "ALL", "POPULAR", "UNDER_20", "LONG_TERM"

    // Group plans by the three required categories
    val plansByCategory = remember(plans) {
        plans.groupBy { it.category }
    }

    val currentCategoryPlans = plansByCategory[activeCategory].orEmpty()

    val filteredPlans = remember(currentCategoryPlans, activeSubFilter) {
        when (activeSubFilter) {
            "POPULAR" -> currentCategoryPlans.filter { it.isPopular }
            "UNDER_300" -> currentCategoryPlans.filter { it.price <= 300.0 }
            "LONG_TERM" -> currentCategoryPlans.filter {
                it.validity.contains("84") || it.validity.contains("365") || it.validity.contains("Unlimited")
            }
            else -> currentCategoryPlans
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("categorized_plans_explorer")
    ) {
        // Category Selector Tab Bar (Data, Talktime, Unlimited)
        CategoryTabBar(
            selectedCategory = activeCategory,
            dataCount = plansByCategory[PlanCategory.DATA]?.size ?: 0,
            talktimeCount = plansByCategory[PlanCategory.TALKTIME]?.size ?: 0,
            unlimitedCount = plansByCategory[PlanCategory.UNLIMITED]?.size ?: 0,
            onCategorySelected = { category ->
                activeCategory = category
                activeSubFilter = "ALL"
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Benefit & Overview Banner
        CategoryOverviewBanner(category = activeCategory)

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Sub-Filter Pills (All, Popular, Under $20, Long Term)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeSubFilter == "ALL",
                onClick = { activeSubFilter = "ALL" },
                label = { Text("All (${currentCategoryPlans.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBlue,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("subfilter_all")
            )

            FilterChip(
                selected = activeSubFilter == "POPULAR",
                onClick = { activeSubFilter = "POPULAR" },
                label = { Text("Popular", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBlue,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("subfilter_popular")
            )

            FilterChip(
                selected = activeSubFilter == "UNDER_300",
                onClick = { activeSubFilter = "UNDER_300" },
                label = { Text("Under ₹300", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBlue,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("subfilter_under_300")
            )

            FilterChip(
                selected = activeSubFilter == "LONG_TERM",
                onClick = { activeSubFilter = "LONG_TERM" },
                label = { Text("Long Validity", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBlue,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("subfilter_long_term")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animated Plans List Display
        AnimatedContent(
            targetState = filteredPlans,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "categorized_plans_animation"
        ) { targetPlans ->
            if (targetPlans.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No plans match this filter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try switching filters to view all available ${activeCategory.displayName} plans.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    targetPlans.forEach { plan ->
                        CategorizedPlanCard(
                            plan = plan,
                            isSelected = selectedPlan?.id == plan.id,
                            onSelect = { onPlanSelected(plan) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-fidelity Tab selector specifically highlighting 'Data', 'Talktime', and 'Unlimited'.
 */
@Composable
private fun CategoryTabBar(
    selectedCategory: PlanCategory,
    dataCount: Int,
    talktimeCount: Int,
    unlimitedCount: Int,
    onCategorySelected: (PlanCategory) -> Unit
) {
    val categories = listOf(
        Triple(PlanCategory.DATA, "Data", dataCount),
        Triple(PlanCategory.TALKTIME, "Talktime", talktimeCount),
        Triple(PlanCategory.UNLIMITED, "Unlimited", unlimitedCount)
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_tab_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { (category, title, count) ->
                val isSelected = selectedCategory == category
                val icon = when (category) {
                    PlanCategory.DATA -> Icons.Default.NetworkCell
                    PlanCategory.TALKTIME -> Icons.Default.PhoneInTalk
                    PlanCategory.UNLIMITED -> Icons.Default.AllInclusive
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ElectricBlue else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategorySelected(category) }
                        .testTag("tab_${title.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contextual dynamic category banner highlighting benefits for Data, Talktime, or Unlimited.
 */
@Composable
private fun CategoryOverviewBanner(category: PlanCategory) {
    val (title, description, accentColor) = when (category) {
        PlanCategory.DATA -> Triple(
            "High-Speed 4G/5G Data Packs",
            "Bulk quota vouchers, daily boosters, and post-midnight unlimited streaming.",
            CyanAccent
        )
        PlanCategory.TALKTIME -> Triple(
            "Guaranteed Talktime Balance",
            "100% full value calling credit with unlimited validity & discounted international rates.",
            AmberGold
        )
        PlanCategory.UNLIMITED -> Triple(
            "Truly Unlimited 5G & Calling",
            "Zero speed caps, unlimited HD calls to all networks & bundled premium OTT subscriptions.",
            EmeraldGreen
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F1A2C),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_overview_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (category) {
                            PlanCategory.DATA -> Icons.Default.Bolt
                            PlanCategory.TALKTIME -> Icons.Default.PhoneInTalk
                            PlanCategory.UNLIMITED -> Icons.Default.WorkspacePremium
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBACDE8)
                )
            }
        }
    }
}

/**
 * Rich plan card highlighting Pricing, Validity, Talktime Value, Data Quota, and 1-tap checkout.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorizedPlanCard(
    plan: RechargePlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val cardBackground = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .testTag("categorized_plan_card_${plan.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Pricing + Validity + Badge Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = PaymentGatewayManager.formatCurrency(plan.price),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (plan.pricePerDay.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = plan.pricePerDay,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (plan.badgeTag.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = plan.badgeTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Validity Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = plan.validity,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Metrics Section tailored to Plan Category
            when (plan.category) {
                PlanCategory.DATA -> {
                    DataMetricsRow(plan = plan)
                }
                PlanCategory.TALKTIME -> {
                    TalktimeMetricsRow(plan = plan)
                }
                PlanCategory.UNLIMITED -> {
                    UnlimitedMetricsRow(plan = plan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detailed plan description
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // Perks & OTT Badges
            if (plan.ottPerks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    plan.ottPerks.forEach { perk ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = perk,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instant Carrier Activation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isSelected) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("plan_selected_btn_${plan.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Selected", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("recharge_now_btn_${plan.id}")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recharge ${PaymentGatewayManager.formatCurrency(plan.price)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DataMetricsRow(plan: RechargePlan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricTile(
            label = "DATA QUOTA",
            value = plan.data,
            tint = ElectricBlue,
            modifier = Modifier.weight(1.2f)
        )
        MetricTile(
            label = "SPEED TIER",
            value = plan.speedTier,
            tint = CyanAccent,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "TALKTIME",
            value = plan.talktimeValue.ifEmpty { "Data Pack" },
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TalktimeMetricsRow(plan: RechargePlan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricTile(
            label = "CALLING BALANCE",
            value = plan.talktimeValue.ifEmpty { plan.voice },
            tint = AmberGold,
            modifier = Modifier.weight(1.3f)
        )
        MetricTile(
            label = "CALL TARIFF",
            value = "Standard / ISD",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "DATA QUOTA",
            value = plan.data,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UnlimitedMetricsRow(plan: RechargePlan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricTile(
            label = "VOICE CALLS",
            value = "Truly Unlimited",
            tint = EmeraldGreen,
            modifier = Modifier.weight(1.2f)
        )
        MetricTile(
            label = "DATA BENEFIT",
            value = plan.data,
            tint = ElectricBlue,
            modifier = Modifier.weight(1.2f)
        )
        MetricTile(
            label = "DAILY SMS",
            value = plan.sms,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = tint.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
