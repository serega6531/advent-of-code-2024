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

    fun resultAsString(results: Map<Int, Boolean>): String {
        return buildString {
            results.keys.sortedDescending().forEach {
                append(if (results.getValue(it)) 1 else 0)
            }
        }
    }

    fun collectOutput(results: Map<Int, Boolean>): Long {
        val asString = resultAsString(results)

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
        fun createStartingWires(prefix: String, value: String): Map<String, Boolean> {
            return value.mapIndexed { index, c ->
                (prefix + index.toString().padStart(2, '0')) to (c == '1')
            }.toMap()
        }

        val (_, gates) = parseInput(input)
        val gateByOutput = gates.associateBy { gate -> gate.resultWire }
        val parser = AdderParser(gateByOutput)

        fun findUsedGates(wire: String): Set<GateDescription> {
            val gate = gateByOutput[wire] ?: return emptySet()
            return findUsedGates(gate.leftWire) + findUsedGates(gate.rightWire) + gate
        }

        fun hasError(wire: String, bit: Int): Boolean {
            try {
                val parsed = parser.parseByName(wire)
                return parsed is AdderSum && parsed.bit == bit
            } catch (e: AdderParserException) {
                return false
            }
        }

        val number = "1".repeat(45)
        val wires = (createStartingWires("x", number) + createStartingWires("y", number)).toMutableMap()

        val results = (0..45).associateWith { getOutput("z" + it.toString().padStart(2, '0'), wires, gateByOutput) }
        val sum = resultAsString(results)

        val used = (0..45).associateWith { findUsedGates("z" + it.toString().padStart(2, '0')) }


        val withErrors = (0..45)
            .filter { hasError("z" + it.toString().padStart(2, '0'), it) }

        TODO()
    }

    val testInput = readEntireInput("Day24_test")
    val testInput2 = readEntireInput("Day24_test2")
    //check(part1(testInput) == 4L)
    //check(part1(testInput2) == 2024L)

    val input = readEntireInput("Day24")
    //part1(input).println()

    // check(part2(testInput) == "TODO")
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

private class AdderParser(
   private val gateByOutput: Map<String, GateDescription>
) {

    fun parse(gate: GateDescription): AdderPart {
        val left = parseByName(gate.leftWire)
        val right = parseByName(gate.rightWire)

        val (first, second) = sortInputs(left, right)

        return when (gate.operation) {
            GateOperation.AND -> parseAnd(gate, first, second)
            GateOperation.OR -> parseOr(gate, first, second)
            GateOperation.XOR -> parseXor(gate, first, second)
        }
    }

    fun parseByName(wire: String): AdderPart {
        return gateByOutput[wire]?.let { parse(it) } ?: parseVariable(wire)
    }

    private fun parseAnd(gate: GateDescription, first: AdderPart, second: AdderPart): AdderPart {
        if (first is X && second is Y && first.bit == second.bit) {
            return HalfAdderCarry(first.bit)
        }

        if (first is AdderSum && second is HalfAdderCarry && first.bit == second.bit + 1) {
            return FullAdderSumAndPart(first.bit)
        }

        if (first is AdderSum && second is FullAdderCarry && first.bit == second.bit + 1) {
            return FullAdderSumAndPart(first.bit)
        }

        throw AdderParserException(gate)
    }

    private fun parseOr(gate: GateDescription, first: AdderPart, second: AdderPart): AdderPart {
        if (first is FullAdderSumAndPart && second is HalfAdderCarry && first.bit == second.bit) {
            return FullAdderCarry(first.bit)
        }

        throw AdderParserException(gate)
    }

    private fun parseXor(gate: GateDescription, first: AdderPart, second: AdderPart): AdderPart {
        if (first is X && second is Y && first.bit == second.bit) {
            return AdderSum(first.bit)
        }

        if (first is AdderSum && second is HalfAdderCarry && first.bit == second.bit + 1) {
            return AdderSum(first.bit)
        }

        if (first is AdderSum && second is FullAdderCarry && first.bit == second.bit + 1) {
            return AdderSum(first.bit)
        }

        throw AdderParserException(gate)
    }

    private fun sortInputs(left: AdderPart, right: AdderPart): List<AdderPart> {
        return listOf(left, right).sortedBy { it.javaClass.simpleName }
    }

    private fun parseVariable(name: String): AdderPart {
        val bit = name.substring(1).toInt()
        return if (name.startsWith('x')) {
            X(bit)
        } else {
            Y(bit)
        }
    }

}

private sealed interface AdderPart {
    val bit: Int
}

private data class HalfAdderCarry(override val bit: Int) : AdderPart
private data class FullAdderCarry(override val bit: Int) : AdderPart
private data class FullAdderSumAndPart(override val bit: Int) : AdderPart
private data class AdderSum(override val bit: Int) : AdderPart
private data class X(override val bit: Int) : AdderPart
private data class Y(override val bit: Int) : AdderPart

private class AdderParserException(val gate: GateDescription) : RuntimeException()