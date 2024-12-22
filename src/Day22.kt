fun main() {

    fun getNext(prev: Long): Long {
        val a = ((prev * 64) xor prev).mod(16777216)
        val b = ((a / 32) xor a).mod(16777216)
        val c = ((b * 2048) xor b).mod(16777216)
        return c.toLong()
    }

    fun getRandomSequence(seed: Long) = generateSequence(seed, ::getNext)

    fun solve(input: List<Long>): Long {
        return input.map { getRandomSequence(it) }
            .sumOf { it.drop(2000).first() }
    }

    fun part1(input: List<String>): Long {
        return solve(input.map { it.toLong() })
    }

    fun part2(input: List<String>): Long {
        TODO()
    }

    val testInput = readInput("Day22_test")
    check(part1(testInput) == 37327623L)

    val input = readInput("Day22")
    part1(input).println()

    part2(input).println()
}
