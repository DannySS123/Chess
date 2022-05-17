package pieces

import com.main.Tile
import javafx.scene.image.Image
import kotlin.math.abs

class Pawn(color: PieceColor, position: Position, image: Image) : Piece("Pawn", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in 0..3) {
            var i = 0
            var j = 0
            when (a) {
                0 -> j = 1
                1 -> j = 2
                2 -> { i = 1; j = 1 }
                3 -> { i = -1; j = 1 }
            }
            if (color == PieceColor.WHITE) {
                j *= -1
            }
            if (isInBoard(cX + i, cY + j) && !isThereSame(tiles[cY + j][cX + i])) {
                val toTile: Tile = tiles[cY + j][cX + i]
                if (
                    (abs(j) == 2 && tiles[cY+(if(color == PieceColor.WHITE) -1 else 1)][cX].piece == null &&
                    cY == (if(color == PieceColor.WHITE) 6 else 1) &&
                    tiles[cY+(if(color == PieceColor.WHITE) -2 else 2)][cX].piece == null) ||
                    (abs(j) == 1 && i != 0 && toTile.piece != null && toTile.piece!!.color != color) ||
                    (abs(j) == 1 && i == 0 && toTile.piece == null)
                )  {
                    if (isGoodStep(tiles[cY][cX], toTile, tiles, turnColor)) {
                        p.add(Position(cX + i, cY + j))
                    }
                }
            }
        }
        return p
    }
}