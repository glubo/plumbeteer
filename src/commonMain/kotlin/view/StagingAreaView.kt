package view

import Assets
import StagingArea
import korlibs.korge.scene.SceneContainer
import korlibs.korge.view.View
import korlibs.korge.view.centered
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2
import korlibs.time.TimeSpan
import tile.CornerPipe
import tile.CrossPipe
import tile.EmptyTile
import tile.StartPipe
import tile.StraightPipe

fun SceneContainer.baseCornerPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: CornerPipe,
) = this.sprite(
    assets.corner,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
    rotation(tile.direction.angle())
}

fun SceneContainer.baseStraigthPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: StraightPipe,
) = this.sprite(
    assets.straightV,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
    rotation(tile.orientation.directions.first().angle())
}

fun SceneContainer.baseCrossPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: CrossPipe,
) = this.sprite(
    assets.cross,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
}

class StagingAreaView(
    val stagingArea: StagingArea,
    val assets: Assets,
    val sContainer: SceneContainer,
    val viewRectangle: Rectangle,
) {
    val views = mutableListOf<View>()
    val tileSize =
        Size(
            viewRectangle.width,
            viewRectangle.height / stagingArea.count,
        )

    fun update(dt: TimeSpan) {
        views.forEach { it.removeFromParent() }
        views.clear()

        stagingArea.fifo.forEachIndexed { y, tile ->
            val tilePos =
                Vector2(
                    viewRectangle.x,
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
                is EmptyTile -> null
                is CornerPipe -> sContainer.baseCornerPipe(assets, tileRect, tile)
                is CrossPipe -> sContainer.baseCrossPipe(assets, tileRect, tile)
                is StartPipe -> null
                is StraightPipe -> sContainer.baseStraigthPipe(assets, tileRect, tile)
            }?.let {
                views.add(it)
            }
        }
    }
}
