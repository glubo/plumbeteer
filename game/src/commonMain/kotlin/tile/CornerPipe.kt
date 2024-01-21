package tile

import Direction
import Orientation
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CornerPipe(
    val length: Duration,
    val rotation: Direction,
) : Tile {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false

    override fun onRender(
        target: Rect,
        shapeRenderer: ShapeRenderer,
        dt: Duration,
    ): TileEvent? {
        shapeRenderer.filledRectangle(
            target,
        )
        shapeRenderer.()
        when (orientation) {
            Orientation.HORIZONTAL -> {
                shapeRenderer.filledRectangle(
                    target.x,
                    target.y + target.height * 0.23333f,
                    target.width,
                    target.height * 0.533333f,
                    color = Color.GRAY.toFloatBits(),
                )
            }

            Orientation.VERTICAL ->
                shapeRenderer.filledRectangle(
                    target.x + target.width * 0.23333f,
                    target.y,
                    target.width * 0.533333f,
                    target.height,
                    color = Color.GRAY.toFloatBits(),
                )
        }

        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }
            when (liquidDirection) {
                Direction.UP ->
                    shapeRenderer.filledRectangle(
                        target.x + target.width * 0.33333f,
                        target.y + target.height,
                        target.width * 0.333333f,
                        -target.height * (elapsed / length).coerceAtMost(1.0).toFloat(),
                        color = Color.GREEN.toFloatBits(),
                    )

                Direction.DOWN ->
                    shapeRenderer.filledRectangle(
                        target.x + target.width * 0.33333f,
                        target.y,
                        target.width * 0.333333f,
                        target.height * (elapsed / length).coerceAtMost(1.0).toFloat(),
                        color = Color.GREEN.toFloatBits(),
                    )

                Direction.LEFT ->
                    shapeRenderer.filledRectangle(
                        target.x + target.width,
                        target.y + target.height * 0.33333f,
                        -target.width * (elapsed / length).coerceAtMost(1.0).toFloat(),
                        target.height * 0.333333f,
                        color = Color.GREEN.toFloatBits(),
                    )

                Direction.RIGHT ->
                    shapeRenderer.filledRectangle(
                        target.x,
                        target.y + target.height * 0.33333f,
                        target.width * (elapsed / length).coerceAtMost(1.0).toFloat(),
                        target.height * 0.333333f,
                        color = Color.GREEN.toFloatBits(),
                    )

                null -> TODO()
            }

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true
                return Overflow(
                    elapsed - length,
                    liquidDirection!!,
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
