package tile

import Direction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StartPipe(
    val length: Duration,
    val direction: Direction,
) : Tile() {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                println(this)
                elapsed += dt
            }

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true

                return Overflow(
                    elapsed - length,
                    direction,
                    0L
                )
            }
        }
        return null
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false


    override fun isEditable() = false
    override fun toString(): String {
        return "StartPipe(direction=$direction, elapsed=$elapsed, liquidDirection=$liquidDirection)"
    }

    fun start() {
        liquidDirection = direction
    }
}
