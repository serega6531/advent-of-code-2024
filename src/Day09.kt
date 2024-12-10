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

    fun calculateChecksum(compressed: List<Int?>): Long = compressed.withIndex()
        .sumOf { (index, value) -> (index * (value ?: 0)).toLong() }

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
        fun calculateBlockStartsByIndex(): Map<Int, Int> {
            return buildMap {
                var current = 0
                var fileNext = true

                input.forEachIndexed { index, c ->
                    val size = c.digitToInt()

                    if (index % 2 == 0) {
                        put(index, current)
                    }

                    current += size
                    fileNext = !fileNext
                }
            }
        }

        fun calculateEmptySpacesIndexes(): NavigableMap<Int, PriorityQueue<Int>> {
            val result = TreeMap<Int, PriorityQueue<Int>>()

            var fileNext = true
            var index = 0

            input.forEach { c ->
                val size = c.digitToInt()

                if (size > 0 && !fileNext) {
                    result.computeIfAbsent(size) { PriorityQueue() }.add(index)
                }

                index += size
                fileNext = !fileNext
            }

            return result
        }

        val layout = buildLayout(input).toMutableList()
        val blockStartsByIndex = calculateBlockStartsByIndex()
        val emptySpacesIndexes = calculateEmptySpacesIndexes()

        fun moveBlock(blockStart: Int, size: Int, newIndex: Int) {
            repeat(size) { offset ->
                layout[newIndex + offset] = layout[blockStart + offset]
                layout[blockStart + offset] = null
            }
        }

        fun moveBlockToFreeSpace(blockStart: Int, blockSize: Int) {
            val subMap = emptySpacesIndexes.tailMap(blockSize, true)
            if (subMap.isEmpty()) return

            val (spaceSize, queue) =  subMap.entries.minBy { (_, queue) -> queue.peek() }

            if (queue.peek() > blockStart) return
            val spaceIndex = queue.remove()

            if (queue.isEmpty()) {
                emptySpacesIndexes.remove(spaceSize)
            }

            if (spaceSize != blockSize) {
                val remainingSpace = spaceSize - blockSize
                val newSpaceIndex = spaceIndex + blockSize
                emptySpacesIndexes.computeIfAbsent(remainingSpace) { PriorityQueue() }.add(newSpaceIndex)
            }

            moveBlock(blockStart, blockSize, spaceIndex)
        }

        input.withIndex()
            .reversed()
            .filter { (index, _) -> index % 2 == 0 }
            .forEach { (index, p) ->
                val blockSize = p.digitToInt()
                val blockStart = blockStartsByIndex.getValue(index)
                moveBlockToFreeSpace(blockStart, blockSize)
            }

        return calculateChecksum(layout)
    }

    val testInput = readEntireInput("Day09_test")
    check(part1(testInput) == 1928L)

    val input = readEntireInput("Day09")
    part1(input).println()

    check(part2(testInput) == 2858L)
    part2(input).println()
}