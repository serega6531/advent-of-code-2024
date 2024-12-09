import java.util.*

fun main() {

    fun buildLayout(input: String): List<Int?> {
        return buildList {
            var fileNext = true
            var currentId = 0

            input.forEach { c ->
                val size = c.digitToInt()

                if (size > 0) {
                    val toAdd = if (fileNext) currentId++ else null
                    addAll(Collections.nCopies(size, toAdd))
                }

                fileNext = !fileNext
            }
        }
    }

    fun calculateChecksum(compressed: List<Int>): Long = compressed.withIndex()
        .sumOf { (index, value) -> (index * value).toLong() }

    fun part1(input: String): Long {
        fun compressLayout(layout: List<Int?>): List<Int> {
            val result = layout.toMutableList()
            var p1 = 0
            var p2 = layout.lastIndex

            while (p1 < p2) {
                when {
                    result[p1] != null -> p1++
                    result[p2] == null -> p2--
                    else -> {
                        result[p1] = result[p2]
                        result[p2] = null
                    }
                }
            }

            val end = result.indexOfFirst { it == null }.takeIf { it != -1 } ?: result.size
            return result.subList(0, end).asNotNull()
        }

        val layout = buildLayout(input)
        val compressed = compressLayout(layout)

        return calculateChecksum(compressed)
    }

    fun part2(input: String): Long {
        TODO()
    }

    val testInput = readEntireInput("Day09_test")
    check(part1(testInput) == 1928L)

    val input = readEntireInput("Day09")
    part1(input).println()

    check(part2(testInput) == 2858L)
    part2(input).println()
}