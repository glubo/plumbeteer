package tile

import Assets
import Direction
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.math.Rect
import easyDraw
import kotlin.time.Duration

data object EmptyTile : Tile {
    override fun onRender(target: Rect, assets: Assets, batch: Batch, dt: Duration): TileEvent? {
        assets.empty.easyDraw(
            batch,
            target.x,
            target.y,
            target.width,
            target.height
        )

        return null
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false

    override fun isEditable() = true
}
