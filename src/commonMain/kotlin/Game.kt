import korlibs.image.atlas.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import tile.EmptyTile
import tile.Overflow
import tile.StraightPipe
import tile.Tile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface FieldEvent

class GameOver : FieldEvent

class PlayField(
    val rect: Rectangle,
    xtiles: Int,
    ytiles: Int,
    val sContainer: SContainer,
    val assets: Assets,
) {
    val tileWidth = 32f
    val tileHeight = 32f
    val tiles =
        (1..xtiles).map { _ ->
            (1..ytiles).map { _ ->
                EmptyTile()
            }.toMutableList<Tile>()
        }

    init {
        sContainer.image(assets.straightH) {
            position(rect.x, rect.y)
            size(rect.size)
        }
        tiles.forEachIndexed { x, line ->
            line.forEachIndexed { y, tile ->
                tile.bindView(getTileRect(x,y), assets, sContainer)
            }
        }
    }



    fun onRender(
        dt: Duration,
    ): FieldEvent? {
        var result: FieldEvent? = null
        tiles.forEachIndexed { x, slice ->
            slice.forEachIndexed { y, tile ->
                val event =
                    tile.onUpdate(
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
    ) = Rectangle(
        rect.x + x * tileWidth,
        rect.y + y * tileHeight,
        tileWidth,
        tileHeight,
    )

    fun onTouchUp(pos: Vector2I) {
        if (!rect.contains(pos)) {
            return
        }
        val x = ((pos.x - rect.x) / tileWidth).toInt()
        val y = ((pos.y - rect.y) / tileHeight).toInt()
        if (getTileRect(x, y).contains(pos)) {
            val currentTile = tiles[x][y]
            if (currentTile.isEditable()) {
                tiles[x][y] = StraightPipe(2.seconds, Orientation.entries.random())
            }
        }
    }

}

//override suspend fun Context.start() {
//    val batch = SpriteBatch(this)
//    val shapeRenderer = ShapeRenderer(batch)
//    val viewport = ExtendViewport(960, 540)
//    val camera = viewport.camera
//    val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
//    val assets = Assets(atlas)
//
//    val startDuration = 4.seconds
//    var startTimer = startDuration
//    var started = false
//
//    var gameOver = false
//
//    val field =
//        PlayField(
//            rect = Rect(-860 / 2f, -440 / 2f, 628f, 440f),
//            10,
//            7,
//        )
//
//    val processor =
//        input.inputProcessor {
//            onTouchUp { screenX, screenY, pointer ->
//                if (!gameOver) {
//                    val worldPos = viewport.camera.screenToWorld(context, Vec2f(screenX, screenY))
//                    field.onTouchUp(worldPos)
//                }
//            }
//        }
//
//    onResize { width, height ->
//        viewport.update(width, height, context)
//    }
//
//    onRender { dt ->
//        gl.clearColor(Color.DARK_GRAY)
//        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
//        startTimer = (startTimer - dt).coerceAtLeast(0.seconds)
//
//        batch.use(camera.viewProjection) {
//            val event =
//                field.onRender(
//                    assets,
//                    batch,
//                    dt,
//                )
//
//            val timerLeft = startTimer / startDuration
//            shapeRenderer.filledRectangle(
//                Rect(
//                    470f,
//                    -270f,
//                    40f,
//                    500f * timerLeft.toFloat(),
//                ),
//            )
//
//            when (event) {
//                is GameOver -> gameOver = true
//                null -> {}
//            }
//
//            if (!started && startTimer == 0.seconds) {
//                field.tiles.first().first().takeLiquid(Direction.RIGHT, dt)
//            }
//
//            if (gameOver) {
//                Fonts.default.draw(it, "Game Over!", -15f, 0f)
//            }
//        }
//    }
//}

data class Assets(val atlas: Atlas) {
    val empty = atlas["empty"]
    val corner = atlas["corner"]
    val straightH = atlas["straightH"]
    val straightV = atlas["straightV"]
    val cornerFluid = (1..8).map {
        atlas["corner-fluid$it"]
    }
    val straightFluid = (1..7).map {
        atlas["straight-fluid$it"]
    }
}

