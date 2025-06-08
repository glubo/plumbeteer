import GameScene.GameState.GAME_OVER
import GameScene.GameState.GAME_WON
import GameScene.GameState.NEXT_LEVEL
import GameScene.GameState.PLAY
import korlibs.image.color.Colors
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

class GameScene(
    val assetsLoader: AssetsLoader,
    val state: GameSceneState,
) : Scene() {
    lateinit var assets: Assets

    data class GameSceneState(
        val score: Long,
        val currentLevel: Level,
        val remainingLevels: List<Level>,
    )

    override suspend fun SContainer.sceneInit() {
        assets = assetsLoader.get()
    }

    enum class GameState {
        PLAY,
        GAME_OVER,
        NEXT_LEVEL,
        GAME_WON,
    }

    override suspend fun SContainer.sceneMain() {
        val startDuration = state.currentLevel.timerS
        var startTimer = startDuration
        var started = false
        var gameState = PLAY

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
            Rectangle.Companion(
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
                currentLevel = state.currentLevel,
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
                                    Random.Default.get(-40.0, 40.0),
                                    Random.Default.get(-30.0, 10.0),
                                ),
                                Vector2(
                                    0,
                                    98.0,
                                ),
                                Angle.Companion.ZERO,
                                Angle.Companion.ZERO,
                                particleLayer.text(it.score.toString()),
                                2.seconds,
                            ),
                        )
                        (1..50).forEach { _ ->

                            particles.addParticle(
                                Particles.Particle(
                                    position,
                                    Vector2(
                                        Random.Default.get(-40.0, 40.0),
                                        Random.Default.get(-30.0, 10.0),
                                    ),
                                    Vector2(
                                        0,
                                        98.0,
                                    ),
                                    Angle.Companion.ZERO,
                                    Angle.Companion.ZERO,
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
                Rectangle.Companion(450, 0, tileWidth, 5 * tileHeight),
            )
        val fieldView =
            PlayFieldView(
                field,
                assets,
                fieldLayer,
                viewRectangle,
                views,
                isActiveCallback = { gameState == PLAY },
            )

        val scoreView =
            text("SCORE: 0", textSize = 24) {
                position(10, 410)
            }
        val distanceView =
            text("DISTANCE: 0", textSize = 24) {
                position(210, 410)
            }
        val levelView =
            text("LEVEL: ${state.currentLevel.name}", textSize = 24) {
                position(10, 430)
            }
        var gameFinalizedView: View? = null
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
                is GameOver -> gameState = GAME_OVER
                is NextLevel -> gameState = if (state.remainingLevels.isEmpty()) GAME_WON else NEXT_LEVEL
                null -> {}
            }
            fieldView.update(dt)
            stagingView.update(dt)
            particles.update(dt)
            scoreView.text = "SCORE: ${field.score}"
            distanceView.text = "DISTANCE: ${field.distance}"
            if (!started && startTimer == 0.seconds) {
                started = true
                field.start()
            } else {
                startTimer = (startTimer - dt).coerceAtLeast(0.seconds)
                val timerLeft = startTimer / startDuration
                startTimerView.height = 200 * timerLeft
            }

            if (gameFinalizedView == null) {
                when (gameState) {
                    GAME_OVER -> {
                        gameFinalizedView =
                            topLayer.text("GAME OVER", textSize = 68, color = Colors.BLACK) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("GAME OVER", textSize = 66, color = Colors.WHITE) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("GAME OVER", textSize = 64, color = Colors.RED) {
                                centerOnStage()
                            }
                    }

                    GAME_WON -> {
                        gameFinalizedView =
                            topLayer.text("YOU ARE THE WINNER", textSize = 68, color = Colors.BLACK) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("YOU ARE THE WINNER", textSize = 66, color = Colors.WHITE) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("YOU ARE THE WINNER", textSize = 64, color = Colors.RED) {
                                centerOnStage()
                            }
                    }

                    NEXT_LEVEL -> {
                        gameFinalizedView =
                            topLayer.text("YOU MADE IT", textSize = 68, color = Colors.BLACK) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("YOU MADE IT", textSize = 66, color = Colors.WHITE) {
                                centerOnStage()
                            }
                        gameFinalizedView =
                            topLayer.text("YOU MADE IT", textSize = 64, color = Colors.GREEN) {
                                centerOnStage()
                            }
                    }

                    else -> {}
                }
            }
        }

        this.mouse {
            onClick {
                when (gameState) {
                    PLAY -> fieldView.onClick(it)
                    GAME_OVER, GAME_WON -> sceneContainer.changeTo<MainMenuScene>()
                    NEXT_LEVEL ->
                        sceneContainer.changeTo<GameScene>(
                            GameSceneState(
                                field.score,
                                currentLevel = state.remainingLevels.first(),
                                remainingLevels = state.remainingLevels.drop(1),
                            ),
                        )
                }
            }
        }
    }
}
