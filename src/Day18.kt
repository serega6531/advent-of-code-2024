package day18

import DirectionOffset
import YX
import println
import readInput
import kotlin.math.abs

fun main() {
    val cardinals = listOf<DirectionOffset>(
        DirectionOffset(-1, 0),
        DirectionOffset(0, 1),
        DirectionOffset(1, 0),
        DirectionOffset(0, -1)
    )

    fun parseInput(input: List<String>, bytes: Int): Set<YX> {
        return input.take(bytes)
            .map { it.split(',') }
            .mapTo(mutableSetOf()) { (x, y) -> YX(y.toInt(), x.toInt()) }
    }

    fun createGraph(corrupted: Set<YX>, fieldSize: Int): MemoryGraph {
        val bounds = 0..fieldSize

        val edges = buildList {
            bounds.forEach { y ->
                bounds.forEach { x ->
                    val yx = YX(y, x)

                    if (yx !in corrupted) {
                        cardinals.forEach { direction ->
                            val neighbor = yx + direction
                            if (neighbor.y in bounds && neighbor.x in bounds && neighbor !in corrupted) {
                                add(MemoryMove(yx.toMemoryPosition(), neighbor.toMemoryPosition()))
                            }
                        }
                    }
                }
            }
        }

        return MemoryGraph(edges, MemoryPosition(0, 0), MemoryPosition(fieldSize, fieldSize))
    }

    fun part1(input: List<String>, fieldSize: Int, bytes: Int): Int {
        val corrupted = parseInput(input, bytes)
        val graph = createGraph(corrupted, fieldSize)
        val (_, cost) = graph.findPath(graph.start, graph.end)

        return cost
    }

    fun part2(input: List<String>, fieldSize: Int) {
        TODO()
    }

    val testInput = readInput("Day18_test")
    check(part1(testInput, 6, 12) == 22)

    val input = readInput("Day18")
    part1(input, 70, 1024).println()

    part2(input, 70).println()
}

private fun YX.toMemoryPosition() = MemoryPosition(this.y, this.x)

private data class MemoryPosition(val y: Int, val x: Int) : Graph.Vertex

private data class MemoryMove(override val a: MemoryPosition, override val b: MemoryPosition) :
    Graph.Edge<MemoryPosition>

private class MemoryGraph(edges: List<MemoryMove>, val start: MemoryPosition, val end: MemoryPosition) :
    AlgorithmAStar<MemoryPosition, MemoryMove>(edges) {
    override fun costToMoveThrough(edge: MemoryMove): Int {
        val (a, b) = edge

        return abs(a.y - b.y) + abs(a.x - b.x)
    }

    override fun createEdge(from: MemoryPosition, to: MemoryPosition): MemoryMove {
        return MemoryMove(from, to)
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