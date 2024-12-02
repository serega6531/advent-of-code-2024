import kotlin.math.abs

private fun findErrorIndex(report: List<Int>): Int? {
    var lastIncreases: Boolean? = null

    report.withIndex()
        .zipWithNext { a, b -> b.index to (b.value - a.value) }
        .forEach { (index, diff) ->
            if (abs(diff) !in 1..3) {
                return index
            }

            val increases = diff > 0
            if (lastIncreases != null && lastIncreases != increases) {
                return index
            }

            lastIncreases = increases
        }

    return null
}

private fun tryRecover(report: List<Int>, errorIndex: Int): Boolean {
    return (-2..0).any { offset ->
        val cut = errorIndex + offset
        if (cut !in 0..report.lastIndex) {
            false
        } else {
            val updated = report.filterIndexed { index, _ -> index != cut }
            findErrorIndex(updated) == null
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return input.map { report -> report.trim().split(" ") }
            .map { report -> report.map { it.toInt() } }
            .count { report -> findErrorIndex(report) == null }
    }

    fun part2(input: List<String>): Int {
        return input.map { report -> report.trim().split(" ") }
            .map { report -> report.map { it.toInt() } }
            .count { report ->
                val error = findErrorIndex(report)
                error == null || tryRecover(report, error)
            }
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
