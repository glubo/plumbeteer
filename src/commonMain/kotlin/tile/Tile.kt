package tile

import Assets
import Direction
import korlibs.korge.view.SContainer
import korlibs.korge.view.View
import korlibs.math.geom.Rectangle
import kotlin.time.Duration

sealed class Tile() {
    protected var views = mutableListOf<View>()

    abstract fun bindView(
        target: Rectangle,
        assets: Assets,
        sContainer: SContainer,
    )

    fun release() {
        views.forEach { it.removeFromParent() }
        views.clear()
    }

    abstract fun onUpdate(dt: Duration): TileEvent?

    abstract fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ): Boolean

    abstract fun isEditable(): Boolean
}

sealed interface TileEvent

data class Overflow(
    val dt: Duration,
    val direction: Direction,
    val score: Long,
) : TileEvent {
    init {
        println(this)
    }
}
