package tile

import Direction
import kotlin.time.Duration

sealed class Tile {
    abstract fun onUpdate(dt: Duration): TileEvent?

    abstract fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ): Boolean

    abstract fun isEditable(): Boolean
}

sealed interface TileEvent

data class Overflow(
    val dt: Duration,
    val direction: Direction,
    val score: Long,
) : TileEvent {
    init {
        println(this)
    }
}
