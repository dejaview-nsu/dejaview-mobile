package ru.dejaview.core.ui.theme

import androidx.annotation.FontRes
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import ru.dejaview.core.ui.R

object DejaViewType {
    private val unbounded =
        FontFamily(
            variableFont(R.font.unbounded, FontWeight.SemiBold),
            variableFont(R.font.unbounded, FontWeight.Bold),
        )
    private val onest =
        FontFamily(
            variableFont(R.font.onest, FontWeight.Normal),
            variableFont(R.font.onest, FontWeight.Medium),
            variableFont(R.font.onest, FontWeight.SemiBold),
        )

    val display = style(unbounded, FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp)
    val headline = style(unbounded, FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp)
    val heading = style(unbounded, FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)
    val subheading = style(unbounded, FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp)
    val title = style(onest, FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp)
    val label = style(onest, FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp)
    val button = style(onest, FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
    val body = style(onest, FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp)
    val bodyMedium = style(onest, FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodySmall = style(onest, FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
    val bodySmallMedium = style(onest, FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
    val caption = style(onest, FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
    val overline = style(onest, FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)

    private fun style(
        family: FontFamily,
        weight: FontWeight,
        fontSize: TextUnit,
        lineHeight: TextUnit,
    ) = TextStyle(fontFamily = family, fontWeight = weight, fontSize = fontSize, lineHeight = lineHeight)

    @OptIn(ExperimentalTextApi::class)
    private fun variableFont(
        @FontRes resource: Int,
        weight: FontWeight,
    ) = Font(
        resource,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )
}
