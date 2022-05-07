package pieces

import com.main.Game
import com.main.Tile
import javafx.scene.image.Image

abstract class Piece(val name: String, val color: PieceColor, var position: Position, val image: Image) {
    abstract fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position>

    open fun isThereSame(t: Tile): Boolean {
        return if (t.piece == null) false else t.piece!!.color == this.color
    }


    companion object {
        lateinit var game: Game

        fun setTheGame(g: Game) {
            this.game = g
        }

        fun isGoodStep(fromTile: Tile, toTile: Tile, tiles: Array<Array<Tile>>, turnColor: PieceColor): Boolean  {
            var res: Boolean = false
            val toPiece = toTile.piece

            game.step(fromTile, toTile, false)
            if (!isCheck(tiles, turnColor)) {
                res = true
            }
            game.step(toTile, fromTile, false)
            toTile.piece = toPiece
            return res
        }

        fun isCheck(tiles: Array<Array<Tile>>, turnColor: PieceColor): Boolean {
            var kingTile: Tile? = null
            for (i in 0..7) {
                for (j in 0..7) {
                    val piece: Piece? = tiles[i][j].piece
                    if (piece != null) {
                        if (piece.name == "King" && (piece.color == turnColor)) {
                            kingTile = tiles[i][j]
                            break
                        }
                    }
                }
            }

            val king: Piece = kingTile!!.piece!!
            val cX: Int = king.position.x
            val cY: Int = king.position.y
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
                if (cY + j in 0..7 && cX + i in 0..7) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null && king.color != piece.color && piece.name == "Knight") {
                        return true
                    }
                }
            }
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
                while (cY + j in 0..7 && cX + i in 0..7) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null) {
                        if (piece.color == king.color) {
                            break
                        }
                        val name: String = piece.name
                        if (king.color != piece.color) {
                            if (name == "Queen" || name == "Rook") {
                                return true
                            } else {
                                break
                            }
                        }
                    }
                    i += di
                    j += dj
                }
            }
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
                while (cY + j in 0..7 && cX + i < 8 && cX + i >= 0) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null) {
                        if (piece.color == king.color) {
                            break
                        }
                        val name: String = piece.name
                        if (king.color != piece.color) {
                            if (name == "Queen" || name == "Bishop") {
                                return true
                            } else {
                                break
                            }
                        }
                    }
                    i += di
                    j += dj
                }
            }
            val m = if (king.color == PieceColor.WHITE) -1 else 1
            if (cY + m in 0..7) {
                var t1: Tile? = null
                var piece1: Piece? = null
                if (cX + 1 < 8) {
                    t1 = tiles[cY + m][cX + 1]
                    piece1 = t1.piece
                }
                var t2: Tile? = null
                var piece2: Piece? = null
                if (cX - 1 >= 0) {
                    t2 = tiles[cY + m][cX - 1]
                    piece2 = t2.piece
                }
                if (t1?.piece != null && piece1!!.color != king.color && piece1.name == "Pawn" ||
                    t2?.piece != null && piece2!!.color != king.color && piece2.name == "Pawn"
                ) {
                    return true
                }
            }
            for (i in -1..1) {
                for (j in -1..1) {
                    if (cX + i in 0..7 && cY + j < 8 && cY + j >= 0 && (j != 0 || i != 0)) {
                        val piece: Piece? = tiles[cY + j][cX + i].piece
                        if (piece != null && piece.color != king.color && piece.name == "King") {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}