package pieces

import com.main.Tile
import javafx.scene.image.Image

class Knight(color: PieceColor, position: Position, image: Image) : Piece("Knight", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in 0..7) {
            val pair = knightStepPos(a)
            val i = pair.first
            val j = pair.second
            if (isInBoard(cX + i, cY + j) && !isThereSame(tiles[cY + j][cX + i]) &&
                isGoodStep(tiles[cY][cX], tiles[cY + j][cX + i], tiles, turnColor)) {
                    p.add(Position(cX + i, cY + j))
            }
        }
        return p
    }
}