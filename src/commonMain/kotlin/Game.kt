import tile.CornerPipe
import tile.CrossPipe
import tile.EmptyTile
import tile.Overflow
import tile.StartPipe
import tile.StraightPipe
import tile.Tile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface FieldEvent

sealed interface FieldFinalEvent : FieldEvent

data class GameOver(
    val score: Long,
) : FieldFinalEvent

data class NextLevel(
    val score: Long,
) : FieldFinalEvent

data class Scored(
    val x: Int,
    val y: Int,
    val score: Long,
) : FieldEvent

class Placed(
    x: Int,
    y: Int,
    tile: Tile,
) : FieldEvent

class StagingArea {
    val fifo = mutableListOf<Tile>()
    val count = 5

    val generators =
        listOf(
            { StraightPipe(2.seconds, Orientation.VERTICAL) },
            { StraightPipe(2.seconds, Orientation.HORIZONTAL) },
            { CornerPipe(2.seconds, Direction.UP) },
            { CornerPipe(2.seconds, Direction.DOWN) },
            { CornerPipe(2.seconds, Direction.LEFT) },
            { CornerPipe(2.seconds, Direction.RIGHT) },
            { CrossPipe(2.seconds) },
        )

    fun retrieve(): Tile {
        val ret = fifo.removeFirst()
        println("$fifo $ret")
        replenish()
        println("$fifo")
        return ret
    }

    fun replenish() {
        (fifo.size..<count).forEach { _ ->
            fifo.add(
                generators.random()(),
            )
        }
    }
}

class PlayField(
    val xtiles: Int,
    val ytiles: Int,
    val stagingArea: StagingArea,
    val currentLevel: Level,
    var score: Long = 0L,
    private val eventCallback: (FieldEvent) -> Unit,
) {
    val tiles =
        (1..xtiles).map { _ ->
            (1..ytiles)
                .map { _ ->
                    EmptyTile()
                }.toMutableList<Tile>()
        }
    val startX = (1..<xtiles - 1).random()
    val startY = (1..<ytiles - 1).random()
    var distance = currentLevel.targetDistance

    init {
        tiles[startX][startY] = StartPipe(2.seconds, Direction.entries.random())
    }

    fun start() {
        (tiles[startX][startY] as StartPipe).start()
    }

    fun onUpdate(dt: Duration): FieldFinalEvent? {
        var result: FieldFinalEvent? = null
        tiles.forEachIndexed { x, slice ->
            slice.forEachIndexed { y, tile ->
                val event =
                    tile.onUpdate(
                        dt,
                    )

                when (event) {
                    is Overflow -> {
                        score += event.score
                        distance--
                        eventCallback(Scored(x, y, event.score))
                        val pos = event.direction.vec + Vec2i(x, y)
                        val newTile = getTileOrNull(pos)
                        if (newTile == null) {
                            result = overOrNextLevel()
                        } else {
                            if (!newTile.takeLiquid(event.direction, event.dt)) {
                                result = overOrNextLevel()
                            }
                        }
                    }

                    null -> {}
                }
            }
        }
        return result?.also {
            eventCallback(it)
        }
    }

    private fun overOrNextLevel() =
        when {
            distance <= 0 -> NextLevel(score)
            else -> GameOver(score)
        }

    private fun getTileOrNull(vec: Vec2i) =
        tiles
            .getOrNull(vec.x)
            ?.getOrNull(vec.y)

    fun onTouchUp(
        x: Int,
        y: Int,
    ) {
        val currentTile = tiles[x][y]
        if (currentTile.isEditable()) {
            val tile = stagingArea.retrieve()
            tiles[x][y] = tile
            eventCallback(Placed(x, y, tile))
        }
    }
}
