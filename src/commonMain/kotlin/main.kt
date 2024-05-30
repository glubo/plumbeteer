import korlibs.image.atlas.readAtlas
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.RegisteredImageFormats
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.mouse
import korlibs.korge.scene.Scene
import korlibs.korge.scene.SceneContainer
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.SContainer
import korlibs.korge.view.View
import korlibs.korge.view.addUpdater
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.centered
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.solidRect
import korlibs.korge.view.sprite
import korlibs.korge.view.text
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2
import korlibs.time.TimeSpan
import tile.CornerPipe
import tile.CrossPipe
import tile.EmptyTile
import tile.StartPipe
import tile.StraightPipe
import kotlin.time.Duration.Companion.seconds

suspend fun main() =
    Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        injector.mapPrototype { MyScene(get()) }
        injector.mapPrototype { MainMenuScene() }
        injector.mapSingleton { AssetsLoader() }
        RegisteredImageFormats.register(PNG)
        sceneContainer.changeTo<MainMenuScene>()
    }

class MainMenuScene : Scene() {
    override suspend fun SContainer.sceneInit() {
    }

    override suspend fun SContainer.sceneMain() {
        text("Pipeteer", textSize = 64, color = Colors.RED) {
            centerOnStage()
        }
        this.mouse {
            onClick {
                sceneContainer.changeTo<MyScene>()
            }
        }
    }
}

class StagingAreaView(
    val stagingArea: StagingArea,
    val assets: Assets,
    val sContainer: SceneContainer,
    val viewRectangle: Rectangle,
) {
    val views = mutableListOf<View>()
    val tileSize =
        Size(
            viewRectangle.width,
            viewRectangle.height / stagingArea.count,
        )

    fun update(dt: TimeSpan) {
        views.forEach { it.removeFromParent() }
        views.clear()

        stagingArea.fifo.forEachIndexed { y, tile ->
            val tilePos =
                Vector2(
                    viewRectangle.x,
                    viewRectangle.y + y * tileSize.height,
                )
            val tileRect =
                Rectangle(
                    tilePos.x,
                    tilePos.y,
                    tileSize.width,
                    tileSize.height,
                )

            views.add(
                sContainer.sprite(assets.empty) {
                    position(tilePos.x, tilePos.y)
                    size(tileSize)
                },
            )

            when (tile) {
                is EmptyTile -> null
                is CornerPipe -> sContainer.baseCornerPipe(assets, tileRect, tile)
                is CrossPipe -> sContainer.baseCrossPipe(assets, tileRect, tile)
                is StartPipe -> null
                is StraightPipe -> sContainer.baseStraigthPipe(assets, tileRect, tile)
            }?.let {
                views.add(it)
            }
        }
    }
}

fun SceneContainer.baseCornerPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: CornerPipe,
) = this.sprite(
    assets.corner,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
    rotation(tile.direction.angle())
}

fun SceneContainer.baseStraigthPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: StraightPipe,
) = this.sprite(
    assets.straightV,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
    rotation(tile.orientation.directions.first().angle())
}

fun SceneContainer.baseCrossPipe(
    assets: Assets,
    tileRect: Rectangle,
    tile: CrossPipe,
) = this.sprite(
    assets.cross,
) {
    position(tileRect.centerX, tileRect.centerY)
    centered
    size(tileRect.size)
}

class AssetsLoader() {
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

class MyScene(
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

        val fieldLayer = sceneContainer()
        val topLayer = sceneContainer()

        val staging =
            StagingArea().also {
                it.replenish()
            }
        val field =
            PlayField(
                xtiles = 10,
                ytiles = 10,
                staging,
            ) { println("unhandled $it") }

        val stagingView =
            StagingAreaView(
                staging,
                assets,
                fieldLayer,
                Rectangle(450, 0, 40, 200),
            )
        val fieldView =
            PlayFieldView(
                field,
                assets,
                fieldLayer,
                Rectangle(
                    0,
                    0,
                    400,
                    400,
                ),
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
