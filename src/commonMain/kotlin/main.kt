import korlibs.image.atlas.readAtlas
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.RegisteredImageFormats
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
import korlibs.korge.view.scale
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.Size
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        RegisteredImageFormats.register(PNG)
        sceneContainer.changeTo({ MyScene() })
    }

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val atlas = resourcesVfs["texture2.json"].readAtlas()
        val startDuration = 4.seconds
        var startTimer = startDuration
        var started = false
        var gameOver = false
        val aaa =
            sceneContainer {
                this.x = 110.0
                this.y = 10.0
                this.size = Size(50, 50)
            }.scale(2.0, 1.0)

        aaa.solidRect(50, 50, Colors.RED)

        val assets = Assets(atlas)
        val field =
            PlayField(
                xtiles = 10,
                ytiles = 10,
                { println("unhandled $it") },
            )

        val staging =
            StagingField().also {
                it.replenish()
            }
        val scoreView =
            text("SCORE: 0", textSize = 24) {
                position(10, 410)
            }
        var gameOverView: View? = null
        val startTimerView =
            solidRect(
                30,
                200,
                Colors.GREEN,
            ) {
                position(410, 0)
            }

        addUpdater { dt ->
            val event = field.onUpdate(dt)
            when (event) {
                is GameOver -> gameOver = true
                null -> {}
            }
            scoreView.text = "SCORE: ${field.score}"
            if (!started && startTimer == 0.seconds) {
                started = true
                field.start()
            } else {
                startTimer = (startTimer - dt).coerceAtLeast(0.seconds)
                val timerLeft = startTimer / startDuration
                startTimerView.height = 200 * timerLeft
            }

            if (gameOver && gameOverView == null) {
                gameOverView =
                    text("GAME OVER", textSize = 68, color = Colors.BLACK) {
                        centerOnStage()
                    }
                gameOverView =
                    text("GAME OVER", textSize = 66, color = Colors.WHITE) {
                        centerOnStage()
                    }
                gameOverView =
                    text("GAME OVER", textSize = 64, color = Colors.RED) {
                        centerOnStage()
                    }
            }
        }

        this.mouse {
            onClick {
                if (!gameOver) {
                    TODO("translate to x, y")
                    field.onTouchUp(0, 0)
                }
            }
        }
    }
}
