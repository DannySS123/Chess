package pieces

import com.main.Tile
import javafx.scene.image.Image

class Knight(color: PieceColor, position: Position, image: Image) : Piece("Knight", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in 0..7) {
            var i = 0
            var j = 0
            when (a) {
                0 -> { i = 1; j = 2 }
                1 -> { i = 1; j = -2 }
                2 -> { i = -1; j = 2 }
                3 -> { i = -1; j = -2 }
                4 -> { i = 2; j = 1 }
                5 -> { i = 2; j = -1 }
                6 -> { i = -2; j = 1 }
                7 -> { i = -2; j = -1 }
            }
            if (isInBoard(cX + i, cY + j) && !isThereSame(tiles[cY + j][cX + i]) &&
                isGoodStep(tiles[cY][cX], tiles[cY + j][cX + i], tiles, turnColor)) {
                    p.add(Position(cX + i, cY + j))
            }
        }
        return p
    }
}