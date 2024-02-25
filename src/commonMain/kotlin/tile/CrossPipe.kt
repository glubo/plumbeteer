package tile

import Direction
import Orientation
import Orientation.HORIZONTAL
import Orientation.VERTICAL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CrossPipe(
    length: Duration,
) : Tile() {
    class Inner(
        val length: Duration,
        val orientation: Orientation,
    ) {
        var elapsed = 0.seconds
        var liquidDirection: Direction? = null
        var filled = false

        fun onUpdate(dt: Duration): TileEvent? {
            if (liquidDirection != null) {
                if (!filled) {
                    elapsed += dt
                }

                if (!filled && liquidDirection != null && elapsed > length) {
                    filled = true
                    return Overflow(
                        elapsed - length,
                        liquidDirection!!,
                        1000,
                    )
                }
            }
            return null
        }

        fun takeLiquid(
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
    }

    val innerMap = mapOf(
        VERTICAL to Inner(length, VERTICAL),
        HORIZONTAL to Inner(length, HORIZONTAL)
    )

    override fun onUpdate(dt: Duration): TileEvent? {
        var ret: TileEvent? = null
        innerMap.forEach {
            ret = it.value.onUpdate(dt) ?: ret
        }
        return when {
            ret is Overflow && innerMap.all { it.value.filled } -> {
                Overflow(
                    (ret as Overflow).dt,
                    (ret as Overflow).direction,
                    5000L
                )
            }

            else -> ret
        }
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = innerMap.any {
        it.value.takeLiquid(direction, dt)
    }

    override fun isEditable() = innerMap.values.none { it.liquidDirection != null }
}
