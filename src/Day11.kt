fun main() {

    fun buildCounts(input: String): Map<Long, Long> {
        return input.split(" ")
            .map { it.toLong() }
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, v) -> v.toLong() }
    }

    fun step(stones: Map<Long, Long>): Map<Long, Long> {
        val result = mutableMapOf<Long, Long>()

        fun addStones(value: Long, count: Long) {
            result.merge(value, count) { a, b -> a + b }
        }

        stones.forEach { (stone, count) ->
            val asString = stone.toString()

            when {
                stone == 0L -> addStones(1L, count)
                asString.length % 2L == 0L -> {
                    val cut = asString.length / 2
                    val left = asString.substring(0, cut).toLong()
                    val right = asString.substring(cut).toLong()
                    addStones(left, count)
                    addStones(right, count)
                }

                else -> addStones(stone * 2024, count)
            }
        }

        return result
    }

    fun solve(input: String, steps: Int): Long {
        var stones = buildCounts(input)

        repeat(steps) {
            stones = step(stones)
        }

        return stones.values.sum()
    }

    fun part1(input: String): Long {
        return solve(input, 25)
    }

    fun part2(input: String): Long {
        return solve(input, 75)
    }

    val testInput = readEntireInput("Day11_test")
    check(part1(testInput) == 55312L)

    val input = readEntireInput("Day11")
    part1(input).println()

    part2(input).println()
}