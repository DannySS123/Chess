package pieces

import com.main.Tile
import javafx.scene.image.Image

class King(color: PieceColor, position: Position, image: Image) : Piece("King", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val cX: Int = position.x
        val cY: Int = position.y
        val p: MutableList<Position> = mutableListOf()
        for (i in -1..1) {
            for (j in -1..1) {
                if (isInBoard(cX + i, cY + j) &&  (j != 0 || i != 0) && !isThereSame(tiles[cY + j][cX + i]) &&
                     isGoodStep(tiles[cY][cX], tiles[cY + j][cX + i], tiles, turnColor)) {
                        p.add(Position(cX + i, cY + j))
                }
            }
        }
        return p
    }
}