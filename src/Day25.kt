fun main() {

    val pinsCount = 5
    val maxPinHeight = 5

    fun List<String>.isKeySchematic(): Boolean {
        return this.first().first() != '#'
    }

    fun parseHeights(schematic: List<String>): List<Int> {
        return (0..<pinsCount).map { x ->
            (1..maxPinHeight).count { y -> schematic[y][x] == '#' }
        }
    }

    fun parseInput(input: String): Pair<List<List<Int>>, List<List<Int>>> {
        val( keySchematics, lockSchematics ) = input.split("\n\n")
            .map { it.lines() }
            .partition { it.isKeySchematic() }

        val keyHeights = keySchematics.map { parseHeights(it) }
        val lockHeights = lockSchematics.map { parseHeights(it) }
        return keyHeights to lockHeights
    }

    fun overlaps(key: List<Int>, lock: List<Int>): Boolean {
        return (0..<pinsCount).all { pin ->
            key[pin] + lock[pin] <= maxPinHeight
        }
    }

    fun part1(input: String): Int {
        val (keyHeights, lockHeights) = parseInput(input)

        return keyHeights.sumOf { key ->
            lockHeights.count { lock ->
                overlaps(key, lock)
            }
        }
    }

    val testInput = readEntireInput("Day25_test")
    check(part1(testInput) == 3)

    val input = readEntireInput("Day25")
    part1(input).println()
}
