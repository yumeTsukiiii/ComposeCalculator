package fan.yumetsuki.composecalculator.pages

import android.widget.Toast
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fan.yumetsuki.composecalculator.atri_script.AtriScriptInterpreter
import fan.yumetsuki.composecalculator.components.*


@Composable
fun NumberText(text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    ScaleAnimationTextButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(56.dp, 56.dp),
        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    ) {
        if (content != null) {
            content()
        } else {
            Text(text = text, fontSize = 28.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun OperatorText(text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    ScaleAnimationTextButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(56.dp, 56.dp),
        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    ) {
        if (content != null) {
            content()
        } else {
            Text(text = text, fontSize = 28.sp)
        }
    }
}

@Composable
fun ConfirmText(text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    ScaleAnimationButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(56.dp, 56.dp),
        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50))
    ) {
        if (content != null) {
            content()
        } else {
            Text(text = text, fontSize = 28.sp)
        }
    }
}

@Composable
fun KeyBoardNumberText(text: String = "", content: (@Composable () -> Unit)? = null,  onClick: () -> Unit) {
    FillCenterBox {
        NumberText(text = text, content, onClick)
    }
}

@Composable
fun KeyBoardOperatorText(text: String = "", content: (@Composable () -> Unit)? = null,  onClick: () -> Unit) {
    FillCenterBox {
        OperatorText(text = text, content, onClick)
    }
}

@Composable
fun KeyBoardConfirmText(text: String = "", content: (@Composable () -> Unit)? = null,  onClick: () -> Unit) {
    FillCenterBox {
        ConfirmText(text = text, content, onClick)
    }
}

fun SimpleFixedGridScope.keyBoardNumberTextItem(span: Int = 1, text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    item(span) {
        KeyBoardNumberText(text, content, onClick)
    }
}

fun SimpleFixedGridScope.keyBoardOperatorTextItem(span: Int = 1, text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    item(span) {
        KeyBoardOperatorText(text, content, onClick)
    }
}

fun SimpleFixedGridScope.keyBoardConfirmTextItem(span: Int = 1, text: String = "", content: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    item(span) {
        KeyBoardConfirmText(text, content, onClick)
    }
}

const val NOT_CHANGE = "not_change"
const val ERROR = "出错"

@Composable
fun CalculateResultPanel(
    modifier: Modifier = Modifier,
    expression: String = "",
    forceAnalyze: Boolean = false,
    onForceResult: (expr: String, showingResult: String) -> Unit
) {

    var showingResult by remember {
        mutableStateOf("")
    }

    fun analyzeExpression(expression: String): String {
        return AtriScriptInterpreter().eval(expression)
    }

    fun Char.isOperator() = this in arrayOf('+', '-', '*', '/', '%')

    val calculateResult: String = remember(expression, forceAnalyze) {
        val replacedExpr = expression.map {
            when(it) {
                '×' -> '*'
                '÷' -> '/'
                else -> it
            }
        }.joinToString("")
        if (forceAnalyze) {
            return@remember analyzeExpression(replacedExpr)
        }
        // 移除末尾的操作符再进行计算
        var toCalExpr = replacedExpr
        while (toCalExpr.isNotEmpty() && toCalExpr.last().isOperator()) {
            toCalExpr = toCalExpr.dropLast(1)
        }
        if (toCalExpr.isEmpty()) {
            return@remember ""
        }
        when {
            toCalExpr.last().isDigit() -> analyzeExpression(toCalExpr)
            toCalExpr.last() == '.' -> analyzeExpression(toCalExpr)
            else -> ERROR
        }
    }

    val focusResult = remember(showingResult, forceAnalyze) {
        if (forceAnalyze) {
            true
        } else {
            showingResult.isEmpty()
        }
    }

    val animationExprFontSize by animateIntAsState(targetValue = if (focusResult) 24 else 48)
    val animationResultFontSize by animateIntAsState(targetValue = if (focusResult) 48 else 24)

    LaunchedEffect(calculateResult) {
        when {
            // 不重刷新
            calculateResult == NOT_CHANGE -> {}
            calculateResult.isEmpty() -> { showingResult = "" }
            calculateResult.toDoubleOrNull() != null -> showingResult = if (calculateResult.toDouble() == calculateResult.toDouble().toLong().toDouble()) {
                calculateResult.toDouble().toLong().toString()
            } else {
                calculateResult
            }
            else -> {
                if (forceAnalyze) {
                    showingResult = "出错"
                }
            }
        }
    }

    LaunchedEffect(showingResult, forceAnalyze, expression) {
        if (forceAnalyze) {
            onForceResult(expression, showingResult)
        }
    }

    val exprScrollableState = rememberScrollState()
    val resultTextScrollableState = rememberScrollState()

    Column(modifier = modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    state = exprScrollableState
                ),
            text = expression,
            fontSize = animationExprFontSize.sp,
            color = if (focusResult) Color.Gray else Color.DarkGray,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    state = resultTextScrollableState
                ),
            text = if (showingResult.isEmpty()) "0" else "= $showingResult",
            fontSize = animationResultFontSize.sp ,
            color = if (focusResult) Color.DarkGray else Color.Gray,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
