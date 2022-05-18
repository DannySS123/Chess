package pieces

import com.main.Tile
import javafx.scene.image.Image

class Rook(color: PieceColor, position: Position, image: Image) : Piece("Rook", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in -2..1) {
            val dir = rookStepPos(a)
            var i = dir.first.first
            var j = dir.first.second
            while (isInBoard(cX + i, cY + j) && !isThereSame(tiles[cY + j][cX + i])) {
                val toTile: Tile = tiles[cY + j][cX + i]
                if (isGoodStep(tiles[cY][cX], toTile, tiles, turnColor)) {
                    p.add(Position(cX + i, cY + j))
                }
                if (toTile.piece != null) break
                i += dir.second.first
                j += dir.second.second
            }
        }
        return p
    }
}