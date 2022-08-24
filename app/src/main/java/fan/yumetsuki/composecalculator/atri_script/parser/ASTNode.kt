package fan.yumetsuki.composecalculator.atri_script.parser

import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.AssignmentTag
import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.VariableDefineTag

sealed class ASTNode<T>(
    val value: T
) {
    abstract fun children(): List<ASTNode<*>>
}

class AssignmentNode(
    val expressionChild: ExpressionNode<*>
): ASTNode<Char>(AssignmentTag) {
    override fun children(): List<ASTNode<*>> = listOf(expressionChild)
}

sealed class ExpressionNode<T>(
    value: T
) : ASTNode<T>(value)

class BinaryOperatorNode(
    value: String,
    val leftChild: ExpressionNode<*>,
    val rightChild: ExpressionNode<*>
) : ExpressionNode<String>(value) {
    override fun children(): List<ASTNode<*>> = listOf(leftChild, rightChild)
}

class UnaryOperatorNode(
    value: String,
    val child: ExpressionNode<*>
) : ExpressionNode<String>(value) {
    override fun children(): List<ASTNode<*>> = listOf(child)
}

class VariableNode(
    value: String
) : ExpressionNode<String>(value) {
    override fun children(): List<ASTNode<*>> = listOf()
}

class IdentifierNode(
    value: String
) : ASTNode<String>(value) {
    override fun children(): List<ASTNode<*>> = listOf()
}

class NumberNode(
    value: Number
) : ExpressionNode<Number>(value) {
    override fun children(): List<ASTNode<*>> = listOf()
}

class BoolNode(
    value: Boolean
) : ExpressionNode<Boolean>(value) {
    override fun children(): List<ASTNode<*>> = listOf()
}

class VariableDefineAndAssignmentNode(
    val identifierChild: IdentifierNode,
    val assignmentChild: AssignmentNode
) : ASTNode<String>(VariableDefineTag) {
    override fun children(): List<ASTNode<*>> = listOf(identifierChild, assignmentChild)
}

class VariableAssignmentNode(
    identifierNode: IdentifierNode,
    val assignmentChild: AssignmentNode
) : ASTNode<IdentifierNode>(identifierNode) {
    override fun children(): List<ASTNode<*>> = listOf(assignmentChild)
}

class VariableDefineNode(
    val identifierChild: IdentifierNode
) : ASTNode<String>(VariableDefineTag) {
    override fun children(): List<ASTNode<*>> = listOf(identifierChild)
}