fun KeyBoardPanel(
    modifier: Modifier = Modifier,
    expression: String,
    forceAnalyze: Boolean,
    onExpressionChange: (expression: String) -> Unit,
    onForceAnalyzeChange: (forceAnalyze: Boolean) -> Unit,
    historyCalculateResult: List<Pair<String, String>>
) {

    fun String.isOperator() = this in arrayOf("+", "-", "×", "÷", "%")

    fun addToExpression(text: String) {
        onExpressionChange(
            (
                if (forceAnalyze) {
                    val lastHistory = historyCalculateResult.lastOrNull()
                    if (lastHistory?.second?.toDoubleOrNull() != null) {
                        lastHistory.second + text
                    } else {
                        text
                    }
                } else {
                    expression + text
                }
            ).let { newExpr ->
                if (newExpr.isOperator()) {
                    "0$newExpr"
                } else {
                    newExpr
                }
            }
        )
    }

    val context = LocalContext.current

    SimpleFixedGird(modifier = modifier, row = 5, column = 4) {
        keyBoardOperatorTextItem(text = "C", onClick = {
            onExpressionChange("")
        })
        keyBoardOperatorTextItem(onClick = {
            onExpressionChange(
                if (expression.isNotEmpty()) {
                    expression.dropLast(1)
                } else {
                    expression
                }
            )
        }, content = {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "删除符号")
        })
        keyBoardOperatorTextItem(text = "%", onClick = { addToExpression("%") })
        keyBoardOperatorTextItem(text = "÷", onClick = { addToExpression("÷") })
        keyBoardNumberTextItem(text = "7", onClick = { addToExpression("7") })
        keyBoardNumberTextItem(text = "8", onClick = { addToExpression("8") })
        keyBoardNumberTextItem(text = "9", onClick = { addToExpression("9") })
        keyBoardOperatorTextItem(text = "×", onClick = { addToExpression("×") })
        keyBoardNumberTextItem(text = "4", onClick = { addToExpression("4") })
        keyBoardNumberTextItem(text = "5", onClick = { addToExpression("5") })
        keyBoardNumberTextItem(text = "6", onClick = { addToExpression("6") })
        keyBoardOperatorTextItem(text = "-", onClick = { addToExpression("-") })
        keyBoardNumberTextItem(text = "1", onClick = { addToExpression("1") })
        keyBoardNumberTextItem(text = "2", onClick = { addToExpression("2") })
        keyBoardNumberTextItem(text = "3", onClick = { addToExpression("3") })
        keyBoardOperatorTextItem(text = "+", onClick = { addToExpression("+") })
        keyBoardNumberTextItem(onClick = {
            Toast.makeText(context, "感谢点赞！！！", Toast.LENGTH_SHORT).show()
        }, content = {
            Icon(imageVector = Icons.Outlined.Star, contentDescription = "点赞")
        })
        keyBoardNumberTextItem(text = "0", onClick = { addToExpression("0") })
        keyBoardNumberTextItem(text = ".", onClick = { addToExpression(".") })
        keyBoardConfirmTextItem(text = "=", onClick = { onForceAnalyzeChange(true) })
    }
}

@Composable
fun Calculator(
    windowSizeClass: WindowSizeClass
) {

    var expression by remember {
        mutableStateOf("")
    }

    var forceAnalyze by remember {
        mutableStateOf(false)
    }

    var historyCalculateResult by remember {
        // expr to result
        mutableStateOf(listOf<Pair<String, String>>())
    }

    fun handleForceResult(expr: String, result: String) {
        historyCalculateResult = listOf(
            *historyCalculateResult.toTypedArray(),
            expr to result
        )
    }

    fun handleExpressionChange(expr: String) {
        expression = expr
        forceAnalyze = false
    }

    fun handleForceAnalyzeChange(newForceAnalyze: Boolean) {
        forceAnalyze = newForceAnalyze
    }

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalculateResultPanel(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.8f)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                expression = expression,
                forceAnalyze = forceAnalyze,
                onForceResult = ::handleForceResult
            )

            VerticalDivider(modifier = Modifier.padding(vertical = 16.dp))

            KeyBoardPanel(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.0f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                expression = expression,
                forceAnalyze = forceAnalyze,
                onExpressionChange = ::handleExpressionChange,
                onForceAnalyzeChange = ::handleForceAnalyzeChange,
                historyCalculateResult = historyCalculateResult
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CalculateResultPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.8f)
                    .padding(horizontal = 24.dp),
                expression = expression,
                forceAnalyze = forceAnalyze,
                onForceResult = ::handleForceResult
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            KeyBoardPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.0f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                expression = expression,
                forceAnalyze = forceAnalyze,
                onExpressionChange = ::handleExpressionChange,
                onForceAnalyzeChange = ::handleForceAnalyzeChange,
                historyCalculateResult = historyCalculateResult
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}