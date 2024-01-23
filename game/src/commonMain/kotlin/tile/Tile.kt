package tile

import Assets
import Direction
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.math.Rect
import kotlin.time.Duration

sealed interface Tile {
    fun onRender(
        target: Rect,
        assets: Assets,
        batch: Batch,
        dt: Duration,
    ): TileEvent?

    fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ): Boolean

    fun isEditable(): Boolean
}

sealed interface TileEvent

data class Overflow(
    val dt: Duration,
    val direction: Direction,
) : TileEvent {
    init {
        println(this)
    }
}
