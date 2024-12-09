fun main() {

    fun parseInput(input: List<String>): List<Pair<Long, List<Long>>> {
        return input.map { it.split(": ") }
            .map { (result, operands) -> result.toLong() to operands.split(" ").map { it.toLong() } }
    }

    fun resultsInGoal(goal: Long, operands: List<Long>, operators: List<(Long, Long) -> Long>): Boolean {
        if (operands.size == 1) {
            return operands.single() == goal
        }

        if (operands.first() > goal) {
            return false
        }

        return operators.any { op ->
            val newOperand = op.invoke(operands[0], operands[1])
            val updatedOperands = listOf(newOperand, *operands.subList(2, operands.size).toTypedArray())
            resultsInGoal(goal, updatedOperands, operators)
        }
    }

    fun part1(input: List<String>): Long {
        val operators: List<(Long, Long) -> Long> = listOf(
            { a: Long, b: Long -> a + b },
            { a: Long, b: Long -> a * b }
        )

        return parseInput(input)
            .filter { (goal, operands) -> resultsInGoal(goal, operands, operators) }
            .sumOf { (goal, _) -> goal }
    }

    fun part2(input: List<String>): Long {
        val operators: List<(Long, Long) -> Long> = listOf(
            { a: Long, b: Long -> a + b },
            { a: Long, b: Long -> a * b },
            { a: Long, b: Long -> (a.toString() + b.toString()).toLong() }
        )

        return parseInput(input)
            .filter { (goal, operands) -> resultsInGoal(goal, operands, operators) }
            .sumOf { (goal, _) -> goal }
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 3749L)

    val input = readInput("Day07")
    part1(input).println()

    check(part2(testInput) == 11387L)
    part2(input).println()
}