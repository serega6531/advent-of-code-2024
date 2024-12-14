fun main() {

    val cardinals = listOf<DirectionOffset>(
        DirectionOffset(-1, 0),
        DirectionOffset(0, 1),
        DirectionOffset(1, 0),
        DirectionOffset(0, -1)
    )

    val diagonals = listOf<DirectionOffset>(
        DirectionOffset(-1, -1),
        DirectionOffset(-1, 1),
        DirectionOffset(1, -1),
        DirectionOffset(1, 1)
    )

    val adjacentDirections = listOf<Pair<DirectionOffset, DirectionOffset>>(
        cardinals[0] to cardinals[1],
        cardinals[1] to cardinals[2],
        cardinals[2] to cardinals[3],
        cardinals[3] to cardinals[0]
    )

    fun calculateRegions(input: List<String>): List<Set<YX>> {
        val seen = mutableSetOf<YX>()

        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        fun expandRegion(y: Int, x: Int, plant: Char, plots: MutableSet<YX>) {
            seen.add(YX(y, x))
            plots.add(YX(y, x))

            cardinals.forEach { (dy, dx) ->
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
            fun countEdges(y: Int, x: Int): Int {
                return cardinals.count { (dy, dx) -> YX(y + dy, x + dx) !in region }
            }

            val area = region.size
            val perimeter = region.sumOf { (y, x) -> countEdges(y, x) }

            return area * perimeter
        }

        return regions.sumOf { calculatePrice(it) }
    }

    fun part2(input: List<String>): Int {
        val regions = calculateRegions(input)

        fun calculatePrice(region: Set<YX>): Int {
            fun countCornersForPlot(y: Int, x: Int): Int {
                val sameRegionDirections: Set<DirectionOffset> =
                    (cardinals + diagonals).filterTo(mutableSetOf()) { (dy, dx) ->
                        region.contains(YX(y + dy, x + dx))
                    }

                val inAngles = adjacentDirections.count { (first, second) ->
                    first !in sameRegionDirections && second !in sameRegionDirections
                }

                val outAngles = adjacentDirections.count { (first, second) ->
                    val diagonal = DirectionOffset(first.dy + second.dy, first.dx + second.dx)
                    first in sameRegionDirections && second in sameRegionDirections && diagonal !in sameRegionDirections
                }

                return inAngles + outAngles
            }

            fun countCorners(): Int {
                return region.sumOf { (y, x) -> countCornersForPlot(y, x) }
            }

            val area = region.size
            val sides = countCorners() // there are the same number of corners and sides

            return area * sides
        }

        return regions.sumOf { calculatePrice(it) }
    }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 1930)

    val input = readInput("Day12")
    part1(input).println()

    check(part2(testInput) == 1206)
    part2(input).println()
}

private data class DirectionOffset(val dy: Int, val dx: Int)