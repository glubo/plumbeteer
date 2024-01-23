import korlibs.image.atlas.*
import korlibs.time.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*

suspend fun main() = Korge(windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()

    RegisteredImageFormats.register(PNG)
	sceneContainer.changeTo({ MyScene() })
}




class MyScene : Scene() {
	override suspend fun SContainer.sceneMain() {
		val minDegrees = (-16).degrees
		val maxDegrees = (+16).degrees

        val atlas = resourcesVfs["texture.json"].readAtlas()
		val image = image(resourcesVfs["korge.png"].readBitmap()) {
			rotation = maxDegrees
			anchor(.5, .5)
			scale(0.1)
			position(256, 256)
		}


        val field = PlayField(
            Rectangle(0,0, 400, 400),
            xtiles = 10,
            ytiles = 10,
            this,
            Assets(atlas)
        )

		while (true) {
			image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
			image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		}
	}
}
