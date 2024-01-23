package tile

import Assets
import Direction
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlin.time.Duration

sealed interface Tile {
    fun bindView(
        target: Rectangle,
        assets: Assets,
        sContainer: SContainer,
    )
    fun release()

    fun onUpdate(
        dt: Duration,
    ): TileEvent?

    fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ): Boolean

    fun isEditable(): Boolean
}

sealed interface TileEvent

data class Overflow(
    val dt: Duration,
    val direction: Direction,
) : TileEvent {
    init {
        println(this)
    }
}
