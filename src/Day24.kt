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

    fun part1(input: String): Long {
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

        val (startingWires, gates) = parseInput(input)
        val gateByOutput = gates.associateBy { gate -> gate.resultWire }

        val wires = startingWires.toMutableMap()

        val results = gateByOutput.keys.filter { it.startsWith("z") }
            .associate { it.substring(1).toInt() to getOutput(it, wires, gateByOutput) }

        val output = collectOutput(results)
        return output
    }

    fun part2(input: String): String {
        val (_, gates) = parseInput(input)

        val outputWiresCount = gates
            .map { it.resultWire }
            .filter { it.startsWith('z') }
            .maxOf { it.substring(1).toInt() }

        fun isValid(wire: String, bit: Int, gatesMap: Map<String, GateDescription>): Boolean {
            val parser = AdderParser(gatesMap)

            return try {
                val parsed = parser.parseByName(wire)

                val validHalfAdder = bit == 0 && parsed is HalfAdderSumOrFullAdderSumXorPart && parsed.bit == bit
                val validFullAdder = bit > 0 && parsed is FullAdderSum && parsed.bit == bit
                val validCarry = bit == outputWiresCount && parsed is FullAdderCarry && parsed.bit == bit - 1

                validHalfAdder || validFullAdder || validCarry
            } catch (e: AdderParserException) {
                false
            } catch (e: StackOverflowError) {
                false
            }
        }

        fun errorInOperands(wire: String, gatesMap: Map<String, GateDescription>): Boolean {
            val parser = AdderParser(gatesMap)

            return try {
                parser.parseByName(wire)
                false
            } catch (e: AdderParserException) {
                true
            }
        }

        fun swapGatesInMap(
            errorWire: String,
            possiblePair: String,
            gatesMap: Map<String, GateDescription>
        ): Map<String, GateDescription> {
            val errorGate = gatesMap.getValue(errorWire)
            val replacementGate = gatesMap.getValue(possiblePair)

            val updatedErrorGate = errorGate.copy(resultWire = possiblePair)
            val updatedReplacementGate = replacementGate.copy(resultWire = errorWire)

            return gatesMap.toMutableMap().apply {
                set(possiblePair, updatedErrorGate)
                set(errorWire, updatedReplacementGate)
            }
        }

        fun tryFindPair(
            errorWire: String,
            resultWire: String,
            bit: Int,
            gatesMap: Map<String, GateDescription>
        ): Pair<String, String>? {
            val secondInPair = gatesMap.keys.singleOrNull { possiblePair ->
                val updatedMap = swapGatesInMap(errorWire, possiblePair, gatesMap)

                isValid(resultWire, bit, updatedMap)
            }

            return secondInPair?.let { errorWire to it }
        }

        fun fixError(
            errorWire: String,
            bit: Int,
            gatesMap: Map<String, GateDescription>,
            swappedWires: MutableList<String>
        ): Map<String, GateDescription> {
            val wiresToSwap = if (errorInOperands(errorWire, gatesMap)) {
                val errorGate = gatesMap.getValue(errorWire)
                listOf(errorGate.leftWire, errorGate.rightWire)
            } else {
                listOf(errorWire)
            }

            val (toSwap1, toSwap2) = wiresToSwap.firstNotNullOf { wireToSwap ->
                tryFindPair(wireToSwap, errorWire, bit, gatesMap)
            }

            println("Swapping $toSwap1 and $toSwap2 to fix $errorWire")
            swappedWires.add(toSwap1)
            swappedWires.add(toSwap2)

            return swapGatesInMap(toSwap1, toSwap2, gatesMap)
        }

        fun Int.toResultWire() = "z" + this.toString().padStart(2, '0')

        fun findRequiredSwaps(): List<String> {
            val result = mutableListOf<String>()

            var gatesMap = gates.associateBy { gate -> gate.resultWire }

            while (true) {
                val firstNotValid = (0..outputWiresCount)
                    .associateWith { it.toResultWire() }
                    .entries
                    .find { (bit, wire) -> !isValid(wire, bit, gatesMap) }

                if (firstNotValid == null) {
                    return result
                }

                val (errorBit, errorWire) = firstNotValid

                gatesMap = fixError(errorWire, errorBit, gatesMap, result)
            }
        }

        val requiredSwaps = findRequiredSwaps()
        return requiredSwaps.sorted().joinToString(separator = ",")
    }

    val testInput = readEntireInput("Day24_test")
    val testInput2 = readEntireInput("Day24_test2")
    check(part1(testInput) == 4L)
    check(part1(testInput2) == 2024L)

    val input = readEntireInput("Day24")
    part1(input).println()

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
            return HalfAdderCarry(first.bit, gate.resultWire)
        }

        if (first is HalfAdderCarry && second is HalfAdderSumOrFullAdderSumXorPart && first.bit + 1 == second.bit) {
            return FullAdderCarryAndPart(second.bit, gate.resultWire)
        }

        if (first is FullAdderCarry && second is HalfAdderSumOrFullAdderSumXorPart && first.bit + 1 == second.bit) {
            return FullAdderCarryAndPart(second.bit, gate.resultWire)
        }

        throw AdderParserException()
    }

    private fun parseOr(gate: GateDescription, first: AdderPart, second: AdderPart): AdderPart {
        if (first is FullAdderCarryAndPart && second is HalfAdderCarry && first.bit == second.bit) {
            return FullAdderCarry(first.bit, gate.resultWire)
        }

        throw AdderParserException()
    }

    private fun parseXor(gate: GateDescription, first: AdderPart, second: AdderPart): AdderPart {
        if (first is X && second is Y && first.bit == second.bit) {
            return HalfAdderSumOrFullAdderSumXorPart(first.bit, gate.resultWire)
        }

        if (first is HalfAdderCarry && second is HalfAdderSumOrFullAdderSumXorPart && first.bit + 1 == second.bit) {
            return FullAdderSum(second.bit, gate.resultWire)
        }

        if (first is FullAdderCarry && second is HalfAdderSumOrFullAdderSumXorPart && first.bit + 1 == second.bit) {
            return FullAdderSum(second.bit, gate.resultWire)
        }

        throw AdderParserException()
    }

    private fun sortInputs(left: AdderPart, right: AdderPart): List<AdderPart> {
        return listOf(left, right).sortedBy { it.javaClass.simpleName }
    }

    private fun parseVariable(name: String): AdderPart {
        val bit = name.substring(1).toInt()
        return if (name.startsWith('x')) {
            X(bit, name)
        } else {
            Y(bit, name)
        }
    }

}

private sealed interface AdderPart {
    val bit: Int
    val wire: String
}

private data class HalfAdderCarry(override val bit: Int, override val wire: String) : AdderPart
private data class HalfAdderSumOrFullAdderSumXorPart(override val bit: Int, override val wire: String) : AdderPart
private data class FullAdderCarry(override val bit: Int, override val wire: String) : AdderPart
private data class FullAdderCarryAndPart(override val bit: Int, override val wire: String) : AdderPart
private data class FullAdderSum(override val bit: Int, override val wire: String) : AdderPart
private data class X(override val bit: Int, override val wire: String) : AdderPart
private data class Y(override val bit: Int, override val wire: String) : AdderPart

private class AdderParserException : RuntimeException()