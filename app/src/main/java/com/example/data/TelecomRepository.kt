package com.example.data

import com.example.model.Bank
import com.example.model.Circle
import com.example.model.Coupon
import com.example.model.DigitalWallet
import com.example.model.Operator
import com.example.model.PlanCategory
import com.example.model.RechargePlan
import com.example.model.RecentContact
import com.example.model.SavedCard

object TelecomRepository {

    val operators = listOf(
        Operator(
            id = "jio",
            name = "Jio 5G",
            code = "JIO",
            brandColorHex = 0xFF0D52FE,
            samplePrefixes = listOf("98", "99", "70", "79", "63")
        ),
        Operator(
            id = "airtel",
            name = "Airtel",
            code = "AIR",
            brandColorHex = 0xFFE50914,
            samplePrefixes = listOf("91", "95", "96", "81", "88")
        ),
        Operator(
            id = "vi",
            name = "Vodafone Idea",
            code = "VI",
            brandColorHex = 0xFFFF7700,
            samplePrefixes = listOf("94", "97", "84", "89", "78")
        ),
        Operator(
            id = "bsnl",
            name = "BSNL 4G",
            code = "BSNL",
            brandColorHex = 0xFF009688,
            samplePrefixes = listOf("94", "93", "83")
        ),
        Operator(
            id = "verizon",
            name = "Verizon",
            code = "VZ",
            brandColorHex = 0xFFCD040B,
            samplePrefixes = listOf("20", "21", "30", "41")
        ),
        Operator(
            id = "att",
            name = "AT&T",
            code = "ATT",
            brandColorHex = 0xFF00A8E0,
            samplePrefixes = listOf("51", "52", "61", "71")
        ),
        Operator(
            id = "tmobile",
            name = "T-Mobile",
            code = "TM",
            brandColorHex = 0xFFEA0A8E,
            samplePrefixes = listOf("80", "82", "85")
        )
    )

    val circles = listOf(
        Circle("nat", "National / All Circles"),
        Circle("ca", "California / West Coast"),
        Circle("ny", "New York / Tri-State"),
        Circle("tx", "Texas / Central"),
        Circle("del", "Delhi NCR"),
        Circle("mum", "Mumbai"),
        Circle("blr", "Karnataka / Bangalore")
    )

    val sampleContacts = listOf(
        RecentContact(
            id = "c1",
            name = "Mom",
            phoneNumber = "9876543210",
            operatorId = "jio",
            relationTag = "Family"
        ),
        RecentContact(
            id = "c2",
            name = "Work Phone",
            phoneNumber = "9123456789",
            operatorId = "airtel",
            relationTag = "Office"
        ),
        RecentContact(
            id = "c3",
            name = "Alex (Self)",
            phoneNumber = "9820123456",
            operatorId = "jio",
            relationTag = "Primary"
        ),
        RecentContact(
            id = "c4",
            name = "Dad",
            phoneNumber = "9765432109",
            operatorId = "vi",
            relationTag = "Family"
        )
    )

    val coupons = listOf(
        Coupon(
            code = "RECHARGE50",
            discount = 50.00,
            minOrder = 299.00,
            description = "Get ₹50 flat cashback on orders above ₹299"
        ),
        Coupon(
            code = "FLAT20",
            discount = 20.00,
            minOrder = 149.00,
            description = "Save ₹20 instantly on any recharge above ₹149"
        ),
        Coupon(
            code = "SUPER5G",
            discount = 30.00,
            minOrder = 349.00,
            description = "Save ₹30 on high-speed Unlimited 5G plans"
        )
    )

    val savedCards = listOf(
        SavedCard(
            id = "card1",
            cardholderName = "Alex Rivera",
            maskedNumber = "•••• •••• •••• 4242",
            network = "VISA",
            expiry = "08/29",
            isDefault = true
        ),
        SavedCard(
            id = "card2",
            cardholderName = "Alex Rivera",
            maskedNumber = "•••• •••• •••• 8812",
            network = "MASTERCARD",
            expiry = "11/27",
            isDefault = false
        )
    )

    val digitalWallets = listOf(
        DigitalWallet(
            id = "gpay",
            name = "Google Pay",
            subtext = "Fast, encrypted UPI & card payment",
            iconType = "GPAY"
        ),
        DigitalWallet(
            id = "apple_phonepe",
            name = "Apple Pay / PhonePe",
            subtext = "Instant 1-tap authorization",
            iconType = "PHONEPE"
        ),
        DigitalWallet(
            id = "paypal",
            name = "PayPal / Paytm",
            subtext = "Secure buyer protection",
            iconType = "PAYPAL"
        ),
        DigitalWallet(
            id = "amazon",
            name = "Amazon Pay",
            subtext = "Use Amazon wallet balance & rewards",
            iconType = "AMAZON"
        )
    )

