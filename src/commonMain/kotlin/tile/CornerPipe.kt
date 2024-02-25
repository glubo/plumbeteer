package tile

import Direction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CornerPipe(
    val length: Duration,
    val direction: Direction,
) : Tile() {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false

    private fun angle() = direction.angle()

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true
                val outputDirection = validDirections().first { it != liquidDirection }

                return Overflow(
                    elapsed - length,
                    outputDirection,
                    1000
                )
            }
        }
        return null
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = when {
        liquidDirection != null -> false
        direction.opposite() in validDirections() -> {
            liquidDirection = direction.opposite()
            true
        }

        else -> false
    }

    private fun validDirections() = listOf(direction, direction.nextClockwise())

    override fun isEditable() = liquidDirection == null
    override fun toString(): String {
        return "CornerPipe(direction=$direction, elapsed=$elapsed, liquidDirection=$liquidDirection)"
    }
}
