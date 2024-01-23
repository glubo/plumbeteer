import Direction.DOWN
import Direction.LEFT
import Direction.RIGHT
import Direction.UP
import com.lehaine.littlekt.math.Vec2i
import com.lehaine.littlekt.math.geom.Angle

enum class Direction(
    val vec: Vec2i,
    val char: Char,
) {
    UP(
        Vec2i(0, -1),
        '↑',
    ),
    DOWN(
        Vec2i(0, 1),
        '↓',
    ),
    LEFT(
        Vec2i(-1, 0),
        '←',
    ),
    RIGHT(
        Vec2i(1, 0),
        '→',
    ),
    ;

    fun opposite() =
        when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

enum class Rotation(
    val angle: Angle
) {
    D0(Angle.ZERO),
    D90(Angle.fromDegrees(90)),
    D180(Angle.fromDegrees(180)),
    D270(Angle.fromDegrees(270)),
}

enum class Orientation(
    val directions: List<Direction>,
) {
    HORIZONTAL(listOf(LEFT, RIGHT)),

    VERTICAL(listOf(UP, DOWN)),
    ;

    fun switch() =
        when (this) {
            HORIZONTAL -> VERTICAL
            VERTICAL -> HORIZONTAL
        }

    companion object {
        fun of(direction: Direction) =
            when (direction) {
                UP -> VERTICAL
                DOWN -> VERTICAL
                LEFT -> HORIZONTAL
                RIGHT -> HORIZONTAL
            }
    }
}

operator fun Vec2i.plus(vec2i: Vec2i) = Vec2i(this.x + vec2i.x, this.y + vec2i.y)
