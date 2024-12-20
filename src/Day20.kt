import kotlin.collections.Map

fun main() {

    fun findStart(map: List<String>): YX {
        return map.findCoordinate('S')
    }

    fun findEnd(map: List<String>): YX {
        return map.findCoordinate('E')
    }

    fun buildDistanceMap(input: List<String>, start: YX, end: YX): Map<YX, Int> {
        val result: MutableMap<YX, Int> = mutableMapOf()

        tailrec fun step(yx: YX, distance: Int) {
            result[yx] = distance

            if (yx == end) {
                return
            }

            val next = cardinals.map { yx + it }
                .filter { input.inBounds(it) }
                .filter { input[it] != '#' }
                .first { it !in result }

            step(next, distance + 1)
        }

        step(start, 0)

        return result
    }

    fun calculateSave(from: YX, to: YX, distances: Map<YX, Int>): Int? {
        val a = distances[from] ?: return null
        val b = distances[to] ?: return null

        val distance = from.distanceTo(to)

        val save = b - a - distance
        return save
    }

    fun isValidCheat(
        from: YX,
        to: YX,
        cheatDistance: Int,
        distances: Map<YX, Int>,
        minSave: Int
    ): Boolean {
        return if (from != to && from.distanceTo(to) <= cheatDistance) {
            val save = calculateSave(from, to, distances)

            save != null && save >= minSave
        } else {
            false
        }
    }

    fun countCheatsFrom(input: List<String>, from: YX, cheatDistance: Int, minSave: Int, distances: Map<YX, Int>): Int {
        if (input[from] == '#') {
            return 0
        }

        val minY = (from.y - cheatDistance).coerceAtLeast(0)
        val maxY = (from.y + cheatDistance).coerceAtMost(input.lastIndex)
        val minX = (from.x - cheatDistance).coerceAtLeast(0)
        val maxX = (from.x + cheatDistance).coerceAtMost(input.first().lastIndex)

        return (minY..maxY).sumOf { toY ->
            (minX..maxX).count { toX ->
                val to = YX(toY, toX)
                isValidCheat(from, to, cheatDistance, distances, minSave)
            }
        }
    }

    fun solve(input: List<String>, minSave: Int, cheatDistance: Int): Int {
        val start = findStart(input)
        val end = findEnd(input)
        val distances = buildDistanceMap(input, start, end)

        return input.indices.sumOf { y ->
            input.first().indices.sumOf { x ->
                countCheatsFrom(input, YX(y, x), cheatDistance, minSave, distances)
            }
        }
    }

    fun part1(input: List<String>, minSave: Int): Int {
        return solve(input, minSave, 2)
    }

    fun part2(input: List<String>, minSave: Int): Int {
        return solve(input, minSave, 20)
    }

    val testInput = readInput("Day20_test")
    check(part1(testInput, 40) == 2)

    val input = readInput("Day20")
    part1(input, 100).println()

    check(part2(testInput, 50) == 285)
    part2(input, 100).println()
}
