package tile

import Direction
import kotlin.time.Duration

class EmptyTile : Tile() {
    override fun onUpdate(dt: Duration) = null

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false

    override fun isEditable() = true
}
