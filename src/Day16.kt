import kotlin.collections.Map
import kotlin.math.abs

fun main() {
    val cardinals = listOf<DirectionOffset>(
        DirectionOffset(-1, 0),
        DirectionOffset(0, 1),
        DirectionOffset(1, 0),
        DirectionOffset(0, -1)
    )

    fun createGraph(input: List<String>): MazeGraph {
        val adjacentDirections = mapOf(
            cardinals[0] to Pair(cardinals[1], cardinals[3]),
            cardinals[1] to Pair(cardinals[0], cardinals[2]),
            cardinals[2] to Pair(cardinals[1], cardinals[3]),
            cardinals[3] to Pair(cardinals[0], cardinals[2])
        )

        fun findStart(): YX {
            input.forEachIndexed { y, x, ch ->
                if (ch == 'S') {
                    return YX(y, x)
                }
            }

            throw IllegalArgumentException()
        }

        fun findEnd(): YX {
            input.forEachIndexed { y, x, ch ->
                if (ch == 'E') {
                    return YX(y, x)
                }
            }

            throw IllegalArgumentException()
        }

        fun prepareEdges(yx: YX): List<MazeStep> {
            return buildList {
                cardinals.forEach { direction ->
                    val neighbor = yx + direction
                    val (dy, dx) = direction

                    if (input.inBounds(neighbor) && input[neighbor] != '#') {
                        val a1 = MazePosition(yx.y, yx.x, DirectionOffset(dy, dx))
                        val b1 = MazePosition(neighbor.y, neighbor.x, DirectionOffset(dy, dx))

                        val a2 = MazePosition(neighbor.y, neighbor.x, DirectionOffset(-dy, -dx))
                        val b2 = MazePosition(yx.y, yx.x, DirectionOffset(-dy, -dx))

                        add(MazeStep(a1, b1))
                        add(MazeStep(a2, b2))
                    }

                    val position = MazePosition(yx.y, yx.x, direction)
                    val adjacent = adjacentDirections.getValue(direction)
                    add(MazeStep(position, position.copy(direction = adjacent.first)))
                    add(MazeStep(position, position.copy(direction = adjacent.second)))
                }
            }
        }

        val start = findStart()
        val end = findEnd()
        val edges = buildList {
            input.forEachIndexed { y, x, c ->
                if (c != '#') {
                    addAll(prepareEdges(YX(y, x)))
                }
            }
        }

        return MazeGraph(edges, start, end)
    }

    fun countLastTurns(path: List<MazePosition>): Int {
        var remaining = path
        while (remaining[remaining.lastIndex].direction != remaining[remaining.lastIndex - 1].direction) {
            remaining = remaining.subList(0, remaining.lastIndex)
        }

        return path.size - remaining.size
    }

    fun part1(input: List<String>): Int {
        val graph = createGraph(input)
        val start = MazePosition(graph.start.y, graph.start.x, DirectionOffset(0, 1))
        val end = MazePosition(graph.end.y, graph.end.x, DirectionOffset(0, 1))
        val (path, cost) = graph.findPath(start, end)
        val lastTurns = countLastTurns(path)
        return cost - (lastTurns * 1000) - 1
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day16_test")
    val testInput2 = readInput("Day16_test2")

    check(part1(testInput) == 7036)
    check(part1(testInput2) == 11048)

    val input = readInput("Day16")
    part1(input).println()

    check(part1(testInput) == 45)
    check(part1(testInput2) == 64)
    part2(input).println()
}

private data class MazePosition(val y: Int, val x: Int, val direction: DirectionOffset) : Graph.Vertex

private data class MazeStep(override val a: MazePosition, override val b: MazePosition) : Graph.Edge<MazePosition>

private class MazeGraph(edges: List<MazeStep>, val start: YX, val end: YX) : AlgorithmAStar<MazePosition, MazeStep>(edges) {
    override fun costToMoveThrough(edge: MazeStep): Int {
        val (a, b) = edge

        val dist = abs(a.y - b.y) + abs(a.x - b.x)
        if (dist <= 1) {
            return if (a.direction != b.direction) {
                1000
            } else {
                1
            }
        }

        return dist
    }

    override fun createEdge(from: MazePosition, to: MazePosition): MazeStep {
        return MazeStep(from, to)
    }
}

// Updated A* code from https://github.com/GustavoHGAraujo/kotlin-a-star-algorithm

private interface Graph {
    interface Vertex
    interface Edge<T : Vertex> {
        val a: T
        val b: T
    }
}

private abstract class AlgorithmAStar<V : Graph.Vertex, E : Graph.Edge<V>>(
    val edges: List<E>
) : Graph {

    private val neighbors: Map<V, Set<V>> = calculateNeighbors()

    private fun calculateNeighbors(): Map<V, Set<V>> {
        val result = mutableMapOf<V, MutableSet<V>>()

        edges.forEach {
            val list = result.computeIfAbsent(it.a) { mutableSetOf() }
            list.add(it.b)
        }

        return result
    }
    private val E.cost: Int
        get() = costToMoveThrough(this)

    private fun findRoute(from: V, to: V): E? {
        return edges.find {
            it.a == from && it.b == to
        }
    }

    private fun findRouteOrElseCreateIt(from: V, to: V): E {
        return findRoute(from, to) ?: createEdge(from, to)
    }

    private fun generatePath(currentPos: V, cameFrom: Map<V, V>): List<V> {
        val path = mutableListOf(currentPos)
        var current = currentPos

        while (cameFrom.containsKey(current)) {
            current = cameFrom.getValue(current)
            path.add(0, current)
        }
        return path.toList()
    }

    abstract fun costToMoveThrough(edge: E): Int
    abstract fun createEdge(from: V, to: V): E

    fun findPath(begin: V, end: V): Pair<List<V>, Int> {
        val cameFrom = mutableMapOf<V, V>()

        val openVertices = mutableSetOf(begin)
        val closedVertices = mutableSetOf<V>()

        val costFromStart = mutableMapOf(begin to 0)

        val estimatedRoute = findRouteOrElseCreateIt(from = begin, to = end)
        val estimatedTotalCost = mutableMapOf(begin to estimatedRoute.cost)

        while (openVertices.isNotEmpty()) {
            val currentPos = openVertices.minBy { estimatedTotalCost.getValue(it) }

            // Check if we have reached the finish
            if (currentPos == end) {
                // Backtrack to generate the most efficient path
                val path = generatePath(currentPos, cameFrom)

                // First Route to finish will be optimum route
                return Pair(path, estimatedTotalCost.getValue(end))
            }

            // Mark the current vertex as closed
            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            (neighbors.getValue(currentPos) - closedVertices).forEach { neighbour ->
                val routeCost = findRouteOrElseCreateIt(from = currentPos, to = neighbour).cost
                val cost: Int = costFromStart.getValue(currentPos) + routeCost

                if (cost < costFromStart.getOrDefault(neighbour, Int.MAX_VALUE)) {
                    if (!openVertices.contains(neighbour)) {
                        openVertices.add(neighbour)
                    }

                    cameFrom[neighbour] = currentPos
                    costFromStart[neighbour] = cost

                    val estimatedRemainingRouteCost = findRouteOrElseCreateIt(from = neighbour, to = end).cost
                    estimatedTotalCost[neighbour] = cost + estimatedRemainingRouteCost
                }
            }
        }

        throw IllegalArgumentException("No Path from Start $begin to Finish $end")
    }
}