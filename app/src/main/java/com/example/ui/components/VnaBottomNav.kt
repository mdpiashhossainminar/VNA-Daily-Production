package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VnaDivider
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaTextSecondary
import com.example.ui.theme.VnaYellow

@Composable
fun VnaBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("শুরু", "স্ক্যান", "হিসাব ও প্রিন্ট")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VnaNavyBackground)
            .navigationBarsPadding()
    ) {
        // Subtle divider on top of bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(VnaDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onTabSelected(index)
                        }
                        .testTag("tab_${index}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Active yellow line on top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (isSelected) VnaYellow else Color.Transparent)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) VnaYellow else VnaTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
