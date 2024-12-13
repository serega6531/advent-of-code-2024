fun main() {

    val directions = listOf<Pair<Int, Int>>(
        -1 to 0,
        1 to 0,
        0 to -1,
        0 to 1
    )

    fun parseMap(input: List<String>): List<List<Int>> {
        return input.map { line ->
            line.map { it.digitToInt() }
        }
    }

    fun findStarts(map: List<List<Int>>): List<YX> {
        return buildList {
            val maxY = map.lastIndex
            val maxX = map.first().lastIndex

            (0..maxY).forEach { y ->
                (0..maxX).forEach { x ->
                    if (map[y][x] == 0) {
                        add(YX(y, x))
                    }
                }
            }
        }
    }

    fun processPaths(map: List<List<Int>>, start: YX, onEnd: (YX) -> Unit) {
        val maxY = map.lastIndex
        val maxX = map.first().lastIndex

        fun step(y: Int, x: Int) {
            val value = map[y][x]

            if (value == 9) {
                onEnd(YX(y, x))
                return
            }

            directions.forEach { (dy, dx) ->
                val newY = y + dy
                val newX = x + dx

                if (newY in 0..maxY && newX in 0..maxX && map[newY][newX] == value + 1) {
                    step(newY, newX)
                }
            }
        }

        step(start.first, start.second)
    }

    fun part1(input: List<String>): Int {
        fun countEnds(map: List<List<Int>>, start: YX): Int {
            val foundEnds = mutableSetOf<YX>()

            processPaths(map, start) { foundEnds.add(it) }

            return foundEnds.size
        }

        val map = parseMap(input)
        val starts = findStarts(map)

        return starts.sumOf { countEnds(map, it) }
    }

    fun part2(input: List<String>): Int {
        fun countPaths(map: List<List<Int>>, start: YX): Int {
            var result = 0

            processPaths(map, start) { result++ }

            return result
        }

        val map = parseMap(input)
        val starts = findStarts(map)

        return starts.sumOf { countPaths(map, it) }
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 36)

    val input = readInput("Day10")
    part1(input).println()

    check(part2(testInput) == 81)
    part2(input).println()
}