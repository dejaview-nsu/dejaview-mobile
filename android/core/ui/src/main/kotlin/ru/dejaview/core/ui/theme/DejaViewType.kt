package ru.dejaview.core.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
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

    val display = style(unbounded, FontWeight.Bold, 30, 36)
    val headline = style(unbounded, FontWeight.Bold, 26, 32)
    val heading = style(unbounded, FontWeight.SemiBold, 22, 28)
    val subheading = style(unbounded, FontWeight.SemiBold, 17, 22)
    val title = style(onest, FontWeight.SemiBold, 16, 22)
    val label = style(onest, FontWeight.SemiBold, 15, 22)
    val button = style(onest, FontWeight.SemiBold, 16, 20)
    val body = style(onest, FontWeight.Normal, 15, 22)
    val bodyMedium = style(onest, FontWeight.Normal, 14, 20)
    val bodySmall = style(onest, FontWeight.Normal, 13, 18)
    val bodySmallMedium = style(onest, FontWeight.Medium, 13, 18)
    val caption = style(onest, FontWeight.Normal, 12, 16)
    val overline = style(onest, FontWeight.SemiBold, 12, 16)

    private fun style(
        family: FontFamily,
        weight: FontWeight,
        size: Int,
        lineHeight: Int,
    ) = TextStyle(fontFamily = family, fontWeight = weight, fontSize = size.sp, lineHeight = lineHeight.sp)

    @OptIn(ExperimentalTextApi::class)
    private fun variableFont(
        resource: Int,
        weight: FontWeight,
    ) = Font(resource, weight = weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
}
