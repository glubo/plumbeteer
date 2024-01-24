package tile

import Assets
import Direction
import korlibs.korge.view.SContainer
import korlibs.korge.view.position
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.math.geom.Rectangle
import kotlin.time.Duration

class EmptyTile : Tile() {
    override fun bindView(
        target: Rectangle,
        assets: Assets,
        sContainer: SContainer,
    ) {
        release()
        views.add(
            sContainer.sprite(assets.empty) {
                position(target.x, target.y)
                size(target.size)
            },
        )
    }

    override fun onUpdate(dt: Duration) = null

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false

    override fun isEditable() = true
}
