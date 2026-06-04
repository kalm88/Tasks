package com.punchlist.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.punchlist.app.data.model.Status

@Composable
fun StatusChip(status: Status, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, status.color),
        color = status.color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = status.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
