
import kotlin.io.path.Path
import kotlin.io.path.readText

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = readEntireInput(name).lines()

fun readEntireInput(name: String) = Path("src/$name.txt").readText().trim()

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

fun <T: Any> List<T?>.asNotNull(): List<T> {
    this.forEach { check(it != null) }
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
}

data class YX(val y: Int, val x: Int) {
    override fun toString(): String = "($y, $x)"
}