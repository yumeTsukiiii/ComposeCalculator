package fan.yumetsuki.composecalculator.atri_script

import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.DefaultTokenScanner
import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.TokenScanner
import fan.yumetsuki.composecalculator.atri_script.semantic_analysis.ASTVisitor
import fan.yumetsuki.composecalculator.atri_script.semantic_analysis.DefaultASTVisitor
import fan.yumetsuki.composecalculator.atri_script.semantic_analysis.GlobalMemory
import fan.yumetsuki.composecalculator.atri_script.semantic_analysis.Memory
import fan.yumetsuki.composecalculator.atri_script.parser.*

/**
 * N 久以前写的，简单的脚本解析器
 * 支持算数运算和逻辑运算
 * 独立仓库见：https://github.com/yumeTsukiiii/atri_script
 * @author yumetsuki
 */
class AtriScriptInterpreter(
    memory: Memory = GlobalMemory()
) {

    private val tokenScanner: TokenScanner = DefaultTokenScanner()

    private val astParser: ASTParser = ASTParser()

    private val astVisitor: ASTVisitor = DefaultASTVisitor(memory)

    fun eval(input: String) : String {
        return try {
            val tokens = tokenScanner.scan(input)
            val nodes = astParser.parse(tokens)
            return nodes.map {
                when(it) {
                    is ExpressionNode<*> -> astVisitor.visitExpression(it).toString()
                    is VariableDefineNode -> astVisitor.visitVariableDefine(it)
                    is VariableDefineAndAssignmentNode -> astVisitor.visitVariableDefineAndAssignment(it).let {
                            pair -> "${pair.first} = ${pair.second}"
                    }
                    is VariableAssignmentNode -> astVisitor.visitVariableAssignment(it).let {
                            pair -> "${pair.first} = ${pair.second}"
                    }
                    is IdentifierNode -> astVisitor.visitIdentifier(it).toString()
                    is NumberNode -> astVisitor.visitNumber(it).toString()
                    else -> "Unsupported syntactic"
                }
            }.lastOrNull()?:"工口発生: Unknown error"
        } catch (e: Exception) {
            e.printStackTrace()
            "工口発生: ${e.message}"
        }
    }

}