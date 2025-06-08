package view

import Assets
import PlayField
import korlibs.image.color.ColorTransform
import korlibs.image.color.Colors.RED
import korlibs.image.color.Colors.WHITE
import korlibs.korge.input.MouseEvents
import korlibs.korge.scene.SceneContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.View
import korlibs.korge.view.Views
import korlibs.korge.view.centered
import korlibs.korge.view.filter.ColorTransformFilter
import korlibs.korge.view.filter.addFilter
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.logger.Logger
import korlibs.math.geom.Angle
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2
import korlibs.math.interpolation.Ratio
import korlibs.time.fast
import korlibs.time.seconds
import tile.CornerPipe
import tile.CrossPipe
import tile.EmptyTile
import tile.StartPipe
import tile.StraightPipe
import kotlin.math.abs
import kotlin.math.sin
import kotlin.time.Duration

class PlayFieldView(
    val playField: PlayField,
    val assets: Assets,
    val sContainer: SceneContainer,
    val viewRectangle: Rectangle,
    val views: Views,
    val isActiveCallback: () -> Boolean,
) {
    var durationFromStart = 0.seconds.fast
    val logger = Logger(this::class.simpleName!!)
    val currentViews = mutableListOf<View>()
    val tileSize =
        Size(
            viewRectangle.width / playField.xtiles,
            viewRectangle.height / playField.ytiles,
        )

    fun update(dt: Duration) {
        durationFromStart += dt.fast
        currentViews.forEach { it.removeFromParent() }
        currentViews.clear()
        val currentMousePos = sContainer.localMousePos(views)

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
                val hover = tileRect.contains(currentMousePos) && isActiveCallback()

                currentViews.add(
                    sContainer.sprite(assets.empty) {
                        position(tilePos.x, tilePos.y)
                        size(tileSize)
                        if (hover) {
                            val ratio = 0.5 * abs(sin( durationFromStart.milliseconds *0.002))
                            logger.info { ratio }
                            addFilter(
                                ColorTransformFilter(
                                    ColorTransform(
                                        multiply = RED.interpolateWith(Ratio(ratio), WHITE)
                                    )
                                )
                            )
                        }
                    },
                )
                when (tile) {
                    is EmptyTile -> {}
                    is CornerPipe -> {
                        cornerPipe(tile, tileRect)
                    }

                    is CrossPipe -> {
                        crossPipe(tile, tileRect)
                    }

                    is StartPipe -> {
                        startPipe(tile, tileRect)
                    }

                    is StraightPipe -> {
                        straightPipe(tile, tileRect)
                    }
                }
            }
        }
    }

    private fun crossPipe(
        tile: CrossPipe,
        tileRect: Rectangle,
    ) {
        currentViews.add(
            sContainer.sprite(
                assets.cross,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileSize)
            },
        )

        tile.innerMap.forEach { innerEntry ->
            val innerTile = innerEntry.value
            val liquidView =
                sContainer.sprite(
                    assets.straightFluid,
                ) {
                    position(tileRect.centerX, tileRect.centerY)
                    centered
                    size(tileSize)
                    rotation(innerTile.liquidDirection?.angle() ?: Angle.ZERO)
                    updateLiquidFrame(
                        innerTile.liquidDirection != null,
                        innerTile.elapsed,
                        innerTile.length,
                    )
                }
            currentViews.add(
                liquidView,
            )
        }
    }

    private fun straightPipe(
        tile: StraightPipe,
        tileRect: Rectangle,
    ) {
        currentViews.add(
            sContainer.sprite(
                assets.straightV,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileSize)
                rotation(tile.orientation.directions.first().angle())
            },
        )

        val liquidView =
            sContainer.sprite(
                assets.straightFluid,
            ) {
                position(tileRect.centerX, tileRect.centerY)
                centered
                size(tileSize)
                rotation(tile.liquidDirection?.angle() ?: Angle.ZERO)
                updateLiquidFrame(
                    tile.liquidDirection != null,
                    tile.elapsed,
                    tile.length,
                )
            }
        currentViews.add(
            liquidView,
        )
    }

    private fun startPipe(
        tile: StartPipe,
        tileRect: Rectangle,
    ) {
        currentViews.add(
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
        currentViews.add(
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
        currentViews.add(
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
        currentViews.add(
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
            logger.debug { "ignoring click outside view.PlayFieldView at $clickPos" }
        }
    }
}
