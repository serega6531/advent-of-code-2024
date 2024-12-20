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

    fun countCheatsThroughWall(wall: YX, distances: Map<YX, Int>, minSave: Int): Int {
        return cardinals.sumOf { firstDirection ->
            (cardinals - firstDirection).count { secondDirection ->
                val a = distances[wall + firstDirection]
                val b = distances[wall + secondDirection]

                if (a != null && b != null && a < b) {
                    val save = b - a - 2

                    save >= minSave
                } else {
                    false
                }
            }
        }
    }

    fun countCheats(input: List<String>, distances: Map<YX, Int>, minSave: Int): Int {
        return input.withIndex().sumOf { (y, line) ->
            line.withIndex().sumOf { (x, ch) ->
                if (input[y][x] == '#') {
                    countCheatsThroughWall(YX(y, x), distances, minSave)
                } else {
                    0
                }
            }
        }
    }

    fun part1(input: List<String>, minSave: Int): Int {
        val start = findStart(input)
        val end = findEnd(input)
        val distances = buildDistanceMap(input, start, end)

        return countCheats(input, distances, minSave)
    }

    fun part2(input: List<String>, minSave: Int): Int {
        TODO()
    }

    val testInput = readInput("Day20_test")
    check(part1(testInput, 40) == 2)

    val input = readInput("Day20")
    part1(input, 100).println()

    check(part2(testInput, 50) == 285)
    part2(input, 100).println()
}
