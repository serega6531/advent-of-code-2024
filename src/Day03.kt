fun main() {
    fun parseCommands(input: String): Sequence<Command> {
        val regex = Regex("""do\(\)|don't\(\)|mul\((\d{1,3}),(\d{1,3})\)""")

        return regex.findAll(input)
            .map { match ->
                val entire = match.groupValues[0]
                when (entire) {
                    "do()" -> EnableCommand
                    "don't()" -> DisableCommand
                    else -> {
                        val x = match.groupValues[1].toInt()
                        val y = match.groupValues[2].toInt()
                        MultiplyCommand(x, y)
                    }
                }
            }
    }

    fun part1(input: String): Int {
        return parseCommands(input)
            .filterIsInstance<MultiplyCommand>()
            .sumOf { (x, y) -> x * y }
    }

    fun part2(input: String): Int {
        var enabled = true
        var result = 0

        parseCommands(input).forEach { command ->
            when (command) {
                EnableCommand -> enabled = true
                DisableCommand -> enabled = false
                is MultiplyCommand -> {
                    if (enabled) {
                        result += command.x * command.y
                    }
                }
            }
        }

        return result
    }

    val testInput1 = readEntireInput("Day03_test1")
    check(part1(testInput1) == 161)

    val input = readEntireInput("Day03")
    part1(input).println()

    val testInput2 = readEntireInput("Day03_test2")
    check(part2(testInput2) == 48)
    part2(input).println()
}

private sealed interface Command

private data class MultiplyCommand(val x: Int, val y: Int) : Command
private data object EnableCommand : Command
private data object DisableCommand : Command