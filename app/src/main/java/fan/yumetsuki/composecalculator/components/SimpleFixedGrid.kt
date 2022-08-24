package fan.yumetsuki.composecalculator.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.min

interface SimpleFixedGridScope {

    fun item(span: Int = 1, content: @Composable () -> Unit)

}

class ContentData(
    val span: Int,
    val content: @Composable () -> Unit
)

@Composable
fun SimpleFixedGird(
    modifier: Modifier = Modifier,
    row: Int,
    column: Int,
    content: SimpleFixedGridScope.() -> Unit
) {
    if (row <= 0 || column <= 0) {
        error("row | column 必须大于0")
    }

    // TODO 注意性能，使用 remember 避免频繁重建
    val children = arrayListOf<ContentData>()
    val scope = object : SimpleFixedGridScope {
        override fun item(span: Int, content: @Composable () -> Unit) {
            children.add(
                ContentData(span, content)
            )
        }
    }
    // 统计子组件
    content(scope)
    val childIterator = children.iterator()
    Box(modifier = modifier) {
        Column {
            // 按 row 和 column 构建行列布局组件
            for (rowIndex in 0 until row) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    var columnIndex = 0
                    while (columnIndex < column) {
                        if (!childIterator.hasNext()) {
                            Box(modifier = Modifier.weight(1f)) {}
                            columnIndex += 1
                            continue
                        }
                        val contentData = childIterator.next()
                        // 判断这一行是不是已经全部被占用，没有空间了则不渲染
                        val span = min(contentData.span, column - columnIndex + 1)
                        if (span <= 0) {
                            continue
                        }
                        Box(modifier = Modifier.weight(span.toFloat())) {
                            contentData.content()
                        }
                        columnIndex += span
                    }
                }
            }
        }
    }
}