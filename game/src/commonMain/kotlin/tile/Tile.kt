package tile

import Direction
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.math.Rect
import kotlin.time.Duration

sealed interface Tile {
    fun onRender(
        target: Rect,
        shapeRenderer: ShapeRenderer,
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
