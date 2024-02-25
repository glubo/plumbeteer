import korlibs.image.atlas.Atlas
import korlibs.korge.view.SpriteAnimation

data class Assets(val atlas: Atlas) {
    val transparent = atlas["transparent"]
    val empty = atlas["empty"]
    val corner = atlas["corner"]
    val start = atlas["start"]
    val cross = atlas["cross"]
    val straightH = atlas["straightH"]
    val straightV = atlas["straightV"]
    val startFluid =
        SpriteAnimation(
            listOf(transparent) +
                    (1..8).map {
                        atlas["start-fluid$it"]
                    },
        )
    val cornerFluid =
        SpriteAnimation(
            listOf(transparent) +
                    (1..8).map {
                        atlas["corner-fluid$it"]
                    },
        )
    val cornerFluidFlipped =
        SpriteAnimation(
            listOf(transparent) +
                    (1..8).map {
                        atlas["corner-fluid-flip$it"]
                    },
        )
    val straightFluid =
        SpriteAnimation(
            listOf(transparent) +
                    (1..8).map {
                        atlas["straight-fluid$it"]
                    },
        )
}
