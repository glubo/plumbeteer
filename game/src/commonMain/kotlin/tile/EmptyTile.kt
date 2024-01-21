package tile

import Direction
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect
import kotlin.time.Duration

data object EmptyTile : Tile {
    override fun onRender(
        target: Rect,
        shapeRenderer: ShapeRenderer,
        dt: Duration,
    ): TileEvent? {
        shapeRenderer.filledRectangle(
            target,
            color = Color.WHITE.toFloatBits(),
        )

        return null
    }

    override fun takeLiquid(direction: Direction, dt: Duration) = false
    override fun isEditable() = true
}