package tile

import Assets
import Direction
import Orientation
import Rotation
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StraightPipe(
    val length: Duration,
    val orientation: Orientation,
) : Tile {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false
    override fun bindView(target: Rectangle, assets: Assets, sContainer: SContainer) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun onUpdate(dt: Duration): TileEvent? {
//        assets.empty.easyDraw(
//            batch = batch,
//            x = target.x,
//            y = target.y,
//            width = target.width,
//            height = target.height
//        )
//
//        val slice = when (orientation) {
//            Orientation.VERTICAL -> assets.straightV
//            Orientation.HORIZONTAL -> assets.straightH
//        }
//
//        slice.easyDraw(
//            batch = batch,
//            x = target.x,
//            y = target.y,
//            width = target.width,
//            height = target.height,
//        )

        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }
//            when (liquidDirection) {
//                Direction.UP ->
//                    shapeRenderer.filledRectangle(
//                        target.x + target.width * 0.33333f,
//                        target.y + target.height,
//                        target.width * 0.333333f,
//                        -target.height * (elapsed / length).coerceAtMost(1.0).toFloat(),
//                        color = Color.GREEN.toFloatBits(),
//                    )
//
//                Direction.DOWN ->
//                    shapeRenderer.filledRectangle(
//                        target.x + target.width * 0.33333f,
//                        target.y,
//                        target.width * 0.333333f,
//                        target.height * (elapsed / length).coerceAtMost(1.0).toFloat(),
//                        color = Color.GREEN.toFloatBits(),
//                    )
//
//                Direction.LEFT ->
//                    shapeRenderer.filledRectangle(
//                        target.x + target.width,
//                        target.y + target.height * 0.33333f,
//                        -target.width * (elapsed / length).coerceAtMost(1.0).toFloat(),
//                        target.height * 0.333333f,
//                        color = Color.GREEN.toFloatBits(),
//                    )
//
//                Direction.RIGHT ->
//                    shapeRenderer.filledRectangle(
//                        target.x,
//                        target.y + target.height * 0.33333f,
//                        target.width * (elapsed / length).coerceAtMost(1.0).toFloat(),
//                        target.height * 0.333333f,
//                        color = Color.GREEN.toFloatBits(),
//                    )
//
//                null -> TODO()
//            }

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
