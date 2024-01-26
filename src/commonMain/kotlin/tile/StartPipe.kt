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

class StartPipe(
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
                assets.start,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
                rotation(angle())
            },
        )


        liquidView = sContainer.sprite(
            assets.startFluid,
        ) {
            position(target.centerX, target.centerY)
            centered
            size(target.size)
            setFrame(0)
            rotation(angle())
        }
        views.add(
            liquidView,
        )

        this.assets = assets
        this.sContainer = sContainer
        this.target = target
    }

    private fun angle() = direction.nextClockwise().angle()

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                println(this)
                elapsed += dt
            }

            val elapsedRatio = (elapsed / length).coerceAtMost(0.9999)
            val frame = ((liquidView.totalFrames - 1) * elapsedRatio).toInt()
            liquidView.setFrame(1 + frame)

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true

                return Overflow(
                    elapsed - length,
                    direction,
                )
            }
        }
        return null
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = false


    override fun isEditable() = false
    override fun toString(): String {
        return "StartPipe(direction=$direction, elapsed=$elapsed, liquidDirection=$liquidDirection)"
    }

    fun start() {
        liquidDirection = direction
    }
}
