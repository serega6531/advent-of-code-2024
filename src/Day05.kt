fun main() {
    fun prepareDependencies(rules: List<String>): Map<Int, Set<Int>> {
        val dependencies = mutableMapOf<Int, MutableSet<Int>>()

        rules.map { it.split("|") }
            .map { (before, after) -> before.toInt() to after.toInt() }
            .forEach { (before, after) -> dependencies.computeIfAbsent(after) { mutableSetOf() }.add(before) }

        return dependencies
    }

    fun isCorrect(
        pages: List<Int>,
        dependencies: Map<Int, Set<Int>>
    ): Boolean {
        val seen = mutableSetOf<Int>()

        pages.forEach { page ->
            val required = dependencies[page] ?: emptySet()
            val missing = (required - seen).filter { it in pages }

            if (missing.isNotEmpty()) {
                return false
            }

            seen.add(page)
        }

        return true
    }

    /**
     * Shift the page with a missing dependency to be after its last dependency
     */
    fun tryFixPage(
        pages: List<Int>,
        dependencies: Map<Int, Set<Int>>
    ): List<Int> {
        val seen = mutableSetOf<Int>()

        pages.forEachIndexed { index, page ->
            val required = dependencies[page] ?: emptySet()
            val missing = (required - seen).filter { it in pages }.maxByOrNull { pages.indexOf(it) }

            if (missing != null) {
                val newIndex = pages.indexOf(missing)
                val updated = buildList {
                    addAll(pages.subList(0, index))                 // copy the chunk before the incorrect page
                    addAll(pages.subList(index + 1, newIndex + 1))  // copy the chunk between the incorrect page and (including) its last dependency
                    add(page)                                       // add the incorrect page back
                    addAll(pages.subList(newIndex + 1, pages.size)) // copy the chunk after the last dependency
                }

                return updated
            }

            seen.add(page)
        }

        throw IllegalArgumentException("Page not broken")
    }

    fun fixUpdate(
        pages: List<Int>,
        dependencies: Map<Int, Set<Int>>
    ): List<Int> {
        var current = pages

        while (!isCorrect(current, dependencies)) {
            current = tryFixPage(current, dependencies)
        }

        return current
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
        val (rulesLines, pagesLines) = input.split("\n\n")
            .map { it.lines() }

        val dependencies = prepareDependencies(rulesLines)
        val pages = pagesLines.map { line -> line.split(",").map { it.toInt() } }

        return pages.filterNot { isCorrect(it, dependencies) }
            .map { fixUpdate(it, dependencies) }
            .sumOf { it[it.size / 2] }
    }

    val testInput = readEntireInput("Day05_test")
    check(part1(testInput) == 143)

    val input = readEntireInput("Day05")
    part1(input).println()

    check(part2(testInput) == 123)
    part2(input).println()
}