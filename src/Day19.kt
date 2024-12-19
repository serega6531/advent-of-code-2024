fun main() {

    fun parseInput(input: String): Pair<Set<String>, List<String>> {
        val (patternsString, designsString) = input.split("\n\n")

        val patterns = patternsString.split(", ").toSet()
        val designs = designsString.lines()

        return patterns to designs
    }

    fun part1(input: String): Int {
        val (patterns, designs) = parseInput(input)

        val minPattern = patterns.minOf { it.length }
        val maxPattern = patterns.maxOf { it.length }
        val patternLengths = minPattern..maxPattern

        fun isPossible(design: String): Boolean {
            if (design.isEmpty()) {
                return true
            }

            return patternLengths.atMost(design.length).reversed().any { length ->
                val prefix = design.substring(0, length)
                val suffix = design.substring(length)

                if (prefix in patterns) {
                    isPossible(suffix)
                } else {
                    false
                }
            }
        }

        return designs.count { isPossible(it) }
    }

    fun part2(input: String): Long {
        val (patterns, designs) = parseInput(input)

        val minPattern = patterns.minOf { it.length }
        val maxPattern = patterns.maxOf { it.length }
        val patternLengths = minPattern..maxPattern
        val counts: MutableMap<String, Long> = mutableMapOf()

        fun countPossibleDesigns(design: String): Long {
            if (design.isEmpty()) {
                return 1
            }

            counts[design]?.let { return it }

            val count = patternLengths.atMost(design.length).reversed().sumOf { length ->
                val prefix = design.substring(0, length)
                val suffix = design.substring(length)

                if (prefix in patterns) {
                    countPossibleDesigns(suffix)
                } else {
                    0
                }
            }

            counts[design] = count
            return count
        }

        return designs.sumOf { countPossibleDesigns(it) }
    }

    val testInput = readEntireInput("Day19_test")
    check(part1(testInput) == 6)

    val input = readEntireInput("Day19")
    part1(input).println()

    check(part2(testInput) == 16L)
    part2(input).println()
}

private fun IntProgression.atMost(max: Int): IntProgression {
    return if (this.last > max) {
        this.first..max
    } else {
        this
    }
}