    val popularBanks = listOf(
        Bank("chase", "Chase Bank", "CHASE"),
        Bank("bofa", "Bank of America", "BOFA"),
        Bank("wells", "Wells Fargo", "WFC"),
        Bank("citi", "Citibank", "CITI"),
        Bank("hdfc", "HDFC Bank", "HDFC"),
        Bank("icici", "ICICI Bank", "ICICI"),
        Bank("sbi", "State Bank of India", "SBI")
    )

    fun getPlansForOperator(operatorId: String): List<RechargePlan> {
        return listOf(
            // --- DATA CATEGORY ---
            RechargePlan(
                id = "${operatorId}_data_1",
                operatorId = operatorId,
                category = PlanCategory.DATA,
                price = 299.00,
                validity = "28 Days",
                data = "2.0 GB / Day",
                voice = "Data Centric Pack",
                sms = "100 SMS / Day",
                talktimeValue = "Data Primary",
                pricePerDay = "₹10.68 / day",
                description = "High-speed daily data allowance with post-midnight unlimited usage & weekend data rollover",
                ottPerks = listOf("Binge All Night", "Weekend Rollover"),
                isPopular = true,
                badgeTag = "POPULAR"
            ),
            RechargePlan(
                id = "${operatorId}_data_2",
                operatorId = operatorId,
                category = PlanCategory.DATA,
                price = 65.00,
                validity = "30 Days",
                data = "4 GB High-Speed",
                voice = "Data Only",
                sms = "NA",
                talktimeValue = "No Talktime (Data Voucher)",
                pricePerDay = "₹2.17 / day",
                description = "Seamless high-speed data booster for gaming, zoom meetings and 4K streaming",
                ottPerks = listOf("5G Speed Boost"),
                badgeTag = "BOOSTER"
            ),
            RechargePlan(
                id = "${operatorId}_data_3",
                operatorId = operatorId,
                category = PlanCategory.DATA,
                price = 499.00,
                validity = "56 Days",
                data = "50 GB Bulk Quota",
                voice = "Data Only",
                sms = "NA",
                talktimeValue = "No Talktime",
                pricePerDay = "₹8.91 / day",
                description = "Uncapped speed bulk bucket with zero daily speed caps and hotspot sharing",
                ottPerks = listOf("Cloud Storage 100GB", "Hotspot Enabled"),
                badgeTag = "BULK DATA"
            ),
            RechargePlan(
                id = "${operatorId}_data_4",
                operatorId = operatorId,
                category = PlanCategory.DATA,
                price = 19.00,
                validity = "1 Day",
                data = "1.5 GB Instant",
                voice = "NA",
                sms = "NA",
                talktimeValue = "Instant Booster",
                pricePerDay = "One-Time",
                description = "Instant emergency top-up valid until your current active telecom billing cycle ends"
            ),
            RechargePlan(
                id = "${operatorId}_data_5",
                operatorId = operatorId,
                category = PlanCategory.DATA,
                price = 359.00,
                validity = "30 Days",
                data = "Uncapped 5G High-Speed",
                voice = "Data Only",
                sms = "100 SMS",
                talktimeValue = "Gigabit Data",
                pricePerDay = "₹11.97 / day",
                description = "High-priority gigabit bandwidth for streamers, content creators & gamers",
                ottPerks = listOf("Uncapped 5G", "Game Pass Access"),
                badgeTag = "5G PRO"
            ),

            // --- TALKTIME CATEGORY ---
            RechargePlan(
                id = "${operatorId}_tt_1",
                operatorId = operatorId,
                category = PlanCategory.TALKTIME,
                price = 10.00,
                validity = "Unlimited Validity",
                data = "NA",
                voice = "₹7.47 Talktime",
                sms = "Standard Rates",
                talktimeValue = "₹7.47 Calling Balance",
                pricePerDay = "No Expiry",
                description = "Affordable core talktime voucher with lifetime unlimited validity",
                isPopular = false,
                badgeTag = "TOP-UP"
            ),
            RechargePlan(
                id = "${operatorId}_tt_2",
                operatorId = operatorId,
                category = PlanCategory.TALKTIME,
                price = 100.00,
                validity = "Unlimited Validity",
                data = "NA",
                voice = "₹100.00 Full Talktime",
                sms = "Standard Rates",
                talktimeValue = "₹100.00 Calling Balance",
                pricePerDay = "No Expiry",
                description = "100% full value talktime credited straight to primary calling balance with zero deductions",
                isPopular = true,
                badgeTag = "FULL TALKTIME"
            ),
            RechargePlan(
                id = "${operatorId}_tt_3",
                operatorId = operatorId,
                category = PlanCategory.TALKTIME,
                price = 20.00,
                validity = "Unlimited Validity",
                data = "1 GB Bonus Data",
                voice = "₹22.00 Talktime (₹2 Extra)",
                sms = "100 Free SMS",
                talktimeValue = "₹22.00 Calling Balance",
                pricePerDay = "No Expiry",
                description = "Get extra ₹2 promotional calling bonus for all domestic and overseas outgoing calls",
                ottPerks = listOf("₹2 Bonus Credit", "1 GB Free Data"),
                isPopular = true,
                badgeTag = "BONUS ₹2"
            ),
            RechargePlan(
                id = "${operatorId}_tt_4",
                operatorId = operatorId,
                category = PlanCategory.TALKTIME,
                price = 50.00,
                validity = "Unlimited Validity",
                data = "NA",
                voice = "₹39.37 Standard Talktime",
                sms = "Standard Rates",
                talktimeValue = "₹39.37 Calling Balance",
                pricePerDay = "No Expiry",
                description = "Pocket-friendly top-up voucher for urgent outgoing calls with lifetime validity"
            ),
            RechargePlan(
                id = "${operatorId}_tt_5",
                operatorId = operatorId,
                category = PlanCategory.TALKTIME,
                price = 500.00,
                validity = "Unlimited Validity",
                data = "5 GB Bonus Data",
                voice = "₹550.00 Talktime (₹50 Extra)",
                sms = "300 SMS",
                talktimeValue = "₹550.00 Calling Balance",
                pricePerDay = "No Expiry",
                description = "Heavy caller executive talktime pack with ₹50 bonus value and bonus mobile data",
                ottPerks = listOf("₹50 Bonus Credit", "5 GB Data"),
                badgeTag = "EXECUTIVE"
            ),

            // --- UNLIMITED CATEGORY ---
            RechargePlan(
                id = "${operatorId}_unl_1",
                operatorId = operatorId,
                category = PlanCategory.UNLIMITED,
                price = 349.00,
                validity = "28 Days",
                data = "Unlimited 5G Data",
                voice = "Truly Unlimited Voice",
                sms = "100 SMS / Day",
                talktimeValue = "Unlimited Calls",
                pricePerDay = "₹12.46 / day",
                description = "True unlimited 5G data with zero speed throttle, unlimited HD calling to any network",
                ottPerks = listOf("Disney+ Hotstar", "True 5G Unlimited"),
                isPopular = true,
                badgeTag = "BESTSELLER"
            ),
            RechargePlan(
                id = "${operatorId}_unl_2",
                operatorId = operatorId,
                category = PlanCategory.UNLIMITED,
                price = 719.00,
                validity = "84 Days",
                data = "1.5 GB / Day + True 5G",
                voice = "Truly Unlimited Voice",
                sms = "100 SMS / Day",
                talktimeValue = "Unlimited 3 Months",
                pricePerDay = "₹8.56 / day",
                description = "Quarterly power pack: 3 full months of uninterrupted daily data and unlimited nationwide calls",
                ottPerks = listOf("Netflix Mobile", "Prime Video Lite", "JioCinema Premium"),
                isPopular = true,
                badgeTag = "VALUE 84 DAYS"
            ),
            RechargePlan(
                id = "${operatorId}_unl_3",
                operatorId = operatorId,
                category = PlanCategory.UNLIMITED,
                price = 2999.00,
                validity = "365 Days",
                data = "2.5 GB / Day + Unlimited 5G",
                voice = "Unlimited Calls All Year",
                sms = "100 SMS / Day",
                talktimeValue = "Unlimited 365 Days",
                pricePerDay = "₹8.22 / day",
                description = "Recharge once, stay connected all 365 days. Save 35% compared to recurring monthly recharges",
                ottPerks = listOf("1 Year Disney+ Hotstar", "1 Year Prime", "Cloud Backup"),
                isPopular = true,
                badgeTag = "ANNUAL VIP"
            ),
            RechargePlan(
                id = "${operatorId}_unl_4",
                operatorId = operatorId,
                category = PlanCategory.UNLIMITED,
                price = 199.00,
                validity = "24 Days",
                data = "1.5 GB / Day",
                voice = "Truly Unlimited Voice",
                sms = "100 SMS / Day",
                talktimeValue = "Unlimited Calls",
                pricePerDay = "₹8.29 / day",
                description = "Budget starter pack with daily internet quota and unlimited local & STD calling"
            ),
            RechargePlan(
                id = "${operatorId}_unl_5",
                operatorId = operatorId,
                category = PlanCategory.UNLIMITED,
                price = 999.00,
                validity = "84 Days",
                data = "3.0 GB / Day + Unlimited 5G",
                voice = "Truly Unlimited Voice",
                sms = "100 SMS / Day",
                talktimeValue = "Unlimited Calls",
                pricePerDay = "₹11.89 / day",
                description = "Heavy internet & calling consumption plan with full OTT entertainment bundle",
                ottPerks = listOf("Netflix Standard", "Hotstar Premium", "Priority QOS"),
                badgeTag = "ENTERTAINMENT"
            )
        )
    }

    /**
     * Auto-detect operator based on phone number prefix
     */
    fun detectOperator(number: String): Operator {
        val cleanNumber = number.filter { it.isDigit() }
        if (cleanNumber.length >= 2) {
            val prefix = cleanNumber.take(2)
            operators.forEach { op ->
                if (op.samplePrefixes.contains(prefix)) {
                    return op
                }
            }
        }
        return operators.first()
    }
}
