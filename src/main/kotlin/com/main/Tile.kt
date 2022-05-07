package com.main

import javafx.scene.shape.Rectangle
import pieces.Piece

class Tile(var  piece: Piece?, var selected: Boolean, val background: Rectangle, var image: Rectangle, var selectedToStep: Boolean = false)