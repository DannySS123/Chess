package pieces

import com.main.Tile
import javafx.scene.image.Image

class Bishop(color: PieceColor, position: Position, image: Image) : Piece("Bishop", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val cX: Int = position.x
        val cY: Int = position.y

        for (a in -2..1) {
            val dir = bishopStepPos(a)
            while (isInBoard(cX + dir.x, cY + dir.y) && !isThereSame(tiles[cY + dir.y][cX + dir.x])) {
                val toTile: Tile = tiles[cY + dir.y][cX + dir.x]
                if (isGoodStep(tiles[cY][cX], toTile, tiles, turnColor)) {
                    p.add(Position(cX + dir.x, cY + dir.y))
                }
                if (toTile.piece != null) break
                dir.step()
            }
        }
        return p
    }
}