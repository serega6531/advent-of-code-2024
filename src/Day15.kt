fun main() {

    fun charToDirection(c: Char): Pair<Int, Int> {
        return when (c) {
            '^' -> Pair(-1, 0)
            'v' -> Pair(1, 0)
            '<' -> Pair(0, -1)
            '>' -> Pair(0, 1)
            else -> throw IllegalArgumentException("Invalid direction $c")
        }
    }

    fun parseMovements(input: String): List<Pair<Int, Int>> {
        return input.filter { it in setOf('^', 'v', '<', '>') }
            .map { charToDirection(it) }
    }

    fun part1(input: String): Int {
        fun parseMap(input: String): Map {
            val objects = input.lines().map { line ->
                line.mapTo(mutableListOf()) { MapObject.parse(it) }
            }

            return Map(objects)
        }

        val (mapString, movementsString) = input.split("\n\n")

        val map = parseMap(mapString)
        val movements = parseMovements(movementsString)

        movements.forEach { direction ->
            val player = map.findPlayer()
            map.tryMove(player.y, player.x, direction)
        }

        return map.findBoxes()
            .sumOf { (y, x) -> y * 100 + x }
    }

    fun part2(input: String): Int {
        fun parseMap(input: String): Map {
            val objects = input.lines().map { line ->
                line.flatMapTo(mutableListOf()) { MapObject.parse(it).widen() }
            }

            return Map(objects)
        }

        val (mapString, movementsString) = input.split("\n\n")

        val map = parseMap(mapString)
        val movements = parseMovements(movementsString)

        movements.forEach { direction ->
            val player = map.findPlayer()
            map.tryMove(player.y, player.x, direction)
        }

        return map.findBoxes()
            .sumOf { (y, x) -> y * 100 + x }
    }

    val testInput = readEntireInput("Day15_test")
    val testInput2 = readEntireInput("Day15_test2")
    check(part1(testInput) == 2028)
    check(part1(testInput2) == 10092)

    val input = readEntireInput("Day15")
    part1(input).println()

    check(part2(testInput2) == 9021)
    part2(input).println()
}

private class Map(
    private val data: List<MutableList<MapObject>>
) {

    private val maxY: Int
        get() = data.lastIndex

    private val maxX: Int
        get() = data.first().lastIndex

    fun findPlayer(): YX {
        data.forEachIndexed { y, row ->
            row.forEachIndexed { x, col ->
                if (col == MapObject.PLAYER) {
                    return YX(y, x)
                }
            }
        }

        throw IllegalStateException("No player")
    }

    fun findBoxes(): List<YX> = buildList {
        data.forEachIndexed { y, row ->
            row.forEachIndexed { x, col ->
                if (col == MapObject.BOX || col == MapObject.LARGE_BOX_LEFT) {
                    add(YX(y, x))
                }
            }
        }
    }

    private fun canBeMoved(y: Int, x: Int, direction: Pair<Int, Int>): Boolean {
        val obj = data[y][x]
        if (obj == MapObject.WALL || obj == MapObject.EMPTY) {
            return false
        }

        val nextY = y + direction.first
        val nextX = x + direction.second

        if (nextY !in 0..maxY || nextX !in 0..maxX) {
            return false
        }

        return data[nextY][nextX] == MapObject.EMPTY || canBeMovedWide(nextY, nextX, direction)
    }

    private fun canBeMovedWide(y: Int, x: Int, direction: Pair<Int, Int>): Boolean {
        if (direction.second != 0) { // horizontal movement
            return canBeMoved(y, x, direction)
        }

        return when (data[y][x]) {
            MapObject.WALL, MapObject.EMPTY -> false
            MapObject.LARGE_BOX_LEFT -> canBeMoved(y, x, direction) && canBeMoved(y, x + 1, direction)
            MapObject.LARGE_BOX_RIGHT -> canBeMoved(y, x, direction) && canBeMoved(y, x - 1, direction)
            else -> canBeMoved(y, x, direction)
        }
    }

    private fun move(y: Int, x: Int, direction: Pair<Int, Int>) {
        val obj = data[y][x]
        if (obj == MapObject.WALL || obj == MapObject.EMPTY) {
            return
        }

        val nextY = y + direction.first
        val nextX = x + direction.second

        if (nextY !in 0..maxY || nextX !in 0..maxX) {
            return
        }

        moveWide(nextY, nextX, direction)
        if (data[nextY][nextX] == MapObject.EMPTY) {
            data[nextY][nextX] = obj
            data[y][x] = MapObject.EMPTY
        }
    }

    private fun moveWide(y: Int, x: Int, direction: Pair<Int, Int>) {
        if (direction.second != 0) { // horizontal movement
            return move(y, x, direction)
        }

        when (data[y][x]) {
            MapObject.WALL, MapObject.EMPTY -> return
            MapObject.LARGE_BOX_LEFT -> {
                move(y, x, direction)
                move(y, x + 1, direction)
            }

            MapObject.LARGE_BOX_RIGHT -> {
                move(y, x, direction)
                move(y, x - 1, direction)
            }

            else -> move(y, x, direction)
        }
    }

    fun tryMove(y: Int, x: Int, direction: Pair<Int, Int>) {
        if (canBeMovedWide(y, x, direction)) {
            moveWide(y, x, direction)
        }
    }

    override fun toString(): String {
        return data.joinToString("\n") { it.joinToString("") { it.asChar().toString() } }
    }

}

private enum class MapObject {
    EMPTY, PLAYER, BOX, LARGE_BOX_LEFT, LARGE_BOX_RIGHT, WALL;

    fun asChar(): Char {
        return when (this) {
            EMPTY -> '.'
            PLAYER -> '@'
            BOX -> 'O'
            LARGE_BOX_LEFT -> '['
            LARGE_BOX_RIGHT -> ']'
            WALL -> '#'
        }
    }

    fun widen(): List<MapObject> {
        return when (this) {
            BOX -> listOf(LARGE_BOX_LEFT, LARGE_BOX_RIGHT)
            PLAYER -> listOf(PLAYER, EMPTY)
            else -> listOf(this, this)
        }
    }

    companion object {
        fun parse(c: Char) = when (c) {
            '.' -> EMPTY
            '@' -> PLAYER
            'O' -> BOX
            '#' -> WALL
            else -> throw IllegalArgumentException("$c is not a valid object")
        }
    }
}
