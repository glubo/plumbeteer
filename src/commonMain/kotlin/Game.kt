import korlibs.image.atlas.Atlas
import korlibs.korge.view.SContainer
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.size
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Vector2I
import tile.EmptyTile
import tile.Overflow
import tile.StraightPipe
import tile.Tile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface FieldEvent

class GameOver : FieldEvent

class PlayField(
    val rect: Rectangle,
    xtiles: Int,
    ytiles: Int,
    val sContainer: SContainer,
    val assets: Assets,
) {
    val tileWidth = rect.width / xtiles
    val tileHeight = rect.height / ytiles
    val tiles =
        (1..xtiles).map { _ ->
            (1..ytiles).map { _ ->
                EmptyTile()
            }.toMutableList<Tile>()
        }

    init {
        sContainer.image(assets.straightH) {
            position(rect.x, rect.y)
            size(rect.size)
        }
        tiles.forEachIndexed { x, line ->
            line.forEachIndexed { y, tile ->
                tile.bindView(getTileRect(x, y), assets, sContainer)
            }
        }
    }

    fun onUpdate(dt: Duration): FieldEvent? {
        var result: FieldEvent? = null
        tiles.forEachIndexed { x, slice ->
            slice.forEachIndexed { y, tile ->
                val event =
                    tile.onUpdate(
                        dt,
                    )

                when (event) {
                    is Overflow -> {
                        val pos = event.direction.vec + Vec2i(x, y)
                        val newTile = getTileOrNull(pos)
                        if (newTile == null) {
                            result = GameOver()
                        } else {
                            if (!newTile.takeLiquid(event.direction, event.dt)) {
                                result = GameOver()
                            }
                        }
                    }

                    null -> {}
                }
            }
        }
        return result
    }

    private fun getTileOrNull(vec: Vec2i) =
        tiles.getOrNull(vec.x)
            ?.getOrNull(vec.y)

    private fun getTileRect(
        x: Int,
        y: Int,
    ) = Rectangle(
        rect.x + x * tileWidth,
        rect.y + y * tileHeight,
        tileWidth,
        tileHeight,
    )

    fun onTouchUp(pos: Vector2I) {
        if (!rect.contains(pos)) {
            return
        }
        val x = ((pos.x - rect.x) / tileWidth).toInt()
        val y = ((pos.y - rect.y) / tileHeight).toInt()
        if (getTileRect(x, y).contains(pos)) {
            val currentTile = tiles[x][y]
            if (currentTile.isEditable()) {
                currentTile.release()
                tiles[x][y] =
                    StraightPipe(2.seconds, Orientation.entries.random()).also {
                        it.bindView(getTileRect(x, y), assets, sContainer)
                    }
            }
        }
    }
}

data class Assets(val atlas: Atlas) {
    val transparent = atlas["transparent"]
    val empty = atlas["empty"]
    val corner = atlas["corner"]
    val straightH = atlas["straightH"]
    val straightV = atlas["straightV"]
    val cornerFluid =
        (1..8).map {
            atlas["corner-fluid$it"]
        }
    val straightFluid =
        SpriteAnimation(
            listOf(transparent) +
                (1..8).map {
                    atlas["straight-fluid$it"]
                },
        )
}
