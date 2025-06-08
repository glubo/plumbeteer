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
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.text
import korlibs.korge.view.visible
import korlibs.math.geom.Size
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        injector.mapPrototype { GameScene(get(), get()) }
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
                        sceneContainer.changeTo<GameScene>(
                            GameScene.GameSceneState(
                                score = 0,
                                currentLevel = levels.first(),
                                remainingLevels = levels.drop(1),
                            ),
                        )
                    }
                }
            }
            val quit =
                uiButton("Quit") {
                    centerXOn(this@uiVerticalStack)
                }
            val really =
                uiButton("Really Quit") {
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

    suspend fun get(): Assets =
        assets
            ?: suspend {
                val atlas =
                    resourcesVfs["texture4.json"].readAtlas()
                Assets(atlas)
            }()
}

data class Level(
    val name: String,
    val targetDistance: Int,
    val timerS: Duration,
)

val levels =
    listOf(
        Level(
            "1 First Steps",
            14,
            17.seconds,
        ),
        Level(
            "2 Faster and Longer",
            15,
            15.seconds,
        ),
        Level(
            "3 Fasterer and Longerer",
            16,
            13.seconds,
        ),
    )
