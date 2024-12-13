fun main() {

    val directions = listOf<Pair<Int, Int>>(
        -1 to 0,
        1 to 0,
        0 to -1,
        0 to 1
    )

    fun calculateRegions(input: List<String>): List<Set<YX>> {
        val seen = mutableSetOf<YX>()

        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        fun expandRegion(y: Int, x: Int, plant: Char, plots: MutableSet<YX>) {
            seen.add(YX(y, x))
            plots.add(YX(y, x))

            directions.forEach { (dy, dx) ->
                val newY = y + dy
                val newX = x + dx

                if (newY in 0..maxY && newX in 0..maxX && YX(newY, newX) !in seen && input[newY][newX] == plant) {
                    expandRegion(newY, newX, plant, plots)
                }
            }
        }

        return buildList {
            (0..maxY).forEach { y ->
                (0..maxX).forEach { x ->
                    val plant = input[y][x]
                    if (YX(y, x) !in seen) {
                        val newRegion = mutableSetOf<YX>()
                        expandRegion(y, x, plant, newRegion)
                        add(newRegion)
                    }
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        val regions = calculateRegions(input)

        fun calculatePrice(region: Set<YX>): Int {
            fun calculateEdges(y: Int, x: Int): Int {
                return directions.count { (dy, dx) -> YX(y + dy, x + dx) !in region }
            }

            val area = region.size
            val perimeter = region.sumOf { (y, x) -> calculateEdges(y, x) }

            return area * perimeter
        }

        return regions.sumOf { calculatePrice(it) }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 1930)

    val input = readInput("Day12")
    part1(input).println()

    check(part2(testInput) == 1206)
    part2(input).println()
}