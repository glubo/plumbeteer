import korlibs.korge.input.MouseEvents
import korlibs.korge.scene.SceneContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.View
import korlibs.korge.view.centered
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.logger.Logger
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2
import korlibs.time.TimeSpan
import tile.CornerPipe
import tile.CrossPipe
import tile.EmptyTile
import tile.StartPipe
import tile.StraightPipe
import kotlin.time.Duration

class PlayFieldView(
    val playField: PlayField,
    val assets: Assets,
    val sContainer: SceneContainer,
    val viewRectangle: Rectangle,
) {
    val logger = Logger(this::class.simpleName!!)
    val views = mutableListOf<View>()
    val tileSize =
        Size(
            viewRectangle.width / playField.xtiles,
            viewRectangle.height / playField.ytiles,
        )

    fun update(dt: TimeSpan) {
        views.forEach { it.removeFromParent() }
        views.clear()

        playField.tiles.forEachIndexed { x, row ->
            row.forEachIndexed { y, tile ->
                val tilePos =
                    Vector2(
                        viewRectangle.x + x * tileSize.width,
                        viewRectangle.y + y * tileSize.height,
                    )
                val tileRect =
                    Rectangle(
                        tilePos.x,
                        tilePos.y,
                        tileSize.width,
                        tileSize.height,
                    )

                views.add(
                    sContainer.sprite(assets.empty) {
                        position(tilePos.x, tilePos.y)
                        size(tileSize)
                    },
                )
                when (tile) {
                    is EmptyTile -> {}
                    is CornerPipe -> {
                        cornerPipe(tile, tileRect)
                    }

                    is CrossPipe -> {

                    }
                    is StartPipe -> {
                        startPipe(tile, tileRect)
                    }

                    is StraightPipe -> {

                    }
                }
            }
        }
    }

    private fun startPipe(
        tile: StartPipe,
        tileRect: Rectangle,
    ) {
        views.add(
            sContainer.sprite(
                assets.start,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileRect.size)
                rotation(tile.direction.angle())
            },
        )

        val liquidView =
            sContainer.sprite(
                assets.startFluid,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileRect.size)
                rotation(tile.direction.angle())
                updateLiquidFrame(
                    tile.liquidDirection != null,
                    tile.elapsed,
                    tile.length,
                )
            }
        views.add(
            liquidView,
        )
    }

    private fun Sprite.updateLiquidFrame(
        liquid: Boolean,
        elapsed: Duration,
        length: Duration,
    ) {
        if (liquid) {
            val elapsedRatio = (elapsed / length).coerceAtMost(0.9999)
            val frame = ((this.totalFrames - 1) * elapsedRatio).toInt()
            this.setFrame(1 + frame)
        } else {
            this.setFrame(0)
        }
    }

    private fun cornerPipe(
        tile: CornerPipe,
        tileRect: Rectangle,
    ) {
        views.add(
            sContainer.sprite(
                assets.corner,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileSize)
                rotation(tile.direction.angle())
            },
        )

        val liquidView =
            if (tile.direction == tile.liquidDirection) {
                sContainer.sprite(
                    assets.cornerFluidFlipped,
                ) {
                    position(tileRect.centerX, tileRect.centerY)
                    centered
                    size(tileSize)
                    rotation(tile.direction.angle())
                    updateLiquidFrame(
                        tile.liquidDirection != null,
                        tile.elapsed,
                        tile.length,
                    )
                }
            } else {
                sContainer.sprite(
                    assets.cornerFluid,
                ) {
                    position(tileRect.centerX, tileRect.centerY)
                    centered
                    size(tileSize)
                    rotation(tile.direction.angle())
                    updateLiquidFrame(
                        tile.liquidDirection != null,
                        tile.elapsed,
                        tile.length,
                    )
                }
            }
        views.add(
            liquidView,
        )
    }

    fun onClick(it: MouseEvents) {
        val clickPos = it.downPosLocal
        if (viewRectangle.contains(clickPos)) {
            val indexX = (clickPos.x - viewRectangle.x) / tileSize.width
            val indexY = (clickPos.y - viewRectangle.y) / tileSize.height
            playField.onTouchUp(indexX.toInt(), indexY.toInt())
        } else {
            logger.debug { "ignoring click outside PlayFieldView at $clickPos" }
        }
    }
}
