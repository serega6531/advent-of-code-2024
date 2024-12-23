import kotlin.collections.Map

fun main() {

    fun parseInput(input: List<String>): List<Pair<String, String>> {
        return input.map { it.split('-') }
            .map { Pair(it[0], it[1]) }
    }

    fun buildConnectionsMap(connections: List<Pair<String, String>>): Map<String, List<String>> {
        return buildMap<String, MutableList<String>> {
            connections.forEach { (left, right) ->
                computeIfAbsent(left) { mutableListOf() }.add(right)
                computeIfAbsent(right) { mutableListOf() }.add(left)
            }
        }.toMap()
    }

    fun isClique(first: String, second: String, third: String, connections: Map<String, List<String>>): Boolean {
        fun isConnected(a: String, b: String): Boolean {
            return connections.getValue(a).contains(b)
        }

        return isConnected(first, second) && isConnected(second, third) && isConnected(first, third)
    }

    fun part1(input: List<String>): Int {
        val connections = parseInput(input)
        val connectionsMap = buildConnectionsMap(connections)
        val possibleComputers = connectionsMap.filterValues { it.size >= 2 }.keys

        val cliques = buildList {
            possibleComputers.forEachIndexed { firstIndex, first ->
                possibleComputers.drop(firstIndex + 1).forEachIndexed { secondIndex, second ->
                    possibleComputers.drop(firstIndex + secondIndex + 2).forEach { third ->
                        if (isClique(first, second, third, connectionsMap)) {
                            add(Triple(first, second, third))
                        }
                    }
                }
            }
        }

        val correctCliques = cliques.filter {
            it.first.startsWith('t') || it.second.startsWith('t') || it.third.startsWith('t')
        }

        return correctCliques.size
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day23_test")
    check(part1(testInput) == 7)

    val input = readInput("Day23")
    part1(input).println()

    part2(input).println()
}
