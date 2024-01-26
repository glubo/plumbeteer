import korlibs.image.atlas.readAtlas
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.RegisteredImageFormats
import korlibs.image.paint.Paint
import korlibs.image.paint.withPaint
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.mouse
import korlibs.korge.scene.Scene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.SContainer
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.toInt
import korlibs.math.geom.vector.StrokeInfo
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        RegisteredImageFormats.register(PNG)
        sceneContainer.changeTo({ MyScene() })
    }

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val atlas = resourcesVfs["texture1.json"].readAtlas()
        val startDuration = 4.seconds
        var startTimer = startDuration
        var started = false
        var gameOver = false

        val assets = Assets(atlas)
        val field =
            PlayField(
                Rectangle(0, 0, 400, 400),
                xtiles = 10,
                ytiles = 10,
                this,
                assets,
            )

        val staging =
            StagingField().also {
                it.bindView(
                    this,
                    assets,
                    Rectangle(450, 0, 40, 200),
                )
                it.replenish()
            }
        var gameOverView: View? = null
        val startTimerView = solidRect(
            30, 200,
            Colors.GREEN
        ) {
            position(410, 0)
        }

        addUpdater { dt ->
            startTimer = (startTimer - dt).coerceAtLeast(0.seconds)

            val timerLeft = startTimer / startDuration
            startTimerView.height = 200*timerLeft
            val event = field.onUpdate(dt)
            when (event) {
                is GameOver -> gameOver = true
                null -> {}
            }
            if (!started && startTimer == 0.seconds) {
                field.tiles.first().first().takeLiquid(Direction.RIGHT, dt)
            }

            if (gameOver && gameOverView == null) {
                gameOverView = text("GAME OVER", textSize = 68, color = Colors.BLACK, ) {
                    centerOnStage()
                }
                gameOverView = text("GAME OVER", textSize = 66, color = Colors.WHITE, ) {
                    centerOnStage()
                }
                gameOverView = text("GAME OVER", textSize = 64, color = Colors.RED, ) {
                    centerOnStage()
                }
            }
        }


        this.mouse {
            onClick {
                if (!gameOver) {
                    field.onTouchUp(it.currentPosLocal.toInt(), staging)
                }
            }
        }
    }
}
