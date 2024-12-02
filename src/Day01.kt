import java.util.PriorityQueue
import kotlin.math.abs

fun main() {
    fun part1(input: List<String>): Int {
        val queueLeft = PriorityQueue<Int>()
        val queueRight = PriorityQueue<Int>()

        input.map { line -> line.split("   ") }
            .map { (a, b) -> a.toInt() to b.toInt() }
            .map { (a, b) ->
                queueLeft.add(a)
                queueRight.add(b)
            }

        return queueLeft.indices.sumOf { _ ->
            val left = queueLeft.poll()
            val right = queueRight.poll()
            abs(left - right)
        }
    }

    fun part2(input: List<String>): Int {
        val rightCounts = input.map { line -> line.split("   ") }
            .map { (_, b) -> b.toInt() }
            .groupingBy { it }
            .eachCount()

        return input.map { line -> line.split("   ") }
            .map { (a, _) -> a.toInt() }
            .sumOf { a -> a * rightCounts.getOrDefault(a, 0) }
    }

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 11)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
