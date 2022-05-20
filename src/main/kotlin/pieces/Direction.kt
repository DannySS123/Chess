package pieces

class Direction(var x: Int, var y: Int, val dX: Int, val dY: Int) {
    fun step() {
        x += dX
        y += dY
    }
}