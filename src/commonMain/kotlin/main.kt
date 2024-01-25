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
import korlibs.korge.view.addUpdater
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.toInt
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        RegisteredImageFormats.register(PNG)
        sceneContainer.changeTo({ MyScene() })
    }

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val atlas = resourcesVfs["texture.json"].readAtlas()
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

        addUpdater { dt ->
            startTimer = (startTimer - dt).coerceAtLeast(0.seconds)

            val timerLeft = startTimer / startDuration
            val event = field.onUpdate(dt)
            when (event) {
                is GameOver -> gameOver = true
                null -> {}
            }
            if (!started && startTimer == 0.seconds) {
                field.tiles.first().first().takeLiquid(Direction.RIGHT, dt)
            }
        }
//
//    onRender { dt ->
//
//
//
//
//
//            if (gameOver) {
//                Fonts.default.draw(it, "Game Over!", -15f, 0f)
//            }
//        }
//    }
// }

        this.mouse {
            onClick {
                field.onTouchUp(it.currentPosLocal.toInt(), staging)
            }
        }
    }
}
