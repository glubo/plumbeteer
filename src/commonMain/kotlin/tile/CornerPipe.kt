package tile

import Assets
import Direction
import korlibs.korge.view.SContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.centered
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.math.geom.Rectangle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CornerPipe(
    val length: Duration,
    val direction: Direction,
) : Tile() {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false
    lateinit var liquidView: Sprite
    lateinit var assets: Assets
    lateinit var sContainer: SContainer
    lateinit var target: Rectangle

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
                assets.corner,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
                rotation(angle())
            },
        )

        liquidView = if (direction == liquidDirection) {
            println("yes")

                sContainer.sprite(
                    assets.cornerFluidFlipped,
                ) {
                    position(target.centerX, target.centerY)
                    centered
                    size(target.size)
                    setFrame(0)
                    rotation(angle())
                }
        } else {
            println("no")

            sContainer.sprite(
                assets.cornerFluid,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
                setFrame(0)
                rotation(angle())
            }
        }
        views.add(
            liquidView,
        )

        this.assets = assets
        this.sContainer = sContainer
        this.target = target
    }

    private fun angle() = direction.angle()

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }

            val elapsedRatio = (elapsed / length).coerceAtMost(0.9999)
            val frame = ((liquidView.totalFrames - 1) * elapsedRatio).toInt()
            liquidView.setFrame(1 + frame)

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true
                val outputDirection = validDirections().first { it != liquidDirection }

                return Overflow(
                    elapsed - length,
                    outputDirection,
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
        direction.opposite() in validDirections() -> {
            println(validDirections())
            liquidDirection = direction.opposite()
            bindView(
                target, assets, sContainer
            )
            true
        }

        else -> false
    }

    private fun validDirections() = listOf(direction, direction.nextClockwise())

    override fun isEditable() = liquidDirection == null
    override fun toString(): String {
        return "CornerPipe(direction=$direction, elapsed=$elapsed, liquidDirection=$liquidDirection)"
    }
}
