package com.main

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import pieces.*
import java.io.IOException

class Game : Application() {

    companion object {
        private const val WIDTH = 720
        private const val HEIGHT = 720
    }

    private lateinit var mainScene: Scene
    private lateinit var gc: GraphicsContext

    private lateinit var chessBoard: Image
    private lateinit var sun: Image

    private var sunX = WIDTH / 2
    private var sunY = HEIGHT / 2

    private var lastFrameTime: Long = System.nanoTime()

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

    private val tiles: Array<Array<Tile>> = Array(8) { Array(8) { Tile(null, false, Rectangle(), Rectangle())} }

    private var turnColor = PieceColor.WHITE
    private var selectedTile: Tile? = null
    private var check: Boolean = false

    override fun start(mainStage: Stage) {
        mainStage.title = "Chess"

        Piece.setTheGame(this)
        createStarterSetup()

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene

        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())
        root.children.add(canvas)

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val tile = tiles[x][y]
                addEventHandler(tile)
                root.children.add(tile.background)
                root.children.add(tile.image)
            }
        }

        prepareActionHandlers()

        gc = canvas.graphicsContext2D

        loadGraphics()

        // Main loop
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender(currentNanoTime)
            }
        }.start()

        mainStage.show()
    }

    private fun addEventHandler(tile: Tile) {

        val mouseEventHandler: EventHandler<MouseEvent> = EventHandler {
            /*val selectedTile: Tile
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val tile = tiles[x][y]

                }
            }*/
            if (tile.selectedToStep) {
                step(selectedTile!!, tile, true)
            } else if (!tile.selected) {
                tile.selected = true
                selectedTile = tile
                for (a in 0 until 8) {
                    for (b in 0 until 8) {
                        if (tile != tiles[a][b]) {
                            tiles[a][b].selected = false
                        }
                    }
                }
            }
        }
        tile.image.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandler)
        tile.background.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandler)
    }

    fun step(from: Tile, to: Tile, changeTurn: Boolean) {
        var newPos: Position? = null

        if (changeTurn) {
            check = false
        }

        for (a in 0 until 8) {
            for (b in 0 until 8) {
                val t: Tile = tiles[a][b]
                if (changeTurn) {
                    t.selected = false
                }
                t.selectedToStep = false
                if (t == to) {
                    newPos = Position(b, a)
                }
            }
        }

        to.piece = from.piece
        to.piece!!.position = newPos!!
        from.piece = null
        from.image.fill = null
        if (changeTurn) {
            turnColor = if (turnColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        }
        if (changeTurn && Piece.isCheck(tiles, turnColor)) {
            check = true
        }
    }

    private fun prepareActionHandlers() {
        mainScene.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
        }
        mainScene.onKeyReleased = EventHandler { event ->
            currentlyActiveKeys.remove(event.code)
        }
    }

    private fun loadGraphics() {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder
        chessBoard = Image(getResource("/chessBoard.png"))
        sun = Image(getResource("/sun.png"))
    }

    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc
        val elapsedNanos = currentNanoTime - lastFrameTime
        lastFrameTime = currentNanoTime

        // clear canvas
       // gc.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())

        // draw background
        //graphicsContext.drawImage(chessBoard, 0.0, 0.0)

        // perform world updates
        updateSunPosition()


        val size: Double = WIDTH.toDouble()/8
        gc.fill = Color.BLACK
        val pm: MutableList<Position> = mutableListOf()
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val tile = tiles[x][y]
                if ((x + 1) % 2 == 1 && y % 2 == 1 || x % 2 == 1 && (y + 1) % 2 == 1) {
                    tile.background.fill = Color.GRAY
                } else {
                    tile.background.fill = Color.LIGHTGRAY
                }
                if (tile.selected && tile.piece?.color == turnColor) {
                    tile.background.fill = Color.GREEN
                    pm.addAll(tile.piece!!.possibleMoves(tiles, turnColor))
                }
                tile.background.x = size*y
                tile.background.y = size*x
                tile.background.width = size
                tile.background.height = size

                if (check && tile.piece != null && tile.piece!!.name == "King" && tile.piece!!.color == turnColor && !tile.selected) {
                    tile.background.fill = Color.RED
                }

                //gc.fillRect(size*y, size*x, size, size)
                if (tile.piece != null) {
                    tile.image.x = size*y
                    tile.image.y = size*x
                    tile.image.width = size
                    tile.image.height = size
                    tile.image.fill = ImagePattern(tile.piece!!.image)
                }
                //gc.drawImage(tile.piece?.image, size*y, size*x, size, size)


            }
        }

        pm.forEach {
            val t = tiles[it.y][it.x]
            t.background.fill = Color.LIGHTGREEN
            t.selectedToStep = true
            //println("${it.x}  ${it.y}")
            if (t.piece != null) {
                t.image.fill = ImagePattern(t.piece!!.image)
            }
        }


        /// draw sun
        gc.drawImage(sun, sunX.toDouble(), sunY.toDouble(), size, size)

        displayFPS(elapsedNanos)
    }

    private fun updateSunPosition() {
        if (currentlyActiveKeys.contains(KeyCode.LEFT) && sunX > 0) {
            sunX -= 4
        }
        if (currentlyActiveKeys.contains(KeyCode.RIGHT) && sunX + 50 < WIDTH) {
            sunX += 4
        }
        if (currentlyActiveKeys.contains(KeyCode.UP) && sunY > 0) {
            sunY -= 4
        }
        if (currentlyActiveKeys.contains(KeyCode.DOWN) && sunY + 50 < HEIGHT) {
            sunY += 4
        }
    }

    @Throws(IOException::class)
    fun createStarterSetup() {

        /*endOfGame = false
        clearTiles()
        */

        tiles[0][7].piece = Rook(PieceColor.BLACK, Position(7, 0), Image(getResource("/blackRook.png")))
        tiles[0][0].piece = Rook(PieceColor.BLACK, Position(0, 0), Image(getResource("/blackRook.png")))
        tiles[7][0].piece = Rook(PieceColor.WHITE, Position(0, 7), Image(getResource("/whiteRook.png")))
        tiles[7][7].piece = Rook(PieceColor.WHITE, Position(7, 7), Image(getResource("/whiteRook.png")))
        tiles[0][2].piece = Bishop(PieceColor.BLACK, Position(2, 0), Image(getResource("/blackBishop.png")))
        tiles[0][5].piece = Bishop(PieceColor.BLACK, Position(5, 0), Image(getResource("/blackBishop.png")))
        tiles[7][2].piece = Bishop(PieceColor.WHITE, Position(2, 7), Image(getResource("/whiteBishop.png")))
        tiles[7][5].piece = Bishop(PieceColor.WHITE, Position(5, 7), Image(getResource("/whiteBishop.png")))
        tiles[0][1].piece = Knight(PieceColor.BLACK, Position(1, 0), Image(getResource("/blackHorse.png")))
        tiles[0][6].piece = Knight(PieceColor.BLACK, Position(6, 0), Image(getResource("/blackHorse.png")))
        tiles[7][1].piece = Knight(PieceColor.WHITE, Position(1, 7), Image(getResource("/whiteHorse.png")))
        tiles[7][6].piece = Knight(PieceColor.WHITE, Position(6, 7), Image(getResource("/whiteHorse.png")))
        tiles[0][4].piece = King(PieceColor.BLACK, Position(4, 0), Image(getResource("/blackKing.png")))
        tiles[7][4].piece = King(PieceColor.WHITE, Position(4, 7), Image(getResource("/whiteKing.png")))
        tiles[0][3].piece = Queen(PieceColor.BLACK, Position(3, 0), Image(getResource("/blackQueen.png")))
        tiles[7][3].piece = Queen(PieceColor.WHITE, Position(3, 7), Image(getResource("/whiteQueen.png")))
        for (i in 0..7) {
            tiles[1][i].piece = Pawn(PieceColor.BLACK, Position(i, 1), Image(getResource("/blackPawn.png")))
            tiles[6][i].piece = Pawn(PieceColor.WHITE, Position(i, 6), Image(getResource("/whitePawn.png")))

        }


        /*addPicLabels()
        val check: Boolean = Piece.isCheck(tiles, whiteTurn)
        isEnd(check)*/
    }

    private fun displayFPS(elapsedNanos: Long) {
        // display crude fps counter
        val elapsedMs = elapsedNanos / 1_000_000
        if (elapsedMs != 0L) {
            gc.fill = Color.BLACK
            gc.fillText("${1000 / elapsedMs} fps", 0.0, 10.0)
        }
    }

}
