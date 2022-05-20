package pieces

import com.main.Tile
import javafx.scene.image.Image

class Knight(color: PieceColor, position: Position, image: Image) : Piece("Knight", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in 0..7) {
            val xy = knightStepPos(a)
            if (isInBoard(cX + xy.first, cY + xy.second) && !isThereSame(tiles[cY + xy.second][cX + xy.first]) &&
                isGoodStep(tiles[cY][cX], tiles[cY + xy.second][cX + xy.first], tiles, turnColor)) {
                    p.add(Position(cX + xy.first, cY + xy.second))
            }
        }
        return p
    }
}