fun main() {

    fun parseInput(input: String): List<Long> {
        return input.split(" ").map { it.toLong() }
    }

    fun step(stones: List<Long>): List<Long> {
        val result = ArrayList<Long>(stones.size)

        stones.forEach { stone ->
            val asString = stone.toString()

            when {
                stone == 0L -> result.add(1L)
                asString.length % 2L == 0L -> {
                    val cut = asString.length / 2
                    val left = asString.substring(0, cut).toLong()
                    val right = asString.substring(cut).toLong()
                    result.add(left)
                    result.add(right)
                }
                else -> {
                    result.add(stone * 2024)
                }
            }
        }

        return result
    }

    fun part1(input: String): Int {
        var stones = parseInput(input)

        repeat(25) {
            stones = step(stones)
        }

        return stones.size
    }

    fun part2(input: String): Int {
        TODO()
    }

    val testInput = readEntireInput("Day11_test")
    check(part1(testInput) == 55312)

    val input = readEntireInput("Day11")
    part1(input).println()

    part2(input).println()
}