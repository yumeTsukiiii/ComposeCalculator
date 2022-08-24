package fan.yumetsuki.composecalculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fan.yumetsuki.composecalculator.pages.Calculator
import fan.yumetsuki.composecalculator.ui.theme.ComposeCalculatorTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCalculatorTheme {
                // A surface container using the 'background' color from the theme
                CalculatorApp(windowSizeClass = calculateWindowSizeClass(activity = this))
            }
        }
    }
}

@Composable
fun CalculatorApp(windowSizeClass: WindowSizeClass) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
            ) {
                var dropDownExpanded by remember {
                    mutableStateOf(false)
                }
                TextButton(onClick = {
                    Toast.makeText(context, "什么也没有！", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "😀", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { dropDownExpanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "更多")
                    DropdownMenu(
                        expanded = dropDownExpanded,
                        offset = DpOffset(0.dp, (-56).dp),
                        onDismissRequest = { dropDownExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            Toast.makeText(context, "什么也没有！", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = "设置")
                        }
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Calculator(windowSizeClass)
        }
    }

}