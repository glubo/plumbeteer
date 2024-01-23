import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.file.vfs.readAtlas
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.node.resource.toDrawable
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.Fonts
import com.lehaine.littlekt.graphics.g2d.*
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.math.Vec2i
import com.lehaine.littlekt.util.viewport.ExtendViewport
import tile.EmptyTile
import tile.Overflow
import tile.StraightPipe
import tile.Tile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Game(context: Context) : ContextListener(context) {
    sealed interface FieldEvent

    class GameOver : FieldEvent

    class PlayField(
        val rect: Rect,
        xtiles: Int,
        ytiles: Int,
    ) {
        val tileWidth = 32f
        val tileHeight = 32f

        val tiles =
            (1..xtiles).map { _ ->
                (1..ytiles).map { _ ->
                    EmptyTile
                }.toMutableList<Tile>()
            }

        fun onRender(
            assets: Assets,
            batch: Batch,
            dt: Duration,
        ): FieldEvent? {
            var result: FieldEvent? = null
            tiles.forEachIndexed { x, slice ->
                slice.forEachIndexed { y, tile ->
                    val tileRect = getTileRect(x, y)

                    val event =
                        tile.onRender(
                            tileRect,
                            assets,
                            batch,
                            dt,
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

        private fun getTileOrNull(vec: Vec2i) =
            tiles.getOrNull(vec.x)
                ?.getOrNull(vec.y)

        private fun getTileRect(
            x: Int,
            y: Int,
        ) = Rect(
            rect.x + x * tileWidth,
            rect.y + y * tileHeight,
            tileWidth,
            tileHeight,
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
                    tiles[x][y] = StraightPipe(2.seconds, Orientation.entries.random())
                }
            }
        }
    }

    override suspend fun Context.start() {
        val batch = SpriteBatch(this)
        val shapeRenderer = ShapeRenderer(batch)
        val viewport = ExtendViewport(960, 540)
        val camera = viewport.camera
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val assets = Assets(atlas)

        val startDuration = 4.seconds
        var startTimer = startDuration
        var started = false

        var gameOver = false

        val field =
            PlayField(
                rect = Rect(-860 / 2f, -440 / 2f, 628f, 440f),
                10,
                7,
            )

        val processor =
            input.inputProcessor {
                onTouchUp { screenX, screenY, pointer ->
                    if (!gameOver) {
                        val worldPos = viewport.camera.screenToWorld(context, Vec2f(screenX, screenY))
                        field.onTouchUp(worldPos)
                    }
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
                val event =
                    field.onRender(
                        assets,
                        batch,
                        dt,
                    )

                val timerLeft = startTimer / startDuration
                shapeRenderer.filledRectangle(
                    Rect(
                        470f,
                        -270f,
                        40f,
                        500f * timerLeft.toFloat(),
                    ),
                )

                when (event) {
                    is GameOver -> gameOver = true
                    null -> {}
                }

                if (!started && startTimer == 0.seconds) {
                    field.tiles.first().first().takeLiquid(Direction.RIGHT, dt)
                }

                if (gameOver) {
                    Fonts.default.draw(it, "Game Over!", -15f, 0f)
                }
            }
        }
    }
}

data class Assets(val atlas: TextureAtlas) {
    val empty = atlas.getByPrefix("empty").slice
    val corner = atlas.getByPrefix("corner").slice
    val straightH = atlas.getByPrefix("straightH").slice
    val straightV = atlas.getByPrefix("straightV").slice
    val cornerFluid = (1..8).map {
        atlas.getByPrefix("corner-fluid$it").slice
    }
    val straightFluid = (1..7).map {
        atlas.getByPrefix("straight-fluid$it").slice
    }
}
fun TextureSlice.easyDraw(
    batch: Batch,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
//    this.toDrawable().draw(
//        batch,
//        x, y,
////        width, height
//    )
    batch.draw(
        slice = this,
        x = x,
        y = y,

    )
}
