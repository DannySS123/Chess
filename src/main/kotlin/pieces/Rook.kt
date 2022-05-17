package pieces

import com.main.Tile
import javafx.scene.image.Image

class Rook(color: PieceColor, position: Position, image: Image) : Piece("Rook", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in -2..1) {
            var di = 0
            var dj = 0
            var i = 0
            var j = 0
            when (a) {
                -2 -> { di = 1; i = 1 }
                -1 -> { di = -1; i = -1 }
                0 -> { dj = 1; j = 1 }
                1 -> { dj = -1; j = -1 }
            }
            while (isInBoard(cX + i, cY + j) && !isThereSame(tiles[cY + j][cX + i])) {
                val toTile: Tile = tiles[cY + j][cX + i]
                if (isGoodStep(tiles[cY][cX], toTile, tiles, turnColor)) {
                    p.add(Position(cX + i, cY + j))
                }
                if (toTile.piece != null) break
                i += di
                j += dj
            }
        }
        return p
    }
}