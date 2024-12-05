fun main() {
    fun prepareDependencies(rules: List<String>): Map<Int, Set<Int>> {
        val dependencies = mutableMapOf<Int, MutableSet<Int>>()

        rules.map { it.split("|") }
            .map { (before, after) -> before.toInt() to after.toInt() }
            .forEach { (before, after) -> dependencies.computeIfAbsent(after) { mutableSetOf()}.add(before) }

        return dependencies
    }

    fun isCorrect(
        pages: List<Int>,
        dependencies: Map<Int, Set<Int>>
    ): Boolean {
        val seen = mutableSetOf<Int>()

        pages.forEach { page ->
            val required = dependencies[page] ?: emptySet()
            val missing = required - seen

            if (missing.isNotEmpty() && missing.any { it in pages }) {
                return false
            }

            seen.add(page)
        }

        return true
    }

    fun part1(input: String): Int {
        val (rulesLines, pagesLines) = input.split("\n\n")
            .map { it.lines() }

        val dependencies = prepareDependencies(rulesLines)
        val pages = pagesLines.map { line -> line.split(",").map { it.toInt() } }

        return pages.filter { isCorrect(it, dependencies) }
            .sumOf { it[it.size / 2] }
    }

    fun part2(input: String): Int {
        TODO()
    }

    val testInput = readEntireInput("Day05_test")
    check(part1(testInput) == 143)

    val input = readEntireInput("Day05")
    part1(input).println()

    check(part2(testInput) == 9)
    part2(input).println()
}