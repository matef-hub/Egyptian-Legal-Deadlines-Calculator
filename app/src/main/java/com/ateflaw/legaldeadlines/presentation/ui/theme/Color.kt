package com.ateflaw.legaldeadlines.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// Vintage Technical Legal Palette
val VintageBackground = Color(0xFFEFE9DC) // Vintage parchment paper texture/color
val VintageFieldFill = Color(0xFFE6DEC8) // Field & Card Fill (Apply alpha = 0.55f at usage)
val JudicialNavy = Color(0xFF152C3E) // Deep Judicial Navy Blue (Primary Ink)
val AntiqueBrass = Color(0xFFA6844E) // Accent Gold/Brass
val TerracottaSeal = Color(0xFF984432) // Urgent / Friday / Holiday Seal (Terracotta red wax seal)
val BlueprintSteel = Color(0xFF5A6B78) // Technical Stroke (Steel blueprint outline, 1.2.dp)
val VerifiedEmerald = Color(0xFF2E6F4E) // Verified Status (Deep emerald green)

// Mapping vintage colors to Material Theme concepts (Light)
val PrimaryLegal = JudicialNavy
val OnPrimaryLegal = VintageBackground
val PrimaryContainerLegal = AntiqueBrass.copy(alpha = 0.2f)
val OnPrimaryContainerLegal = JudicialNavy

val SecondaryGold = AntiqueBrass
val OnSecondaryGold = VintageBackground
val SecondaryContainerGold = VintageFieldFill.copy(alpha = 0.55f)
val OnSecondaryContainerGold = JudicialNavy

val TertiaryEmerald = VerifiedEmerald
val OnTertiaryEmerald = VintageBackground
val TertiaryContainerEmerald = VerifiedEmerald.copy(alpha = 0.15f)
val OnTertiaryContainerEmerald = VerifiedEmerald

val BackgroundWarm = VintageBackground
val OnBackgroundDark = JudicialNavy

val SurfaceWarm = VintageBackground
val OnSurfaceDark = JudicialNavy
val SurfaceVariantWarm = VintageFieldFill.copy(alpha = 0.55f)
val OnSurfaceVariantDark = BlueprintSteel

val OutlineBorder = BlueprintSteel
val ErrorRed = TerracottaSeal
val OnErrorRed = VintageBackground
val ErrorContainerRed = TerracottaSeal.copy(alpha = 0.15f)
val OnErrorContainerRed = TerracottaSeal

// Dark theme accents (Optional, keeping aligned with Vintage theme or standard dark mode)
val PrimaryLegalDark = Color(0xFFA8C8FF)
val OnPrimaryLegalDark = Color(0xFF003062)
val BackgroundDark = Color(0xFF111418)
val SurfaceDark = Color(0xFF1A1D21)
val SurfaceVariantDark = Color(0xFF2E3136)
val OnBackgroundLight = Color(0xFFE2E2E6)
val OnSurfaceLight = Color(0xFFE2E2E6)
val OutlineDark = BlueprintSteel

// Dark theme - Secondary
val SecondaryGoldDark = AntiqueBrass
val OnSecondaryGoldDark = Color(0xFF402D04)
val SecondaryContainerGoldDark = Color(0xFF5A4300)
val OnSecondaryContainerGoldDark = Color(0xFFFAF5E8)

// Dark theme - Tertiary
val TertiaryEmeraldDark = Color(0xFF81D79E)
val OnTertiaryEmeraldDark = Color(0xFF00391A)
val TertiaryContainerEmeraldDark = Color(0xFF005228)
val OnTertiaryContainerEmeraldDark = Color(0xFF9DF4B9)

// Dark theme - Error
val ErrorRedDark = Color(0xFFFFB4AB)
val OnErrorRedDark = Color(0xFF690005)
val ErrorContainerRedDark = Color(0xFF93000A)
val OnErrorContainerRedDark = Color(0xFFFFDAD6)
