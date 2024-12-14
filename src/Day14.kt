fun main() {

    fun parseInput(input: List<String>): List<RobotData> {
        val pattern = Regex("""p=(\d+),(\d+) v=(-?\d+),(-?\d+)""")

        return input.map { pattern.matchEntire(it)!!.destructured }
            .map { (px, py, vx, vy) -> RobotData(px.toInt(), py.toInt(), vx.toInt(), vy.toInt()) }
    }

    fun getNewCoordinate(start: Int, velocity: Int, fieldSize: Int, steps: Int): Int {
        return (start + velocity*steps).mod(fieldSize)
    }

    fun getQuadrant(y: Int, x: Int, maxY: Int, maxX: Int): Int? {
        val middleY = maxY / 2
        val middleX = maxX / 2

        if (y == middleY || x == middleX) {
            return null
        }

        val beforeMiddleY = y < middleY
        val beforeMiddleX = x < middleX

        return when (beforeMiddleY to beforeMiddleX) {
            false to false -> 1
            false to true -> 2
            true to false -> 3
            true to true -> 4
            else -> throw IllegalStateException("unreachable")
        }
    }

    fun printRobots(robots: List<RobotData>, maxY: Int, maxX: Int) {
        val coordinates: Set<YX> = robots.mapTo(mutableSetOf()) { YX(it.py, it.px) }

        (0..<maxY).forEach { y ->
            (0..<maxX).forEach { x ->
                if (YX(y, x) in coordinates) {
                    print('#')
                } else {
                    print(' ')
                }
            }
            println()
        }
    }

    fun part1(input: List<String>, fieldDimensions: Pair<Int, Int>): Int {
        val robots = parseInput(input)
        val steps = 100

        return robots.map {
            val newX = getNewCoordinate(it.px, it.vx, fieldDimensions.second, steps)
            val newY = getNewCoordinate(it.py, it.vy, fieldDimensions.first, steps)

            newY to newX
        }
            .mapNotNull { (newY, newX) -> getQuadrant(newY, newX, fieldDimensions.first, fieldDimensions.second) }
            .groupingBy { it }
            .eachCount()
            .values
            .product()
    }

    fun part2(input: List<String>, fieldDimensions: Pair<Int, Int>, steps: Int) {
        var robots = parseInput(input)

        fun step(): List<RobotData> {
            return robots.map {
                val newX = getNewCoordinate(it.px, it.vx, fieldDimensions.second, 1)
                val newY = getNewCoordinate(it.py, it.vy, fieldDimensions.first, 1)
                RobotData(newX, newY, it.vx, it.vy)
            }
        }

        repeat(steps + 1) {
            println("======================================= $it =======================================")
            printRobots(robots, fieldDimensions.first, fieldDimensions.second)
            robots = step()
        }
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput, 7 to 11) == 12)

    val input = readInput("Day14")
    part1(input, 103 to 101).println()

    part2(input, 103 to 101, 10000)
}

private fun Iterable<Int>.product(): Int {
    return this.reduce { a, b -> a * b }
}

private data class RobotData(val px: Int, val py: Int, val vx: Int, val vy: Int)