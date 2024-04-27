package tile

import Direction
import Orientation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StraightPipe(
    val length: Duration,
    val orientation: Orientation,
) : Tile() {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true
                return Overflow(
                    elapsed - length,
                    liquidDirection!!,
                    1000L,
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
        direction in orientation.directions -> {
            liquidDirection = direction
            true
        }

        else -> false
    }

    override fun isEditable() = liquidDirection == null
}
