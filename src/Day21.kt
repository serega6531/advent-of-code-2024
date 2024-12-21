import java.util.*
import kotlin.collections.Map
import kotlin.math.abs

fun main() {

    tailrec fun getParentCode(code: List<Char>, keypads: List<Keypad>): List<Char> {
        val first = keypads.first()
        val parents = keypads.drop(1)
        val parentCode = code.flatMap { first.generateCodeToMoveTo(it) }

        return if (parents.isNotEmpty()) {
            getParentCode(parentCode, parents)
        } else {
            parentCode
        }
    }

    fun getFullCode(code: String, generateKeypads: () -> List<Keypad>): List<Char> {
        val keypads = generateKeypads()

        return getParentCode(code.toList(), keypads)
    }

    fun getComplexity(code: String, generateKeypads: () -> List<Keypad>): Int {
        val fullCode = getFullCode(code, generateKeypads)
        val numericPart = code.dropLast(1).toInt()

        return fullCode.size * numericPart
    }

    fun part1(input: List<String>): Int {
        fun generateKeypads(): List<Keypad> {
            return listOf(NumericKeypad(), DirectionalKeypad(), DirectionalKeypad())
        }

        return input.sumOf { getComplexity(it, ::generateKeypads) }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput) == 126384)

    val input = readInput("Day21")
    part1(input).println()

    part2(input).println()
}

private abstract class Keypad(val layout: List<List<Char?>>) {

    val coordinates: Map<Char, YX> = layout.flatten()
        .filterNotNull()
        .associateWith { layout.findCoordinate(it) }

    var position = layout.findCoordinate('A')
    private val empty = layout.findCoordinate(null)

    abstract fun getHorizontalPriority(goal: YX): Boolean

    fun generateCodeToMoveTo(button: Char): List<Char> {
        val goal = coordinates.getValue(button)
        val diffY = goal.y - position.y
        val diffX = goal.x - position.x

        val movementX = if (diffX < 0) '<' else '>'
        val movementY = if (diffY < 0) '^' else 'v'

        val horizontalPriority = getHorizontalPriority(goal)

        val actions = buildList {
            if (horizontalPriority) {
                addAll(Collections.nCopies(abs(diffX), movementX))
                addAll(Collections.nCopies(abs(diffY), movementY))
            } else {
                addAll(Collections.nCopies(abs(diffY), movementY))
                addAll(Collections.nCopies(abs(diffX), movementX))
            }

            add('A')
        }

        position = goal
        return actions
    }

    fun crossesEmptySpace(goal: YX): Boolean {
        val minX = minOf(position.x, goal.x)
        val maxX = maxOf(position.x, goal.x)
        val minY = minOf(position.y, goal.y)
        val maxY = maxOf(position.y, goal.y)

        val rangeX = minX..maxX
        val rangeY = minY..maxY

        return empty.x in rangeX && empty.y in rangeY
    }

}

private class NumericKeypad : Keypad(layout) {

    override fun getHorizontalPriority(goal: YX): Boolean {
        val diffY = goal.y - position.y
        val diffX = goal.x - position.x

        return if (crossesEmptySpace(goal)) {
            diffY > 0
        } else {
            diffX < 0
        }
    }

    companion object {
        private val layout = listOf(
            listOf('7', '8', '9'),
            listOf('4', '5', '6'),
            listOf('1', '2', '3'),
            listOf(null, '0', 'A')
        )
    }

}

private class DirectionalKeypad : Keypad(layout) {

    override fun getHorizontalPriority(goal: YX): Boolean {
        val diffY = goal.y - position.y
        val diffX = goal.x - position.x

        return if (crossesEmptySpace(goal)) {
            diffY < 0
        } else {
            diffX < 0
        }
    }

    companion object {
        private val layout = listOf(
            listOf(null, '^', 'A'),
            listOf('<', 'v', '>')
        )
    }

}