package tile

import Assets
import Direction
import Orientation
import Orientation.HORIZONTAL
import Orientation.VERTICAL
import korlibs.korge.view.SContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.centered
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.math.geom.Angle
import korlibs.math.geom.Rectangle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CrossPipe(
    val length: Duration,
) : Tile() {
    class Inner(
        val length: Duration,
        val orientation: Orientation,
    ) {
        var elapsed = 0.seconds
        var liquidDirection: Direction? = null
        var filled = false
        lateinit var liquidView: Sprite
        fun angle() =
            liquidDirection?.angle() ?: Angle.ZERO

        fun onUpdate(dt: Duration): TileEvent? {
            if (liquidDirection != null) {
                if (!filled) {
                    elapsed += dt
                }

                val elapsedRatio = (elapsed / length).coerceAtMost(0.9999)
                val frame = ((liquidView.totalFrames - 1) * elapsedRatio).toInt()
                liquidView.setFrame(1 + frame)

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

        fun takeLiquid(
            direction: Direction,
            dt: Duration,
        ) = when {
            liquidDirection != null -> false
            direction in orientation.directions -> {
                liquidDirection = direction
                liquidView.rotation(
                    direction.angle()
                )
                true
            }

            else -> false
        }
    }

    val innerMap = mapOf(
        VERTICAL to Inner(length, VERTICAL),
        HORIZONTAL to Inner(length, HORIZONTAL)
    )

    override fun bindView(
        target: Rectangle,
        assets: Assets,
        sContainer: SContainer,
    ) {
        release()
        views.add(
            sContainer.image(
                assets.empty,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
            },
        )
        views.add(
            sContainer.image(
                assets.cross,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
            },
        )
        innerMap.forEach {
            it.value.liquidView =
                sContainer.sprite(
                    assets.straightFluid,
                ) {
                    position(target.centerX, target.centerY)
                    centered
                    size(target.size)
                    setFrame(0)
                    rotation(it.value.angle())
                }
            views.add(
                it.value.liquidView,
            )
        }
    }

    override fun onUpdate(dt: Duration): TileEvent? {
        var ret: TileEvent? = null
        innerMap.forEach {
            ret = it.value.onUpdate(dt) ?: ret
        }
        return ret
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = innerMap.any {
        it.value.takeLiquid(direction, dt)
    }

    override fun isEditable() = innerMap.values.none { it.liquidDirection != null }
}
