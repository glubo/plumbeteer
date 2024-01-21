import Direction.RIGHT
import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.Fonts
import com.lehaine.littlekt.graphics.g2d.*
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.math.Vec2i
import com.lehaine.littlekt.util.viewport.ExtendViewport
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Game(context: Context) : ContextListener(context) {
    sealed interface TileEvent

    data class Overflow(
        val dt: Duration,
        val direction: Direction,
    ) : TileEvent

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

    data object EmptyTile : Tile {
        override fun onRender(
            target: Rect,
            shapeRenderer: ShapeRenderer,
            dt: Duration,
        ): TileEvent? {
            shapeRenderer.filledRectangle(
                target,
                color = Color.GRAY.toFloatBits(),
            )

            return null
        }

        override fun takeLiquid(direction: Direction, dt: Duration) = false
        override fun isEditable() = true
    }

    class StraightPipe(
        val length: Duration,
    ) : Tile {
        var elapsed = 0.seconds
        var liquid = false
        var filled = false

        override fun onRender(
            target: Rect,
            shapeRenderer: ShapeRenderer,
            dt: Duration,
        ): TileEvent? {
            shapeRenderer.filledRectangle(
                target,
            )

            if (liquid) {
                if (!filled) {
                    elapsed += dt
                }
                shapeRenderer.filledRectangle(
                    target.x,
                    target.y,
                    target.width * (elapsed / length).coerceAtMost(1.0).toFloat(),
                    target.height,
                    color = Color.GREEN.toFloatBits(),
                )

                if (!filled && liquid && elapsed > length) {
                    filled = true
                    return Overflow(
                        elapsed - length,
                        RIGHT,
                    )
                }
            }
            return null
        }

        override fun takeLiquid(direction: Direction, dt: Duration) = when {
            liquid -> false
            else -> {
                liquid = true
                true
            }
        }

        override fun isEditable() = !liquid
    }

    sealed interface FieldEvent

    class GameOver : FieldEvent

    class PlayField(
        val rect: Rect,
        xtiles: Int,
        ytiles: Int,
    ) {
        val tileWidth = rect.width / xtiles
        val tileHeight = rect.height / ytiles

        val tiles = (1..xtiles).map { _ ->
            (1..ytiles).map { _ ->
                EmptyTile
            }.toMutableList<Tile>()
        }

        fun onRender(
            shapeRenderer: ShapeRenderer,
            dt: Duration,
        ): FieldEvent? {
            var result: FieldEvent? = null
            tiles.forEachIndexed { x, slice ->
                slice.forEachIndexed { y, tile ->
                    val tileRect = getTileRect(x, y)

                    val event = tile.onRender(
                        tileRect,
                        shapeRenderer,
                        dt
                    )

                    when (event) {
                        is Overflow -> {
                            val pos = event.direction.vec + Vec2i(x, y)
                            val newTile = getTileOrNull(pos)
                            if (newTile == null) {
                                result = GameOver()
                            } else {
                                if (!newTile.takeLiquid(event.direction, event.dt)) {
                                    result = GameOver()
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }
            return result
        }

        private fun getTileOrNull(vec: Vec2i) = tiles.getOrNull(vec.x)
            ?.getOrNull(vec.y)

        private fun getTileRect(x: Int, y: Int) = Rect(
            rect.x + x * tileWidth,
            rect.y + y * tileHeight,
            tileWidth * 0.95f,
            tileHeight * 0.95f
        )

        fun onTouchUp(pos: Vec2f) {
            if (!rect.intersects(pos.x, pos.y, pos.x, pos.y)) {
                return
            }
            val x = ((pos.x - rect.x) / tileWidth).toInt()
            val y = ((pos.y - rect.y) / tileHeight).toInt()
            if (getTileRect(x, y).intersects(pos.x, pos.y, pos.x, pos.y)) {
                val currentTile = tiles[x][y]
                if (currentTile.isEditable()) {
                    tiles[x][y] = StraightPipe(2.seconds)
                }
            }
        }
    }

    override suspend fun Context.start() {
        val batch = SpriteBatch(this)
        val shapeRenderer = ShapeRenderer(batch)
        val viewport = ExtendViewport(960, 540)
        val camera = viewport.camera

        val startDuration = 10.seconds
        var startTimer = startDuration
        var started = false

        var gameOver = false

        val field = PlayField(
            rect = Rect(-860 / 2f, -440 / 2f, 860f, 440f),
            4,
            2
        )

        val processor =
            input.inputProcessor {
                onTouchUp { screenX, screenY, pointer ->
                    val worldPos = viewport.camera.screenToWorld(context, Vec2f(screenX, screenY))
                    field.onTouchUp(worldPos)
                }
            }

        onResize { width, height ->
            viewport.update(width, height, context)
        }

        onRender { dt ->
            gl.clearColor(Color.DARK_GRAY)
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
            startTimer = (startTimer - dt).coerceAtLeast(0.seconds)

            batch.use(camera.viewProjection) {
                val event = field.onRender(
                    shapeRenderer, dt
                )

                val timerLeft = startTimer / startDuration
                shapeRenderer.filledRectangle(
                    Rect(
                        470f,
                        -270f,
                        40f,
                        500f * timerLeft.toFloat(),
                    )

                )

                when (event) {
                    is GameOver -> gameOver = true
                    null -> {}
                }

                if (!started && startTimer == 0.seconds) {
                    field.tiles.first().first().takeLiquid(RIGHT, dt)
                }

                if (gameOver) {
                    Fonts.default.draw(it, "Game Over!", -15f, 0f)
                }
            }
        }
    }
}


