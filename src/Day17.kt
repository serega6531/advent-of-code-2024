import java.math.BigInteger

fun main() {

    fun parseInput(input: String): Pair<VM, List<Int>> {
        val (registersString, programString) = input.split("\n\n")

        val (a, b, c) = registersString.lines()
            .map { it.substringAfter(": ").toBigInteger() }

        val program = programString.substringAfter(": ").split(",").map { it.toInt() }

        return VM(a, b, c) to program
    }

    fun part1(input: String): String {
        val (vm, program) = parseInput(input)

        vm.runUntilHalt(program)
        return vm.output.joinToString(",") { it.toString() }
    }

    fun part2(input: String): BigInteger {
        val (baseVM, program) = parseInput(input)

        fun bruteforce(): Int {
            repeat(Int.MAX_VALUE) { a ->
                val vm = baseVM.copy()
                vm.a = a.toBigInteger()
                vm.runUntilHalt(program)

                if (vm.output == program) {
                    return a
                }
            }

            throw IllegalStateException("Result not found")
        }

        fun guessNthNumber(base: BigInteger, n: Int): Int { // only works for programs with a = a % 8
            (0..500000).forEach { guess ->
                val vm = baseVM.copy()
                vm.a = base + guess.toBigInteger()
                vm.runUntilHalt(program)

                if (vm.output.takeLast(n) == program.takeLast(n)) {
                    return guess
                }
            }

            throw IllegalStateException("Could not find a solution for ${n}th number")
        }

        if (program.size < 8) {
            return bruteforce().toBigInteger()
        }

        var result = BigInteger.ZERO

        repeat(program.size) {
            result = result * BigInteger.valueOf(8)
            val guess = guessNthNumber(result, it + 1)
            result = result + guess.toBigInteger()
        }

        return result
    }

    val testInput = readEntireInput("Day17_test")
    check(part1(testInput) == "4,6,3,5,6,3,5,2,1,0")

    val input = readEntireInput("Day17")
    part1(input).println()

    val testInput2 = readEntireInput("Day17_test2")
    check(part2(testInput2) == BigInteger.valueOf(117440))
    part2(input).println()
}

private class VM(
    var a: BigInteger,
    var b: BigInteger,
    var c: BigInteger
) {

    private var ip: Int = 0

    val output: MutableList<Int> = mutableListOf()

    fun runUntilHalt(program: List<Int>) {
        while (ip < program.size) {
            val currentIp = ip
            val instruction = program[currentIp]
            val operand = program[currentIp + 1].toBigInteger()

            execute(instruction, operand)

            if (ip == currentIp) {
                // did not jump
                ip += 2
            }
        }
    }

    private fun execute(instruction: Int, operand: BigInteger) {
        when (instruction) {
            0 -> adv(unpackComboOperand(operand))
            1 -> bxl(operand)
            2 -> bst(unpackComboOperand(operand))
            3 -> jnz(operand)
            4 -> bxc()
            5 -> out(unpackComboOperand(operand))
            6 -> bdv(unpackComboOperand(operand))
            7 -> cdv(unpackComboOperand(operand))
            else -> throw IllegalArgumentException("Unknown instruction: $instruction")
        }
    }

    private fun unpackComboOperand(operand: BigInteger): BigInteger {
        return when (operand) {
            BigInteger.ZERO, BigInteger.ONE, BigInteger.TWO, BigInteger.valueOf(3) -> operand
            BigInteger.valueOf(4) -> a
            BigInteger.valueOf(5) -> b
            BigInteger.valueOf(6) -> c
            BigInteger.valueOf(7) -> throw IllegalArgumentException("Operand 7 is reserved")
            else -> throw IllegalArgumentException("Invalid operand $operand")
        }
    }

    private fun adv(operand: BigInteger) {
        a = a / (BigInteger.TWO.pow(operand.toInt()))
    }

    private fun bxl(operand: BigInteger) {
        b = b xor operand
    }

    private fun bst(operand: BigInteger) {
        b = operand % 8.toBigInteger()
    }

    private fun jnz(operand: BigInteger) {
        if (a == BigInteger.ZERO) {
            return
        }

        ip = operand.toInt()
    }

    private fun bxc() {
        b = b xor c
    }

    private fun out(operand: BigInteger) {
        output.add((operand % 8.toBigInteger()).toInt())
    }

    private fun bdv(operand: BigInteger) {
        b = a / (BigInteger.TWO.pow(operand.toInt()))
    }

    private fun cdv(operand: BigInteger) {
        c = a / (BigInteger.TWO.pow(operand.toInt()))
    }

    fun copy() = VM(a, b, c)

}