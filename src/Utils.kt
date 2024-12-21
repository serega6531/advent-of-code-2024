
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.abs

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = readEntireInput(name).lines()

fun readEntireInput(name: String) = Path("src/$name.txt").readText().trim()

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

inline fun <T> Iterable<Iterable<T>>.forEachIndexed(action: (y: Int, x: Int, T) -> Unit) {
    this.forEachIndexed { y, line ->
        line.forEachIndexed { x, item ->
            action(y, x, item)
        }
    }
}

@JvmName("forEachIndexedString")
inline fun Iterable<String>.forEachIndexed(action: (y: Int, x: Int, Char) -> Unit) {
    this.forEachIndexed { y, line ->
        line.forEachIndexed { x, item ->
            action(y, x, item)
        }
    }
}

fun Iterable<String>.findCoordinate(target: Char): YX {
    this.forEachIndexed { y, x, ch ->
        if (ch == target) {
            return YX(y, x)
        }
    }

    throw IllegalArgumentException()
}

fun <T> Iterable<Iterable<T>>.findCoordinate(target: T): YX {
    this.forEachIndexed { y, x, ch ->
        if (ch == target) {
            return YX(y, x)
        }
    }

    throw IllegalArgumentException()
}

fun List<String>.inBounds(yx: YX): Boolean {
    val (y, x) = yx
    val maxY = this.lastIndex
    val maxX = this.first().lastIndex

    return y in 0..maxY && x in 0..maxX
}

operator fun List<String>.get(yx: YX): Char {
    return this[yx.y][yx.x]
}

fun <T: Any> List<T?>.asNotNull(): List<T> {
    this.forEach { check(it != null) }
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
}

data class YX(val y: Int, val x: Int) {

    fun distanceTo(other: YX): Int {
        return abs(x - other.x) + abs(y - other.y)
    }

    override fun toString(): String = "($y, $x)"

    operator fun plus(dir: DirectionOffset): YX {
        return YX(this.y + dir.dy, this.x + dir.dx)
    }
}

data class DirectionOffset(val dy: Int, val dx: Int)

val cardinals = listOf<DirectionOffset>(
    DirectionOffset(-1, 0),
    DirectionOffset(0, 1),
    DirectionOffset(1, 0),
    DirectionOffset(0, -1)
)