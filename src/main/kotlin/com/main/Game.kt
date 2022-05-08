package com.main

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import javafx.stage.Stage
import pieces.*
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter


class Game : Application() {

    companion object {
        private const val WIDTH = 800
        private const val HEIGHT = 720
    }

    private lateinit var mainScene: Scene
    private lateinit var gc: GraphicsContext


    private var lastFrameTime: Long = System.nanoTime()

    // use a set so duplicates are not possible
    private val currentlyActiveKeys = mutableSetOf<KeyCode>()

    private val tiles: Array<Array<Tile>> = Array(8) { Array(8) { Tile(null, false, Rectangle(), Rectangle())} }

    private var turnColor = PieceColor.WHITE
    private var selectedTile: Tile? = null
    private var check: Boolean = false
    private var endOfGame: Boolean = false
    private var actionHappened: Boolean = false

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

        /*-------------------------------------
        val alert = Alert(AlertType.INFORMATION)
        alert.title = "End of the game!"
        alert.headerText = null
        alert.contentText = "Congrats!\n" + (if (turnColor == PieceColor.BLACK) "WHITE" else "BLACK") + " is the winner!"
        alert.setOnHidden { alert.close() }
        alert.show()
        val options = FXCollections.observableArrayList(
            "Option 1",
            "Option 2",
            "Option 3"
        )
        val comboBox = ComboBox(options);
        comboBox.translateX = 750.0
        //root.children.add(comboBox)
        //-------------------------------------*/
        val restartButton = Button("Restart")
        restartButton.translateX = 725.0
        restartButton.translateY = 90.0
        restartButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            createStarterSetup()
        }

        val saveButton = Button("Save")
        saveButton.translateX = 725.0
        saveButton.translateY = 30.0
        saveButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            //Creating a File chooser
            val fileChooser = FileChooser()
            fileChooser.title = "Save"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("All Files", "*.*"))
            val file: File? = fileChooser.showSaveDialog(mainStage)
            if (file != null) {
                val pw = PrintWriter(file)
                pw.println(if(turnColor == PieceColor.WHITE) "white" else "black")
                tiles.forEach { tiles -> tiles.forEach { tile ->
                    if (tile.piece != null) {
                        pw.println(tile.toString())
                    }
                }}
                pw.flush()
            }

        }

        val loadButton = Button("Load")
        loadButton.translateX = 725.0
        loadButton.translateY = 60.0
        loadButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            //Creating a File chooser
            val fileChooser = FileChooser()
            fileChooser.title = "Load"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("All Files", "*.*"))
            val file: File? = fileChooser.showOpenDialog(mainStage)
            if (file != null) {
                clearTiles()
                val fileReader = FileReader(file)
                fileReader.readLines().forEach { line ->
                    val input = line.split(" ")
                    var x = 0
                    var y = 0
                    var c: PieceColor = PieceColor.WHITE
                    if (input.size > 1) {
                        y = input[3].toInt()
                        x = input[2].toInt()
                        c = if (input[1] == "WHITE") PieceColor.WHITE else PieceColor.BLACK
                    }
                    when(input[0]) {
                        "white" -> turnColor = PieceColor.WHITE
                        "black" -> turnColor = PieceColor.BLACK
                        "King" -> tiles[y][x].piece = King(c, Position(x, y), Image(getResource("/${input[1].lowercase()}King.png")))
                        "Queen" -> tiles[y][x].piece = Queen(c, Position(x, y), Image(getResource("/${input[1].lowercase()}Queen.png")))
                        "Knight" -> tiles[y][x].piece = Knight(c, Position(x, y), Image(getResource("/${input[1].lowercase()}Knight.png")))
                        "Rook" -> tiles[y][x].piece = Rook(c, Position(x, y), Image(getResource("/${input[1].lowercase()}Rook.png")))
                        "Bishop" -> tiles[y][x].piece = Bishop(c, Position(x, y), Image(getResource("/${input[1].lowercase()}Bishop.png")))
                        "Pawn" -> tiles[y][x].piece = Pawn(c, Position(x, y), Image(getResource("/${input[1].lowercase()}Pawn.png")))
                    }
                }
                actionHappened = true
            }
        }


        root.children.add(restartButton)
        root.children.add(saveButton)
        root.children.add(loadButton)


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
            actionHappened = true
            if (tile.selectedToStep) {
                step(selectedTile!!, tile, true)
            } else if (!tile.selected) {
                tile.selected = true
                selectedTile = tile
                for (a in 0 until 8) {
                    for (b in 0 until 8) {
                        if (tile != tiles[a][b]) {
                            tiles[a][b].selected = false
                            tiles[a][b].selectedToStep = false
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

        if (changeTurn) {
            if (newPos!!.y == 0 && from.piece!!.color == PieceColor.WHITE && from.piece!!.name == "Pawn") {
                to.piece = Queen(PieceColor.WHITE, Position(newPos.x, 0), Image(getResource("/whiteQueen.png")))
            } else if (newPos.y == 7 && from.piece!!.color == PieceColor.BLACK && from.piece!!.name == "Pawn") {
                to.piece = Queen(PieceColor.BLACK, Position(newPos.x, 7), Image(getResource("/blackQueen.png")))
            }
        }

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

    }

    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed since the last frame, in nanoseconds
        // can be used for physics calculation, etc
        val elapsedNanos = currentNanoTime - lastFrameTime
        lastFrameTime = currentNanoTime

        // clear canvas
        gc.clearRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())

        // draw background
        //graphicsContext.drawImage(chessBoard, 0.0, 0.0)

        // perform world updates
        updateSunPosition()

        if (actionHappened) {
            actionHappened = false
            val size: Double = (WIDTH.toDouble() - 80) / 8
            gc.fill = Color.BLACK
            val pm: MutableList<Position> = mutableListOf()
            val allPm: MutableList<Position> = mutableListOf()
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val tile = tiles[x][y]
                    if ((x + 1) % 2 == 1 && y % 2 == 1 || x % 2 == 1 && (y + 1) % 2 == 1) {
                        tile.background.fill = Color.GRAY
                    } else {
                        tile.background.fill = Color.LIGHTGRAY
                    }
                    if (tile.piece?.color == turnColor) {
                        if (tile.selected) {
                            tile.background.fill = Color.GREEN
                            pm.addAll(tile.piece!!.possibleMoves(tiles, turnColor))
                        }
                        allPm.addAll(tile.piece!!.possibleMoves(tiles, turnColor))
                    }

                    tile.background.x = size * y
                    tile.background.y = size * x
                    tile.background.width = size
                    tile.background.height = size

                    if (check && tile.piece != null && tile.piece!!.name == "King" && tile.piece!!.color == turnColor && !tile.selected) {
                        tile.background.fill = Color.RED
                    }

                    //gc.fillRect(size*y, size*x, size, size)
                    if (tile.piece != null) {
                        tile.image.x = size * y
                        tile.image.y = size * x
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

            if (allPm.size == 0 && !endOfGame) {
                endOfGame = true
                val alert = Alert(AlertType.INFORMATION)
                alert.title = "End of the game!"
                alert.headerText = null
                alert.contentText = (if (check) "Congrats!\n" + (if (turnColor == PieceColor.BLACK) "WHITE" else "BLACK") + " is the winner!" else "Stalemate!")
                check = false
                alert.setOnHidden { alert.close() }
                alert.show()
            }
        }
        /// draw sun
        //gc.drawImage(sun, sunX.toDouble(), sunY.toDouble(), size, size)

        displayFPS(elapsedNanos)
    }

    private fun updateSunPosition() {

    }

    private fun clearTiles() {
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                tiles[y][x].piece = null
                tiles[y][x].image.fill = null
            }
        }
    }

    @Throws(IOException::class)
    fun createStarterSetup() {
        endOfGame = false
        actionHappened = true
        turnColor = PieceColor.WHITE
        clearTiles()
        tiles[0][7].piece = Rook(PieceColor.BLACK, Position(7, 0), Image(getResource("/blackRook.png")))
        tiles[0][0].piece = Rook(PieceColor.BLACK, Position(0, 0), Image(getResource("/blackRook.png")))
        tiles[7][0].piece = Rook(PieceColor.WHITE, Position(0, 7), Image(getResource("/whiteRook.png")))
        tiles[7][7].piece = Rook(PieceColor.WHITE, Position(7, 7), Image(getResource("/whiteRook.png")))
        tiles[0][2].piece = Bishop(PieceColor.BLACK, Position(2, 0), Image(getResource("/blackBishop.png")))
        tiles[0][5].piece = Bishop(PieceColor.BLACK, Position(5, 0), Image(getResource("/blackBishop.png")))
        tiles[7][2].piece = Bishop(PieceColor.WHITE, Position(2, 7), Image(getResource("/whiteBishop.png")))
        tiles[7][5].piece = Bishop(PieceColor.WHITE, Position(5, 7), Image(getResource("/whiteBishop.png")))
        tiles[0][1].piece = Knight(PieceColor.BLACK, Position(1, 0), Image(getResource("/blackKnight.png")))
        tiles[0][6].piece = Knight(PieceColor.BLACK, Position(6, 0), Image(getResource("/blackKnight.png")))
        tiles[7][1].piece = Knight(PieceColor.WHITE, Position(1, 7), Image(getResource("/whiteKnight.png")))
        tiles[7][6].piece = Knight(PieceColor.WHITE, Position(6, 7), Image(getResource("/whiteKnight.png")))
        tiles[0][4].piece = King(PieceColor.BLACK, Position(4, 0), Image(getResource("/blackKing.png")))
        tiles[7][4].piece = King(PieceColor.WHITE, Position(4, 7), Image(getResource("/whiteKing.png")))
        tiles[0][3].piece = Queen(PieceColor.BLACK, Position(3, 0), Image(getResource("/blackQueen.png")))
        tiles[7][3].piece = Queen(PieceColor.WHITE, Position(3, 7), Image(getResource("/whiteQueen.png")))
        for (i in 0..7) {
            tiles[1][i].piece = Pawn(PieceColor.BLACK, Position(i, 1), Image(getResource("/blackPawn.png")))
            tiles[6][i].piece = Pawn(PieceColor.WHITE, Position(i, 6), Image(getResource("/whitePawn.png")))

        }

    }

    private fun displayFPS(elapsedNanos: Long) {
        // display crude fps counter
        val elapsedMs = elapsedNanos / 1_000_000
        if (elapsedMs != 0L) {
            gc.fill = Color.BLACK
            gc.fillText("${1000 / elapsedMs} fps", WIDTH-50.0, 10.0)
        }
    }

}
