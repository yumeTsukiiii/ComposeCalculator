package fan.yumetsuki.composecalculator.atri_script.parser

import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.*
import fan.yumetsuki.composecalculator.atri_script.lexical_analysis.Number as NumberToken

class TokenIterator(
    private val tokens: List<Token>
) : Iterator<Token> {

    var currentPos: Int = 0

    override fun hasNext(): Boolean {
        return currentPos < tokens.size && currentPos > -1
    }

    override fun next(): Token = tokens[currentPos++]
}

class ASTParser {

    fun parse(tokens: List<Token>) : List<ASTNode<*>> {
        val result = arrayListOf<ASTNode<*>>()
        val tokenIterator = TokenIterator(tokens)
        while (tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken is Semicolon || nextToken is NewLine) {
                continue
            } else {
                tokenIterator.currentPos--
            }
            var hasAST = false
            hasAST = hasAST || parseVariableAssignment(tokenIterator)?.also { result.add(it) } != null
            hasAST = hasAST || parseExpression(tokenIterator)?.also { result.add(it) } != null
            hasAST = hasAST || parseVariableDefineOrAssignment(tokenIterator)?.also { result.add(it) } != null
            if (!hasAST) {
                error("syntactic is wrong...")
            }
        }
        return result
    }

    private fun parseVariableDefineOrAssignment(tokenIterator: TokenIterator): ASTNode<*>? {
        if (!tokenIterator.hasNext()) return null
        val startPos = tokenIterator.currentPos
        return tokenIterator.next().takeIf {
            it is VariableDefine && tokenIterator.hasNext()
        }?.let {
            tokenIterator.next()
        }?.takeIf {
            it is Identifier && tokenIterator.hasNext()
        }?.let {
            it to tokenIterator.next()
        }?.let { (identifierToken, currentToken) ->
            if (currentToken is NewLine || currentToken is Semicolon) {
                VariableDefineNode(IdentifierNode(identifierToken.value))
            } else if (currentToken is Assignment) {
                VariableDefineAndAssignmentNode(
                        IdentifierNode(identifierToken.value),
                        AssignmentNode(parseExpression(tokenIterator)?:error("syntactic is wrong..."))
                )
            } else {
                null
            }
        }?:run {
            tokenIterator.currentPos = startPos
            null
        }
    }

    private fun parseVariableAssignment(tokenIterator: TokenIterator): ASTNode<*>? {
        if (!tokenIterator.hasNext()) return null
        val startPos = tokenIterator.currentPos
        return tokenIterator.next().takeIf {
            it is Identifier && tokenIterator.hasNext()
        }?.let {
            it to tokenIterator.next()
        }?.let { (identifierToken, currentToken) ->
            if (currentToken is Assignment) {
                VariableAssignmentNode(
                        IdentifierNode(identifierToken.value),
                        AssignmentNode(parseExpression(tokenIterator)?:error("syntactic is wrong..."))
                )
            } else {
                null
            }
        }?:run {
            tokenIterator.currentPos = startPos
            null
        }
    }

    private fun parseExpression(tokenIterator: TokenIterator): ExpressionNode<*>? {
        return parseOperatorExpression(tokenIterator)
    }

    private fun parseOperatorExpression(tokenIterator: TokenIterator): ExpressionNode<*>? {
        var result: ExpressionNode<*>?
        val (arithmeticExpStartPos, arithmeticExp) = parseArithmeticExpression(tokenIterator)
        result = arithmeticExp
        if (result != null && tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken !is NewLine && nextToken !is Semicolon) {
                result = null
                tokenIterator.currentPos = arithmeticExpStartPos
            }
        }
        if (result != null) return result
        val (logicStartPos, logicExp) = parseLogicExpression(tokenIterator)
        result = logicExp
        if (result != null && tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken !is NewLine && nextToken !is Semicolon) {
                result = null
                tokenIterator.currentPos = logicStartPos
            }
        }
        return result
    }

    private fun parseLogicExpression(tokenIterator: TokenIterator): Pair<Int, ExpressionNode<*>?> {
        return parseAndOrExpression(tokenIterator)
    }

    private fun parseAndOrExpression(tokenIterator: TokenIterator) : Pair<Int, ExpressionNode<*>?> {
        if (!tokenIterator.hasNext()) return tokenIterator.currentPos to null
        val startPos = tokenIterator.currentPos
        var result: ExpressionNode<*>? = null
        var leftExpression = parseCompareExpression(tokenIterator)?:run {
            tokenIterator.currentPos = startPos
            return tokenIterator.currentPos to null
        }
        result = leftExpression
        while (tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken is And) {
                val rightExpression = parseCompareExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(AndTag, leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is Or) {
                val rightExpression = parseCompareExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(OrTag, leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is RightParentheses) {
                tokenIterator.currentPos++
            } else {
                tokenIterator.currentPos--
                break
            }
        }
        return startPos to result
    }

    private fun parseCompareExpression(tokenIterator: TokenIterator) : ExpressionNode<*>? {
        if (!tokenIterator.hasNext()) return null
        var result: ExpressionNode<*>? = null
        val startPos = tokenIterator.currentPos
        val (_, ariResult) = parseArithmeticExpression(tokenIterator)
        var leftExpression = ariResult?:parseVariableOrBoolOrNotExpression(tokenIterator)?: run {
            tokenIterator.currentPos = startPos
            return null
        }
        result = leftExpression
        while (tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken is GreatThan) {
                val rightExpression = parseVariableOrBoolOrNotExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(GreatThanTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is GreatEquals) {
                val rightExpression = parseVariableOrBoolOrNotExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(GreatEqualsTag, leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is LessThan) {
                val rightExpression = parseVariableOrBoolOrNotExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(LessThanTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is LessEquals) {
                val rightExpression = parseVariableOrBoolOrNotExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(LessEqualsTag, leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is Equals) {
                val rightExpression = parseVariableOrBoolOrNotExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(EqualsTag, leftExpression, rightExpression)
                leftExpression = result
            } else {
                tokenIterator.currentPos--
                break
            }
        }
        return result
    }

    private fun parseArithmeticExpression(tokenIterator: TokenIterator): Pair<Int, ExpressionNode<*>?> {
        return parsePlusOrMinusExpression(tokenIterator)
    }

    private fun parsePlusOrMinusExpression(tokenIterator: TokenIterator): Pair<Int, ExpressionNode<*>?> {
        if (!tokenIterator.hasNext()) return tokenIterator.currentPos to null
        val startPos = tokenIterator.currentPos
        var result: ExpressionNode<*>? = null
        var leftExpression = parseMultiOrDivOrModExpression(tokenIterator)?:run {
            tokenIterator.currentPos = startPos
            return tokenIterator.currentPos to null
        }
        result = leftExpression
        while (tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken is Plus) {
                val rightExpression = parseMultiOrDivOrModExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(PlusTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is Minus) {
                val rightExpression = parseMultiOrDivOrModExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(MinusTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is RightParentheses) {
                tokenIterator.currentPos++
            } else {
                tokenIterator.currentPos--
                break
            }
        }
        return startPos to result
    }

    private fun parseMultiOrDivOrModExpression(tokenIterator: TokenIterator): ExpressionNode<*>? {
        if (!tokenIterator.hasNext()) return null
        var result: ExpressionNode<*>? = null
        val startPos = tokenIterator.currentPos
        var leftExpression = parseVariableOrNumberExpression(tokenIterator)?: run {
            tokenIterator.currentPos = startPos
            return null
        }
        result = leftExpression
        while (tokenIterator.hasNext()) {
            val nextToken = tokenIterator.next()
            if (nextToken is Multiplication) {
                val rightExpression = parseVariableOrNumberExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(MultiplicationTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is Division) {
                val rightExpression = parseVariableOrNumberExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(DivisionTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else if (nextToken is Mod) {
                val rightExpression = parseVariableOrNumberExpression(tokenIterator)?: error("syntactic is wrong...")
                result = BinaryOperatorNode(ModTag.toString(), leftExpression, rightExpression)
                leftExpression = result
            } else {
                tokenIterator.currentPos--
                break
            }
        }
        return result
    }

    private fun parseVariableOrNumberExpression(tokenIterator: TokenIterator): ExpressionNode<*>? {
        if (!tokenIterator.hasNext()) return null
        return when (val nextToken = tokenIterator.next()) {
            is Identifier -> {
                VariableNode(nextToken.value)
            }
            is NumberToken -> {
                NumberNode(nextToken.value.let {
                    if (it.matches(Regex("^\\d+\\.\\d*"))) {
                        it.toDouble()
                    } else {
                        it.toInt()
                    }
                })
            }
            is LeftParentheses -> {
                val (startPos, result) = parsePlusOrMinusExpression(tokenIterator)
                result?.let {
                    if (!tokenIterator.hasNext()) return@let it
                    val nNextToken = tokenIterator.next()
                    if (nNextToken !is NewLine && nNextToken !is Semicolon) {
                        tokenIterator.currentPos = startPos
                        null
                    } else it
                }
            }
            else -> null
        }
    }

    private fun parseVariableOrBoolOrNotExpression(tokenIterator: TokenIterator): ExpressionNode<*>? {
        if (!tokenIterator.hasNext()) return null
        return when (val nextToken = tokenIterator.next()) {
            is Identifier -> {
                VariableNode(nextToken.value)
            }
            is NumberToken -> {
                NumberNode(nextToken.value.let {
                    if (it.matches(Regex("^\\d+\\.\\d*"))) {
                        it.toDouble()
                    } else {
                        it.toInt()
                    }
                })
            }
            is True -> {
                BoolNode(true)
            }
            is False -> {
                BoolNode(false)
            }
            is Not -> {
                UnaryOperatorNode(NotTag.toString(), VariableNode(nextToken.value))
            }
            is LeftParentheses -> {
                val (startPos, result) = parsePlusOrMinusExpression(tokenIterator)
                result?.let {
                    if (!tokenIterator.hasNext()) return@let it
                    val nNextToken = tokenIterator.next()
                    if (nNextToken !is NewLine && nNextToken !is Semicolon) {
                        tokenIterator.currentPos = startPos
                        null
                    } else it
                }
            }
            else -> null
        }
    }

}