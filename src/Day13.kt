import org.jetbrains.kotlinx.multik.api.linalg.solve
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import kotlin.math.roundToLong

fun main() {

    fun parseInput(input: String): List<Configuration> {
        val configurationRegex = Regex(
            """
                Button A: X\+(\d+), Y\+(\d+)
                Button B: X\+(\d+), Y\+(\d+)
                Prize: X=(\d+), Y=(\d+)""".trimIndent()
        )

        return input.split("\n\n")
            .map { configurationRegex.matchEntire(it)!!.destructured }
            .map { (ax, ay, bx, by, prizeX, prizeY) ->
                Configuration(
                    ax = ax.toLong(),
                    ay = ay.toLong(),
                    bx = bx.toLong(),
                    by = by.toLong(),
                    prizeX = prizeX.toLong(),
                    prizeY = prizeY.toLong()
                )
            }
    }

    fun verify(configuration: Configuration, a: Long, b: Long): Boolean {
        val totalX = a * configuration.ax + b * configuration.bx
        val totalY = a * configuration.ay + b * configuration.by
        return totalX == configuration.prizeX && totalY == configuration.prizeY
    }

    fun solve(configuration: Configuration): Pair<Long, Long>? {
        val buttons = mk.ndarray(mk[mk[configuration.ax, configuration.bx], mk[configuration.ay, configuration.by]])
        val prize = mk.ndarray(mk[mk[configuration.prizeX], mk[configuration.prizeY]])
        val solution = mk.linalg.solve(buttons, prize)

        val (a, b) = solution.data.map { it.roundToLong() }

        if (a < 0 || b < 0 || !verify(configuration, a, b)) {
            return null
        }

        return a to b
    }

    fun countPoints(a: Long, b: Long): Long {
        return a * 3 + b
    }

    fun part1(input: String): Long {
        return parseInput(input)
            .mapNotNull { solve(it) }
            .sumOf { (a, b) -> countPoints(a, b) }
    }

    fun part2(input: String): Long {
        return parseInput(input)
            .map { Configuration(it.ax, it.ay, it.bx, it.by, it.prizeX + 10000000000000, it.prizeY + 10000000000000) }
            .mapNotNull { solve(it) }
            .sumOf { (a, b) -> countPoints(a, b) }
    }

    val testInput = readEntireInput("Day13_test")
    check(part1(testInput) == 480L)

    val input = readEntireInput("Day13")
    part1(input).println()

    part2(input).println()
}

private data class Configuration(
    val ax: Long,
    val ay: Long,
    val bx: Long,
    val by: Long,
    val prizeX: Long,
    val prizeY: Long
)