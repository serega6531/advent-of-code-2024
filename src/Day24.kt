fun main() {

    fun parseInput(input: String): ParsedInput {
        val (startingWiresString, gatesString) = input.split("\n\n")

        val startingWires = startingWiresString.lines()
            .map { it.split(": ") }
            .associate { (name, value) -> Pair(name, value == "1") }

        val gatePattern = Regex("([a-z0-9]+) (AND|OR|XOR) ([a-z0-9]+) -> ([a-z0-9]+)")
        val gates = gatesString.lines()
            .map { gatePattern.matchEntire(it)!!.destructured }
            .map { (wire1, operation, wire2, wire3) ->
                GateDescription(
                    wire1,
                    wire2,
                    GateOperation.valueOf(operation),
                    wire3
                )
            }

        return ParsedInput(startingWires, gates)
    }

    fun getOutput(
        wire: String,
        wires: MutableMap<String, Boolean>,
        gateByOutput: Map<String, GateDescription>
    ): Boolean {
        fun evaluate(gate: GateDescription): Boolean {
            val left = getOutput(gate.leftWire, wires, gateByOutput)
            val right = getOutput(gate.rightWire, wires, gateByOutput)

            return when (gate.operation) {
                GateOperation.AND -> left && right
                GateOperation.OR -> left || right
                GateOperation.XOR -> left xor right
            }
        }

        wires[wire]?.let { return it }

        val sourceGate = gateByOutput.getValue(wire)
        val result = evaluate(sourceGate)
        wires[wire] = result

        return result
    }

    fun collectOutput(results: Map<Int, Boolean>): Long {
        val asString = buildString {
            results.keys.sortedDescending().forEach {
                append(if (results.getValue(it)) 1 else 0)
            }
        }

        return asString.toLong(2)
    }

    fun part1(input: String): Long {
        val (startingWires, gates) = parseInput(input)
        val gateByOutput = gates.associateBy { gate -> gate.resultWire }

        val wires = startingWires.toMutableMap()

        val results = gateByOutput.keys.filter { it.startsWith("z") }
            .associate { it.substring(1).toInt() to getOutput(it, wires, gateByOutput) }

        val output = collectOutput(results)
        return output
    }

    fun part2(input: String): String {
        TODO()
    }

    val testInput = readEntireInput("Day24_test")
    val testInput2 = readEntireInput("Day24_test2")
    check(part1(testInput) == 4L)
    check(part1(testInput2) == 2024L)

    val input = readEntireInput("Day24")
    part1(input).println()

    check(part2(testInput) == "TODO")
    part2(input).println()
}

private data class ParsedInput(
    val startingWires: Map<String, Boolean>,
    val gates: List<GateDescription>
)

private data class GateDescription(
    val leftWire: String,
    val rightWire: String,
    val operation: GateOperation,
    val resultWire: String
)

private enum class GateOperation {
    AND, OR, XOR
}