fun main() {

    fun findStart(map: List<String>): YX {
        map.forEachIndexed { y, x, ch ->
            if (ch == '^') {
                return YX(y, x)
            }
        }

        throw IllegalArgumentException()
    }

    fun withinBounds(y: Int, x: Int, map: List<String>): Boolean {
        val maxY = map.lastIndex
        val maxX = map.first().lastIndex
        return y in 0..maxY && x in 0..maxX
    }

    fun pathGenerator(map: List<String>): Sequence<PathingState> {
        val (startY, startX) = findStart(map)
        val startingState = PathingState(startY, startX, Direction.UP)

        return generateSequence(startingState) { (y, x, lastDirection) ->
            var nextDirection = lastDirection
            var nextY = y + nextDirection.dy
            var nextX = x + nextDirection.dx

            if (!withinBounds(nextY, nextX, map)) {
                return@generateSequence null
            }

            while (map[nextY][nextX] == '#') {
                nextDirection = nextDirection.turnRight()

                nextY = y + nextDirection.dy
                nextX = x + nextDirection.dx

                if (!withinBounds(nextY, nextX, map)) {
                    return@generateSequence null
                }
            }

            return@generateSequence PathingState(nextY, nextX, nextDirection)
        }
    }

    fun part1(input: List<String>): Int {
        val visited = mutableSetOf<YX>()

        pathGenerator(input).forEach { (y, x, _) ->
            visited.add(YX(y, x))
        }

        return visited.size
    }


    fun becomesLoop(map: List<String>, obstacleY: Int, obstacleX: Int): Boolean {
        if (map[obstacleY][obstacleX] == '#' || map[obstacleY][obstacleX] == '^') {
            // already an obstacle or a starting position
            return false
        }

        val seenStates = mutableSetOf<PathingState>()
        val updatedMap = map.toMutableList().apply {
            set(obstacleY, get(obstacleY).replaceRange(obstacleX, obstacleX + 1, "#"))
        }

        pathGenerator(updatedMap).forEach { state ->
            if (!seenStates.add(state)) {
                return true
            }
        }

        return false
    }

    fun part2(input: List<String>): Int {
        val maxY = input.lastIndex
        val maxX = input.first().lastIndex

        return (0..maxY).sumOf { obstacleY ->
            (0..maxX).count { obstacleX ->
                becomesLoop(input, obstacleY, obstacleX)
            }
        }
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 41)

    val input = readInput("Day06")
    part1(input).println()

    check(part2(testInput) == 6)
    part2(input).println()
}

private data class PathingState(
    val y: Int,
    val x: Int,
    val direction: Direction
)

private enum class Direction(val dy: Int, val dx: Int) {
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1);

    fun turnRight(): Direction {
        return when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }
    }
}