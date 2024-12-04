fun main() {
    fun part1(input: List<String>): Int {
        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        val offsets: List<Pair<Int, Int>> = listOf(
            1 to 0,  // right
            0 to 1,  // down
            -1 to 0, // left
            0 to -1, // up
            1 to 1,  // right down
            1 to -1, // right up
            -1 to 1, // left down
            -1 to -1 // left up
        )

        fun hasWordAt(x: Int, y: Int, dx: Int, dy: Int): Boolean {
            val needle = "XMAS"

            repeat(needle.length) { step ->
                val stepY = y + dy * step
                val stepX = x + dx * step

                if (stepX !in 0..maxX || stepY !in 0..maxY || input[stepY][stepX] != needle[step]) {
                    return false
                }
            }

            return true
        }

        fun countWordsAt(x: Int, y: Int): Int {
            return offsets.count { (dx, dy) -> hasWordAt(x, y, dx, dy) }
        }

        return (0..maxX).sumOf { x ->
            (0..maxY).sumOf { y ->
                countWordsAt(x, y)
            }
        }
    }

    fun part2(input: List<String>): Int {
        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        fun isCenterOfWord(x: Int, y: Int): Boolean {
            if (input[y][x] != 'A') return false

            val diagonal1 = setOf(input[y - 1][x - 1], input[y + 1][x + 1]) == setOf('M', 'S')
            val diagonal2 = setOf(input[y - 1][x + 1], input[y + 1][x - 1]) == setOf('M', 'S')

            return diagonal1 && diagonal2
        }

        return (1..maxX - 1).sumOf { x ->
            (1..maxY - 1).count { y ->
                isCenterOfWord(x, y)
            }
        }
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 18)

    val input = readInput("Day04")
    part1(input).println()

    check(part2(testInput) == 9)
    part2(input).println()
}