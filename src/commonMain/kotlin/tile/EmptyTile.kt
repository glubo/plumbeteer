package tile

import Assets
import Direction
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlin.time.Duration

class EmptyTile : Tile {
    private var view: View? = null
    override fun bindView(target: Rectangle, assets: Assets, sContainer: SContainer) {
        release()
        view = sContainer.image(assets.empty) {
            position(target.x, target.y)
            size(target.size)
        }
    }

    override fun release() {
        view?.removeFromParent()
        view = null
    }

    override fun onUpdate(dt: Duration) = null

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false

    override fun isEditable() = true
}
