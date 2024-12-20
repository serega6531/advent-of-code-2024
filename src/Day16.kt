import kotlin.collections.Map

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
            return input.findCoordinate('S')
        }

        fun findEnd(): YX {
            return input.findCoordinate('E')
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

                        add(MazeStep(a1, b1, 1))
                        add(MazeStep(a2, b2, 1))
                    }

                    val position = MazePosition(yx.y, yx.x, direction)
                    val adjacent = adjacentDirections.getValue(direction)
                    add(MazeStep(position, position.copy(direction = adjacent.first), 1000))
                    add(MazeStep(position, position.copy(direction = adjacent.second), 1000))
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
        val ends = cardinals.map { MazePosition(graph.end.y, graph.end.x, it) }.toSet()
        val result = graph.findShortestPath(start, ends)
        val paths = result.shortestPaths()
        val distance = result.shortestDistance()!!

        val lastTurns = countLastTurns(paths.first())
        return distance - (lastTurns * 1000)
    }

    fun part2(input: List<String>): Int {
        val graph = createGraph(input)
        val start = MazePosition(graph.start.y, graph.start.x, DirectionOffset(0, 1))
        val ends = cardinals.map { MazePosition(graph.end.y, graph.end.x, it) }.toSet()
        val result = graph.findShortestPath(start, ends)
        val paths = result.shortestPaths()

        val tiles: Set<YX> = paths.flatMapTo(mutableSetOf()) { path -> path.map { YX(it.y, it.x) } }
        return tiles.size
    }

    val testInput = readInput("Day16_test")
    val testInput2 = readInput("Day16_test2")

    check(part1(testInput) == 7036)
    check(part1(testInput2) == 11048)

    val input = readInput("Day16")
    part1(input).println()

    check(part2(testInput) == 45)
    check(part2(testInput2) == 64)
    part2(input).println()
}

private data class MazePosition(val y: Int, val x: Int, val direction: DirectionOffset) : Graph.Vertex

private data class MazeStep(
    override val a: MazePosition,
    override val b: MazePosition,
    override val distance: Int
) : Graph.Edge<MazePosition>

private class MazeGraph(edges: List<MazeStep>, val start: YX, val end: YX) : DijstraAlgorithm<MazePosition, MazeStep>(edges)

// Updated A* code from https://github.com/GustavoHGAraujo/kotlin-a-star-algorithm

private interface Graph {
    interface Vertex
    interface Edge<T : Vertex> {
        val a: T
        val b: T
        val distance: Int
    }
}

// https://gist.github.com/trygvea/6067a744ee67c2f0447c3c7f5b715d62
private abstract class DijstraAlgorithm<V : Graph.Vertex, E : Graph.Edge<V>>(val edges: List<E>) {
    /**
     * See https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
     */
    fun findShortestPath(source: V, targets: Set<V>): ShortestPathResult<V> {
         val dist = mutableMapOf<V, Int>()
        val prev = mutableMapOf<V, MutableSet<V>>()
        val q = findDistinctVs(edges)
        var shortestPathLength = Integer.MAX_VALUE

        q.forEach { v ->
            dist[v] = Integer.MAX_VALUE
            prev[v] = mutableSetOf()
        }
        dist[source] = 0

        while (q.isNotEmpty()) {
            val u = q.minBy { dist[it] ?: 0 }
            q.remove(u)

            if ((dist[u] ?: 0) > shortestPathLength) {
                break
            }

            if (u in targets) {
                shortestPathLength = dist.getValue(u)
            }
            edges
                .filter { it.a == u }
                .forEach { edge ->
                    val v = edge.b
                    val alt = (dist[u] ?: 0) + edge.distance
                    val currentDist = dist[v] ?: 0
                    if (alt < currentDist) {
                        dist[v] = alt
                        prev[v] = mutableSetOf(u)
                    } else if (alt == currentDist) {
                        prev.getValue(v).add(u)
                    }
                }
        }

        return ShortestPathResult(prev, dist, source, targets)
    }

    private fun findDistinctVs(edges: List<E>): MutableSet<V> {
        val nodes = mutableSetOf<V>()
        edges.forEach {
            nodes.add(it.a)
            nodes.add(it.b)
        }
        return nodes
    }

    class ShortestPathResult<V : Graph.Vertex>(val prev: Map<V, Set<V>>, val dist: Map<V, Int>, val source: V, val targets: Set<V>) {

        fun shortestPaths(): List<List<V>> {
            val shortest = shortestDistance() ?: return emptyList()
            val reachedTargets = targets.filter { dist[it] == shortest }
            return reachedTargets.flatMap { shortestPaths(source, it, emptyList()) }
        }

        private fun shortestPaths(from: V, to: V, list: List<V>): List<List<V>> {
            val last = prev[to]?.takeIf { it.isNotEmpty() } ?: return if (from == to) {
                listOf(list + to)
            } else {
                emptyList()
            }

            return last.flatMap { shortestPaths(from, it, list) }.map { it + to }
        }

        fun shortestDistance(): Int? {
            val shortest = targets.minOf { dist.getValue(it) }
            if (shortest == Integer.MAX_VALUE) {
                return null
            }
            return shortest
        }
    }
}