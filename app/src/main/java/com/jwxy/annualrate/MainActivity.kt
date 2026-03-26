package com.jwxy.annualrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jwxy.annualrate.ui.AprScreen
import com.jwxy.annualrate.ui.theme.AnnualrateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnualrateTheme {
                AprScreen()
            }
        }
    }
}


@Composable
fun CustomCircularProgress(
    progress: Float, // 进度，0.0f 到 1.0f
    modifier: Modifier = Modifier,
    strokeWidth: Float = 10f,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
) {
    // Canvas 提供了一个 DrawScope，你可以在其中执行绘制命令
    Canvas(modifier = modifier) {
        val sweepAngle = progress * 360f

        // 1. 绘制背景圆环
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )

        // 2. 绘制前景进度圆弧
        drawArc(
            color = foregroundColor,
            startAngle = -90f, // 从顶部开始
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

@Preview
@Composable
fun CustomCircularProgressPreview() {
    MaterialTheme {
        CustomCircularProgress(
            progress = 0.60f,
            modifier = Modifier.size(100.dp)
        )
    }
}