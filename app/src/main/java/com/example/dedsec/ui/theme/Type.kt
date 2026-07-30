package com.example.dedsec.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.dedsec.R

val PixellariFont = FontFamily(Font(R.font.pixellari))

val DedSecTypography = Typography(
    bodyLarge = TextStyle(fontFamily = PixellariFont, fontSize = 16.sp),
    titleLarge = TextStyle(fontFamily = PixellariFont, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = PixellariFont, fontSize = 18.sp)
)