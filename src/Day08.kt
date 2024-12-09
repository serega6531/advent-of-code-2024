fun main() {

    fun findAntennas(input: List<String>): Map<Char, List<YX>> {
        val allAntennas = input.flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, ch ->
                if (ch != '.') Pair(ch, YX(y, x)) else null
            }
        }

        return allAntennas.groupBy(keySelector = { it.first }, valueTransform = { it.second })
    }

    fun calculateAntinodes(antennas: List<YX>, getAntinodes: (YX, YX) -> List<YX>): List<YX> {
        return antennas.flatMapIndexed { index, first ->
            antennas.subList(index + 1, antennas.size)
                .flatMap { second -> getAntinodes(first, second) }
        }
    }

    fun part1(input: List<String>): Int {
        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        fun getAntinodesInLine(first: YX, second: YX): List<YX> {
            val (y1, x1) = first
            val (y2, x2) = second

            val dy = y2 - y1
            val dx = x2 - x1

            val y3 = y2 + dy
            val y4 = y1 - dy

            val x3 = x2 + dx
            val x4 = x1 - dx

            return listOf(YX(y3, x3), YX(y4, x4)).filter { (y, x) -> y in 0..maxY && x in 0..maxX }
        }

        val antennasByFreq = findAntennas(input)
        val antinodes = antennasByFreq.values.flatMapTo(mutableSetOf()) { antennas ->
            calculateAntinodes(antennas, ::getAntinodesInLine)
        }

        return antinodes.size
    }


    fun part2(input: List<String>): Int {
        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        fun getFollowingAntinodes(startY: Int, startX: Int, dy: Int, dx: Int): List<YX> {
            var currentY = startY
            var currentX = startX

            return buildList {
                while (currentY in 0..maxY && currentX in 0..maxX) {
                    add(YX(currentY, currentX))

                    currentY += dy
                    currentX += dx
                }
            }
        }

        fun getAntinodesOnLine(first: YX, second: YX): List<YX> {
            val (y1, x1) = first
            val (y2, x2) = second

            val dy = y2 - y1
            val dx = x2 - x1

            return getFollowingAntinodes(y2, x2, dy, dx) + getFollowingAntinodes(y1, x1, -dy, -dx)
        }

        val antennasByFreq = findAntennas(input)
        val antinodes = antennasByFreq.values.flatMapTo(mutableSetOf()) { antennas ->
            calculateAntinodes(antennas, ::getAntinodesOnLine)
        }

        return antinodes.size
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 14)

    val input = readInput("Day08")
    part1(input).println()

    check(part2(testInput) == 34)
    part2(input).println()
}