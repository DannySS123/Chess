package pieces

import com.main.Game
import com.main.Tile
import javafx.scene.image.Image

abstract class Piece(val name: String, val color: PieceColor, var position: Position, val image: Image) {
    abstract fun possibleMoves(tiles: Array<Array<Tile>>, turnColor: PieceColor): MutableList<Position>

    fun isThereSame(t: Tile): Boolean {
        return if (t.piece == null) false else t.piece!!.color == this.color
    }

    companion object {
        private lateinit var game: Game

        fun setTheGame(g: Game) {
            this.game = g
        }

        fun isInBoard(i: Int, j: Int): Boolean = (i in 0..7 && j in 0..7)

        fun knightStepPos(a: Int): Pair<Int, Int> {
            return when (a) {
                0 -> Pair(1,2)
                1 -> Pair(1,-2)
                2 -> Pair(-1,2)
                3 -> Pair(-1,-2)
                4 -> Pair(2,1)
                5 -> Pair(2,-1)
                6 -> Pair(-2,1)
                7 -> Pair(-2,-1)
                else -> Pair(0,0)
            }
        }

        fun bishopStepPos(a: Int): Pair<Pair<Int, Int>, Pair<Int, Int>> {
            return when (a) {
                -2 -> Pair(Pair(1,1), Pair(1,1))
                -1 -> Pair(Pair(-1,-1), Pair(-1,-1))
                0 -> Pair(Pair(1,-1), Pair(1,-1))
                1 -> Pair(Pair(-1,1), Pair(-1,1))
                else -> Pair(Pair(0,0), Pair(0,0))
            }
        }

        fun rookStepPos(a: Int): Pair<Pair<Int, Int>, Pair<Int, Int>> {
            return when (a) {
                -2 -> Pair(Pair(1,0), Pair(1,0))
                -1 -> Pair(Pair(-1, 0), Pair(-1,0))
                0 -> Pair(Pair(0,-1), Pair(0,-1))
                1 -> Pair(Pair(0,1), Pair(0,1))
                else -> Pair(Pair(0,0), Pair(0,0))
            }
        }

        fun isGoodStep(fromTile: Tile, toTile: Tile, tiles: Array<Array<Tile>>, turnColor: PieceColor): Boolean  {
            val toPiece = toTile.piece
            val toFill = toTile.image.fill
            game.step(fromTile, toTile, false)
            val res = !isCheck(tiles, turnColor)
            game.step(toTile, fromTile, false)
            toTile.piece = toPiece
            toTile.image.fill = toFill
            return res
        }

        fun isCheck(tiles: Array<Array<Tile>>, turnColor: PieceColor): Boolean {
            var kingTile: Tile? = null
            for (i in 0..7) {
                for (j in 0..7) {
                    val piece: Piece? = tiles[i][j].piece
                    if (piece != null) {
                        if (piece is King && (piece.color == turnColor)) {
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
                val pair = knightStepPos(a)
                val i = pair.first
                val j = pair.second
                if (isInBoard(cX + i, cY + j)) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null && king.color != piece.color && piece is Knight) {
                        return true
                    }
                }
            }
            for (a in -2..1) {
                val dir = rookStepPos(a)
                var i = dir.first.first
                var j = dir.first.second
                while (isInBoard(cX + i, cY + j)) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null) {
                        if (king.color != piece.color && (piece is Queen || piece is Rook)) return true else break
                    }
                    i += dir.second.first
                    j += dir.second.second
                }
            }
            for (a in -2..1) {
                val dir = bishopStepPos(a)
                var i = dir.first.first
                var j = dir.first.second
                while (isInBoard(cX + i, cY + j)) {
                    val piece: Piece? = tiles[cY + j][cX + i].piece
                    if (piece != null) {
                        if (king.color != piece.color && (piece is Queen || piece is Bishop)) return true else break
                    }
                    i += dir.second.first
                    j += dir.second.second
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
                if (t1?.piece != null && piece1!!.color != king.color && piece1 is Pawn ||
                    t2?.piece != null && piece2!!.color != king.color && piece2 is Pawn
                ) {
                    return true
                }
            }
            for (i in -1..1) {
                for (j in -1..1) {
                    if (isInBoard(cX + i, cY + j)&& (j != 0 || i != 0)) {
                        val piece: Piece? = tiles[cY + j][cX + i].piece
                        if (piece != null && piece.color != king.color && piece is King) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}