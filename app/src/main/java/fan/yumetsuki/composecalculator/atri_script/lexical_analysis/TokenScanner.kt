package fan.yumetsuki.composecalculator.atri_script.lexical_analysis

interface TokenScanner {
    fun scan(input: String): List<Token>
}

class DefaultTokenScanner : TokenScanner {
    override fun scan(input: String): List<Token> {
        val result = arrayListOf<Token>()
        var currentState: ScanState = InitState()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            var nextState = currentState.nextState(c)
            if (nextState == null) {
                result.add(currentState.token)
                while (c == ' ' && i < input.length && input[i + 1] == ' ') i++
                nextState = if (c == ' ') {
                    InitState()
                } else {
                    InitState().nextState(c)?: error("Unsupported token")
                }
            }
            currentState = nextState
            i++
        }
        if (currentState !is InitState) {
            result.add(currentState.token)
        }
        return result
    }
}

private interface ScanState {
    val text: String
    val token: Token
    fun nextState(char: Char): ScanState?
}

private class InitState : ScanState {

    override val text: String
        get() = ""

    override val token: Token
        get() { error("InitState can not generate token") }

    override fun nextState(char: Char): ScanState? = when {
        char.isJavaIdentifierStart() -> IdentifierState(char.toString())
        char.isDigit() -> NumberState(char.toString())
        char == AssignmentTag -> AssignmentState(char.toString())
        char == PlusTag -> PlusState(char.toString())
        char == MinusTag -> MinusState(char.toString())
        char == MultiplicationTag -> MultiplicationState(char.toString())
        char == DivisionTag -> DivisionState(char.toString())
        char == ModTag -> ModState(char.toString())
        char == LeftParenthesesTag -> LeftParenthesesState(char.toString())
        char == RightParenthesesTag -> RightParenthesesState(char.toString())
        char == NewLineTag -> NewLineState(char.toString())
        char == SemicolonTag -> SemicolonState(char.toString())
        char == LogicOrTag -> LogicOrState(char.toString())
        char == LogicAndTag -> LogicAndState(char.toString())
        char == NotTag -> NotState(char.toString())
        char == GreatThanTag -> GreatThanState(char.toString())
        char == LessThanTag -> LessThanState(char.toString())
        else -> null
    }
}

private class VariableDefineState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = VariableDefine(text)

    override fun nextState(char: Char): ScanState? {
        val nextText = "$text$char"
        return when {
            char.isJavaIdentifierPart() -> IdentifierState(nextText)
            else -> null
        }
    }
}

private class IdentifierState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Identifier(text)

    override fun nextState(char: Char): ScanState? {
        val nextText = "$text$char"
        return when {
            nextText == TrueTag -> TrueState(nextText)
            nextText == FalseTag -> FalseState(nextText)
            nextText == VariableDefineTag -> VariableDefineState(nextText)
            char.isDigit() || char.isJavaIdentifierPart() -> IdentifierState(nextText)
            else -> null
        }
    }
}

private class AssignmentState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Assignment(text)

    override fun nextState(char: Char): ScanState? = when(val nextText = "${text}$char") {
        EqualsTag -> EqualsState(nextText)
        else -> null
    }

}

private class NumberState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Number(text)

    override fun nextState(char: Char): ScanState? {
        val nextText = "$text$char"
        return when {
            char.isWhitespace() -> null
            (nextText.toIntOrNull() != null || nextText.toDoubleOrNull() != null) -> NumberState(nextText)
            else -> null
        }
    }

}

private class PlusState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Plus(text)

    override fun nextState(char: Char): ScanState? = null

}

private class MinusState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Minus(text)

    override fun nextState(char: Char): ScanState? = null

}

private class MultiplicationState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Multiplication(text)

    override fun nextState(char: Char): ScanState? = null

}

private class DivisionState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Division(text)

    override fun nextState(char: Char): ScanState? = null

}

private class ModState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Mod(text)

    override fun nextState(char: Char): ScanState? = null

}

private class LeftParenthesesState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = LeftParentheses(text)

    override fun nextState(char: Char): ScanState? = null

}

private class RightParenthesesState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = RightParentheses(text)

    override fun nextState(char: Char): ScanState? = null

}

private class NewLineState(
    override val text: String
) : ScanState {

    override val token: Token
        get() = NewLine(text)

    override fun nextState(char: Char): ScanState? = null

}

private class SemicolonState(
    override val text: String
) : ScanState {

    override val token: Token
        get() = Semicolon(text)

    override fun nextState(char: Char): ScanState? = null

}

private class TrueState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = True(text)

    override fun nextState(char: Char): ScanState? = null

}

private class FalseState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = False(text)

    override fun nextState(char: Char): ScanState? = null

}

private class LogicOrState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = LogicOr(text)

    override fun nextState(char: Char): ScanState? {
        return when(val nextText = "${text}$char") {
            OrTag -> OrState(nextText)
            else -> null
        }
    }

}

private class LogicAndState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = LogicAnd(text)

    override fun nextState(char: Char): ScanState? {
        return when(val nextText = "${text}$char") {
            AndTag -> AndState(nextText)
            else -> null
        }
    }

}

private class OrState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Or(text)

    override fun nextState(char: Char): ScanState? = null

}

private class AndState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = And(text)

    override fun nextState(char: Char): ScanState? = null

}

private class NotState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Not(text)

    override fun nextState(char: Char): ScanState? {
        val nextText = "${text}$char"
        return when {
            nextText == NotEqualsTag -> NotEqualsState(nextText)
            char.isJavaIdentifierPart() -> NotExprState(char.toString())
            else -> null
        }
    }

}

private class NotExprState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Not(text)

    override fun nextState(char: Char): ScanState? {
        val nextText = "${text}$char"
        return when {
            nextText == TrueTag -> FalseState(nextText)
            nextText == FalseTag -> TrueState(nextText)
            char.isJavaIdentifierPart() -> NotExprState(nextText)
            else -> null
        }
    }

}

private class EqualsState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = Equals(text)

    override fun nextState(char: Char): ScanState? = null

}

private class NotEqualsState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = NotEquals(text)

    override fun nextState(char: Char): ScanState? = null

}

private class GreatThanState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = GreatThan(text)

    override fun nextState(char: Char): ScanState? = when(val nextText = "${text}$char") {
        GreatEqualsTag -> GreatEqualsState(nextText)
        else -> null
    }
}

private class GreatEqualsState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = GreatEquals(text)

    override fun nextState(char: Char): ScanState? = null

}

private class LessThanState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = LessThan(text)

    override fun nextState(char: Char): ScanState? = when(val nextText = "${text}$char") {
        LessEqualsTag -> LessEqualsState(nextText)
        else -> null
    }

}

private class LessEqualsState(
        override val text: String
) : ScanState {

    override val token: Token
        get() = LessEquals(text)

    override fun nextState(char: Char): ScanState? = null

}