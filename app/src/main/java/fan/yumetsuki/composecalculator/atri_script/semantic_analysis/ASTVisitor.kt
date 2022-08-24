package fan.yumetsuki.composecalculator.atri_script.semantic_analysis

import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.*
import fan.yumetsuki.composecalculator.atri_script.parser.*

interface ASTVisitor {
    fun visitAssignment(node: AssignmentNode) : Any
    fun visitExpression(node: ExpressionNode<*>) : Any
    fun visitBinaryOperator(node: BinaryOperatorNode) : Any
    fun visitUnaryOperator(node: UnaryOperatorNode) : Any
    fun visitVariable(node: VariableNode) : Any
    fun visitIdentifier(node: IdentifierNode) : Any
    fun visitNumber(node: NumberNode) : Number
    fun visitVariableDefineAndAssignment(node: VariableDefineAndAssignmentNode) : Pair<String, Any>
    fun visitVariableAssignment(node: VariableAssignmentNode) : Pair<String, Any>
    fun visitVariableDefine(node: VariableDefineNode) : String
    fun visitBool(node: BoolNode) : Boolean
}

class DefaultASTVisitor(
        private val memory: Memory
): ASTVisitor {
    override fun visitAssignment(node: AssignmentNode): Any {
        return visitExpression(node.expressionChild)
    }

    override fun visitExpression(node: ExpressionNode<*>): Any {
        return when(node) {
            is BinaryOperatorNode -> visitBinaryOperator(node)
            is UnaryOperatorNode -> visitUnaryOperator(node)
            is VariableNode -> visitVariable(node)
            is NumberNode -> visitNumber(node)
            is BoolNode -> visitBool(node)
        }
    }

    override fun visitBinaryOperator(node: BinaryOperatorNode): Any {
        val leftValue = visitExpression(node.leftChild)
        val rightValue = visitExpression(node.rightChild)
        if (node.value == EqualsTag) return leftValue == rightValue
        if (leftValue is Number && rightValue is Number) {
            return when(node.value) {
                PlusTag.toString() -> leftValue.toDouble() + rightValue.toDouble()
                MinusTag.toString() -> leftValue.toDouble() - rightValue.toDouble()
                MultiplicationTag.toString() -> leftValue.toDouble() * rightValue.toDouble()
                DivisionTag.toString() -> leftValue.toDouble() / rightValue.toDouble()
                ModTag.toString() -> leftValue.toDouble() % rightValue.toDouble()
                GreatEqualsTag -> leftValue.toDouble() >= rightValue.toDouble()
                GreatThanTag.toString() -> leftValue.toDouble() > rightValue.toDouble()
                LessEqualsTag -> leftValue.toDouble() <= rightValue.toDouble()
                LessThanTag.toString() -> leftValue.toDouble() < rightValue.toDouble()
                else -> error("Unsupported operator: ${node.value} between Number($leftValue) and Number($rightValue)")
            }
        }
        if (leftValue is Boolean && rightValue is Boolean) {
            return when(node.value) {
                AndTag -> leftValue && rightValue
                OrTag -> leftValue && rightValue
                else -> error("Unsupported operator: ${node.value} between Boolean($leftValue) and Boolean($rightValue)")
            }
        }
        error("This operation \"${node.value}\" cannot be used for ${node.leftChild.value}($leftValue) and ${node.rightChild.value}($rightValue)")
    }

    override fun visitUnaryOperator(node: UnaryOperatorNode): Any {
        return when(node.value) {
            NotTag.toString() -> visitExpression(node.child).let {
                !when(it) {
                    is Boolean -> it
                    is Number -> it == 0
                    is String -> it.isNotEmpty()
                    is Char -> !it.isWhitespace()
                    else -> false
                }
            }
            else -> error("Unsupported operator: ${node.value}")
        }
    }

    override fun visitVariable(node: VariableNode): Any {
        return memory.getVariable(node.value)?: Undefined
    }

    override fun visitIdentifier(node: IdentifierNode): Any {
        return memory.getVariable(node.value)?:"undefined"
    }

    override fun visitNumber(node: NumberNode): Number {
        return node.value.toDouble()
    }

    override fun visitVariableDefineAndAssignment(node: VariableDefineAndAssignmentNode): Pair<String, Any> {
        val variableName = node.identifierChild.value
        if (memory.getVariable(variableName) != null) error("$variableName is defined")
        val value = visitAssignment(node.assignmentChild)
        memory.setVariable(variableName, value)
        return variableName to value
    }

    override fun visitVariableAssignment(node: VariableAssignmentNode): Pair<String, Any> {
        val variableName = node.value.value
        if (memory.getVariable(variableName) == null) error("$variableName is undefined")
        val value = visitAssignment(node.assignmentChild)
        memory.setVariable(variableName, value)
        return variableName to value
    }

    override fun visitVariableDefine(node: VariableDefineNode): String {
        val variableName = node.identifierChild.value
        if (memory.getVariable(variableName) != null) error("$variableName is defined")
        memory.setVariable(variableName, null)
        return variableName
    }

    override fun visitBool(node: BoolNode): Boolean = node.value
}

object Undefined {
    override fun toString(): String = "undefined"
}
