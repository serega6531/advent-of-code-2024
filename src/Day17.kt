import kotlin.math.pow

fun main() {

    fun parseInput(input: String): Pair<VM, List<Int>> {
        val (registersString, programString) = input.split("\n\n")

        val (a, b, c) = registersString.lines()
            .map { it.substringAfter(": ").toInt() }

        val program = programString.substringAfter(": ").split(",").map { it.toInt() }

        return VM(a, b, c) to program
    }

    fun part1(input: String): String {
        val (vm, program) = parseInput(input)

        vm.runUntilHalt(program)
        return vm.output.joinToString(",") { it.toString() }
    }

    fun part2(input: String): String {
        TODO()
    }

    val testInput = readEntireInput("Day17_test")
    check(part1(testInput) == "4,6,3,5,6,3,5,2,1,0")

    val input = readEntireInput("Day17")
    part1(input).println()

    part2(input).println()
}

private class VM(
    var a: Int,
    var b: Int,
    var c: Int
) {

    private var ip: Int = 0

    val output: MutableList<Int> = mutableListOf()

    fun runUntilHalt(program: List<Int>) {
        while (ip < program.size) {
            val currentIp = ip
            val instruction = program[currentIp]
            val operand = program[currentIp + 1]

            execute(instruction, operand)

            if (ip == currentIp) {
                // did not jump
                ip += 2
            }
        }
    }

    private fun execute(instruction: Int, operand: Int) {
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

    private fun unpackComboOperand(operand: Int): Int {
        return when (operand) {
            in 0..3 -> operand
            4 -> a
            5 -> b
            6 -> c
            7 -> throw IllegalArgumentException("Operand 7 is reserved")
            else -> throw IllegalArgumentException("Invalid operand $operand")
        }
    }

    private fun adv(operand: Int) {
        a = a / (2.0.pow(operand).toInt())
    }

    private fun bxl(operand: Int) {
        b = b xor operand
    }

    private fun bst(operand: Int) {
        b = operand % 8
    }

    private fun jnz(operand: Int) {
        if (a == 0) {
            return
        }

        ip = operand
    }

    private fun bxc() {
        b = b xor c
    }

    private fun out(operand: Int) {
        output.add(operand % 8)
    }

    private fun bdv(operand: Int) {
        b = a / (2.0.pow(operand).toInt())
    }

    private fun cdv(operand: Int) {
        c = a / (2.0.pow(operand).toInt())
    }

}