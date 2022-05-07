package pieces

import com.main.Tile
import com.main.getResource
import javafx.scene.image.Image

class Queen(color: PieceColor, position: Position, image: Image) : Piece("Queen", color, position, image) {
    override fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position> {
        val p: MutableList<Position> = mutableListOf()
        val r = Rook(color, Position(position.x, position.y), Image(getResource("/blackRook.png")))
        val b = Bishop(color, Position(position.x, position.y), Image(getResource("/blackBishop.png")))
        p.addAll(r.possibleMoves(tiles, turnColor))
        p.addAll(b.possibleMoves(tiles, turnColor))
        return p
    }
}