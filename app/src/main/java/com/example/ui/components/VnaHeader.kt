package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.VnaNavyBackground
import com.example.ui.theme.VnaNavyCard
import com.example.ui.theme.VnaTextDark
import com.example.ui.theme.VnaTextPrimary
import com.example.ui.theme.VnaYellow

@Composable
fun VnaLogo(
    modifier: Modifier = Modifier,
    size: Int = 36
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.22).dp))
            .background(VnaYellow),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "VNA",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.38).sp,
            color = VnaTextDark
        )
    }
}

@Composable
fun VnaHeader(
    onAboutClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(VnaNavyBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VnaLogo(size = 36)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "VNA Daily Production",
            color = VnaTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("header_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = VnaTextPrimary
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(VnaNavyCard)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "অ্যাবাউট",
                            color = VnaTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onAboutClick()
                    },
                    modifier = Modifier.testTag("menu_about_item")
                )
            }
        }
    }
}
