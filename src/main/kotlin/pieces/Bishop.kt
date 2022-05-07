package pieces

import com.main.Tile
import javafx.scene.image.Image

class Bishop(color: PieceColor, position: Position, image: Image) : Piece("Bishop", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in -2..1) {
            var di = 1
            var dj = 1
            var i = 1
            var j = 1
            when (a) {
                -1 -> { dj = -1; di = -1; j = -1; i = -1 }
                0 -> { di = -1; i = -1 }
                1 -> { dj = -1; j = -1 }
            }
            while (cY + j in 0..7 && cX + i in 0..7 && !isThereSame(tiles[cY + j][cX + i])) {
                if (isGoodStep(tiles[cY][cX], tiles[cY + j][cX + i], tiles, turnColor)) {
                    p.add(Position(cX + i, cY + j))
                }

                if (tiles[cY + j][cX + i].piece != null) {
                    break
                }
                i += di
                j += dj
            }
        }
        return p
    }
}