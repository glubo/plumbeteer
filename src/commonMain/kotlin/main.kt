@file:Suppress("ktlint:standard:filename")

import korlibs.image.atlas.readAtlas
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.RegisteredImageFormats
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.mouse
import korlibs.korge.scene.Scene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiVerticalFill
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.visible
import korlibs.math.geom.Angle
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2
import korlibs.math.random.get
import view.Particles
import view.PlayFieldView
import view.StagingAreaView
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        injector.mapPrototype { GameScene(get()) }
        injector.mapPrototype { MainMenuScene() }
        injector.mapSingleton { AssetsLoader() }
        RegisteredImageFormats.register(PNG)
        sceneContainer.changeTo<MainMenuScene>()
    }

class MainMenuScene : Scene() {
    override suspend fun SContainer.sceneInit() {
    }

    override suspend fun SContainer.sceneMain() {
        uiVerticalStack(padding = 10.0) {
            text("Pipeteer", textSize = 64, color = Colors.RED) { }
            uiVerticalFill { }
            uiButton("New Game") {
                centerXOn(this@uiVerticalStack)
                mouse {
                    onClick {
                        sceneContainer.changeTo<GameScene>()
                    }
                }
            }
            val quit = uiButton("Quit") {
                centerXOn(this@uiVerticalStack)
            }
            val really = uiButton("Really Quit") {
                visible = false
                centerXOn(this@uiVerticalStack)
                mouse {
                    onClick {
                        views.gameWindow.close()
                    }
                }
            }
            quit.mouse {
                onClick {
                    really.visible(true)
                }
            }
            centerOnStage()
        }
    }
}

class AssetsLoader {
    var assets: Assets? = null

    suspend fun get(): Assets {
        return assets
            ?: suspend {
                val atlas =
                    resourcesVfs["texture3.json"].readAtlas()
                Assets(atlas)
            }()
    }
}

data class Level(
    val name: String,
    val targetDistance: Int,
    val timerS: Int,
)

val levels = listOf(
    Level(
        "1 First Steps",
        14,
        17,
    ),
    Level(
        "2 Faster and Longer",
        15,
        15,
    ),
    Level(
        "3 Fasterer and Longerer",
        16,
        13,
    ),
)

class GameScene(
    val assetsLoader: AssetsLoader,
) : Scene() {
    lateinit var assets: Assets

    override suspend fun SContainer.sceneInit() {
        assets = assetsLoader.get()
    }

    override suspend fun SContainer.sceneMain() {
        val startDuration = 4.seconds
        var startTimer = startDuration
        var started = false
        var gameOver = false

        val tileWidth = 40
        val tileHeight = 40
        val fieldCountX = 10
        val fieldCountY = 10

        val fieldLayer = sceneContainer()
        val particleLayer = sceneContainer()
        val topLayer = sceneContainer()

        val staging =
            StagingArea().also {
                it.replenish()
            }
        val viewRectangle =
            Rectangle(
                0,
                0,
                tileWidth * fieldCountX,
                tileHeight * fieldCountY,
            )
        val particles =
            Particles(
                assets,
                particleLayer,
                viewRectangle,
            )
        val field =
            PlayField(
                xtiles = fieldCountX,
                ytiles = fieldCountY,
                staging,
            ) {
                when (it) {
                    is Scored -> {
                        val position =
                            Vector2(
                                it.x * tileWidth + tileWidth * 0.5,
                                it.y * tileHeight + tileHeight * 0.5,
                            )
                        particles.addParticle(
                            Particles.Particle(
                                position,
                                Vector2(
                                    Random.get(-40.0, 40.0),
                                    Random.get(-30.0, 10.0),
                                ),
                                Vector2(
                                    0,
                                    98.0,
                                ),
                                Angle.ZERO,
                                Angle.ZERO,
                                particleLayer.text(it.score.toString()),
                                2.seconds,
                            ),
                        )
                        (1..50).forEach { _ ->

                            particles.addParticle(
                                Particles.Particle(
                                    position,
                                    Vector2(
                                        Random.get(-40.0, 40.0),
                                        Random.get(-30.0, 10.0),
                                    ),
                                    Vector2(
                                        0,
                                        98.0,
                                    ),
                                    Angle.ZERO,
                                    Angle.ZERO,
                                    particleLayer.solidRect(
                                        Size(1, 1),
                                    ),
                                    2.seconds,
                                ),
                            )
                        }
                    }

                    else -> {
                        println("unhandled $it")
                    }
                }
            }

        val stagingView =
            StagingAreaView(
                staging,
                assets,
                fieldLayer,
                Rectangle(450, 0, tileWidth, 5 * tileHeight),
            )
        val fieldView =
            PlayFieldView(
                field,
                assets,
                fieldLayer,
                viewRectangle,
                views,
                isActiveCallback = { !gameOver }
            )

        val scoreView =
            text("SCORE: 0", textSize = 24) {
                position(10, 410)
            }
        var gameOverView: View? = null
        val startTimerView =
            topLayer.solidRect(
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
            fieldView.update(dt)
            stagingView.update(dt)
            particles.update(dt)
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
                    topLayer.text("GAME OVER", textSize = 68, color = Colors.BLACK) {
                        centerOnStage()
                    }
                gameOverView =
                    topLayer.text("GAME OVER", textSize = 66, color = Colors.WHITE) {
                        centerOnStage()
                    }
                gameOverView =
                    topLayer.text("GAME OVER", textSize = 64, color = Colors.RED) {
                        centerOnStage()
                    }
            }
        }

        this.mouse {
            onClick {
                if (!gameOver) {
                    fieldView.onClick(it)
                } else {
                    sceneContainer.changeTo<MainMenuScene>()
                }
            }
        }
    }
}
