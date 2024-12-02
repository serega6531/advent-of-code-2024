import kotlin.math.abs

fun main() {
    fun part1(input: List<String>): Int {
        fun isSafe(report: List<Int>): Boolean {
            var lastIncreases: Boolean? = null

            report.zipWithNext { a, b -> b - a }
                .forEach { diff ->
                    if (abs(diff) !in 1..3) {
                        return false
                    }

                    val increases = diff > 0
                    if (lastIncreases != null && lastIncreases != increases) {
                        return false
                    }

                    lastIncreases = increases
                }

            return true
        }

        return input.map { report -> report.trim().split(" ") }
            .map { report -> report.map { it.toInt() } }
            .count { report -> isSafe(report) }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 2)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
