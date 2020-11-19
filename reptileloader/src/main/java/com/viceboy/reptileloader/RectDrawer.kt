package com.viceboy.reptileloader

import android.animation.ValueAnimator
import android.graphics.*
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

class RectDrawer(
        private var loaderLength: Int,
        private val cornerRadius: Int,
        private val padding : Int,
        private val speed: Int
) : Drawer {

    //Tracking Drawing coordinates
    private var height = 0
    private var width = 0

    private var isEndRunning = false
    private var hasCrossedEnd = false

    private val startCoord = Coord()
    private val endCoord = Coord()
    private val middleCoords = mutableListOf<Coord>()

    private val path = Path()
    private val borderRect = Rect()

    private var moveDir = MoveDir.LEFT_TO_RIGHT

    override fun onDraw(canvas: Canvas, paintSimple: Paint, paintBlur: Paint) {
        path.reset()

        val strokeGap = paintBlur.strokeWidth - paintSimple.strokeWidth
        val diff =
                floor(Math.PI * cornerRadius / 2f).toFloat() - (paintBlur.strokeWidth - paintSimple.strokeWidth)

        path.apply {
            moveTo(startCoord.x.toFloat(), startCoord.y.toFloat())

            if (cornerRadius > 0 && hasCrossedEnd) {
                when (moveDir) {
                    MoveDir.RIGHT_TO_LEFT -> {
                        val ratio = ((startCoord.x.toFloat()) / width.toFloat())
                        val updateX = (diff * ratio)

                        cubicTo(
                                width.toFloat() - strokeGap,
                                height.toFloat() - paintBlur.strokeWidth,
                                width.toFloat() + 8f,
                                height.toFloat() + 2f * updateX,
                                endCoord.x.toFloat(),
                                endCoord.y.toFloat()
                        )
                    }

                    MoveDir.LEFT_TO_RIGHT -> {
                        val ratio = ((startCoord.x.toFloat()) / width.toFloat())
                        val updateX = diff - (diff * ratio)

                        cubicTo(
                                -strokeGap, paintBlur.strokeWidth, 8f,
                                -2f * updateX, endCoord.x.toFloat(), endCoord.y.toFloat()
                        )
                    }

                    MoveDir.TOP_TO_BOTTOM -> {
                        val ratio = (startCoord.y.toFloat()) / height.toFloat()
                        val updateY = diff - diff * ratio

                        cubicTo(
                                -4f, height + updateY, -paintBlur.strokeWidth,
                                height.toFloat(), endCoord.x.toFloat(), endCoord.y.toFloat()
                        )
                    }

                    else -> {
                        val ratio = (startCoord.y.toFloat()) / height.toFloat()
                        val updateY = diff * ratio

                        cubicTo(
                                width + 4f, -updateY, width + paintBlur.strokeWidth,
                                0f, endCoord.x.toFloat(), endCoord.y.toFloat()
                        )
                    }
                }
            } else {
                middleCoords.forEach {
                    lineTo(it.x.toFloat(), it.y.toFloat())
                }
                lineTo(endCoord.x.toFloat(), endCoord.y.toFloat())
            }
        }

        drawPaths(canvas, paintSimple, paintBlur)
    }

    override fun initStartAndEndCoord(contentRect: Rect) {
        height = contentRect.height()
        width = contentRect.width()
        borderRect.inset(padding, padding)

        initStartCoord()
        updateEndCoord()
    }

    /**
     * Initializing starting coordinates
     */
    private fun initStartCoord() {
        val startX = startCoord.x + width
        val startY = borderRect.top
        startCoord.set(startX, startY)
    }

    /**
     * Update Start Coordinates
     */
    private fun updateStartCoord() {
        when (moveDir) {
            MoveDir.LEFT_TO_RIGHT -> {
                val endX = borderRect.left
                val startX = startCoord.x - speed
                val startY = borderRect.top
                if (startX < endX) {
                    startCoord.set(endX, startY)
                    if (isEndRunning) middleCoords.remove(startCoord)
                    moveDir = MoveDir.TOP_TO_BOTTOM
                } else {
                    startCoord.set(startX, startY)
                }
            }

            MoveDir.TOP_TO_BOTTOM -> {
                val endY = height - borderRect.top
                val startY = startCoord.y + speed
                val startX = borderRect.left

                if (startY > endY) {
                    startCoord.set(startX, endY)
                    if (isEndRunning) middleCoords.remove(startCoord)
                    moveDir = MoveDir.RIGHT_TO_LEFT
                } else {
                    startCoord.set(startX, startY)
                }
            }

            MoveDir.RIGHT_TO_LEFT -> {
                val endX = width - borderRect.left
                val startY = height - borderRect.top
                val startX = startCoord.x + speed

                if (startX > endX) {
                    startCoord.set(endX, startY)
                    if (isEndRunning) middleCoords.remove(startCoord)
                    moveDir = MoveDir.BOTTOM_TO_TOP
                } else {
                    startCoord.set(startX, startY)
                }
            }

            MoveDir.BOTTOM_TO_TOP -> {
                val endY = borderRect.top
                val startY = startCoord.y - speed
                val startX = width - borderRect.left

                if (startY < endY) {
                    startCoord.set(startX, endY)
                    if (isEndRunning) middleCoords.remove(startCoord)
                    moveDir = MoveDir.LEFT_TO_RIGHT
                } else {
                    startCoord.set(startX, startY)
                }
            }
        }
    }

    /**
     * Calculate end coordinates
     */
    private fun updateEndCoord() {
        var endX = 0
        var endY = 0
        val limit: Int
        val listOfLimits = createListOfLimits(width, height, moveDir)
        middleCoords.clear()
        when (moveDir) {
            MoveDir.LEFT_TO_RIGHT -> {
                endX = startCoord.x - loaderLength
                endY = borderRect.top
                limit = borderRect.left

                if (endX < limit) {
                    hasCrossedEnd = true
                    val diff = limit - endX
                    val index = max(listOfLimits.indexOfFirst { it > diff }, 0)
                    calculateCoordsForLTR(diff, index, listOfLimits)
                    return
                }
            }

            MoveDir.TOP_TO_BOTTOM -> {
                endX = borderRect.left
                endY = startCoord.y + loaderLength
                limit = height - borderRect.top

                if (endY > limit) {
                    hasCrossedEnd = true
                    val diff = endY - limit
                    val index = max(listOfLimits.indexOfFirst { it > diff }, 0)
                    calculateCoordsForTTB(diff, index, listOfLimits)
                    return
                }
            }

            MoveDir.RIGHT_TO_LEFT -> {
                endX = startCoord.x + loaderLength
                endY = height - borderRect.top
                limit = width - borderRect.left

                if (endX > limit) {
                    hasCrossedEnd = true
                    val diff = endX - limit
                    val index = max(listOfLimits.indexOfFirst { it > diff }, 0)
                    calculateCoordsForRTL(diff, index, listOfLimits)
                    return
                }
            }

            MoveDir.BOTTOM_TO_TOP -> {
                endX = width - borderRect.left
                endY = startCoord.y - loaderLength
                limit = borderRect.top

                if (endY < limit) {
                    hasCrossedEnd = true
                    val diff = limit - endY
                    val index = max(listOfLimits.indexOfFirst { it > diff }, 0)
                    calculateCoordsForBTT(diff, index, listOfLimits)
                    return
                }
            }
        }
        hasCrossedEnd = false
        //Set the Middle coord and end coord if no condition matched in when branches
        middleCoords.add(Coord(endX, endY))
        endCoord.set(endX, endY)
    }

    override fun onStart() {
        isEndRunning = false
        updateStartCoord()
        updateEndCoord()
    }

    override fun onEnd(canvas: Canvas, paintSimple: Paint, paintBlur: Paint, snakeLoaderView: SnakeLoaderView, onEndAnimation: () -> Unit) {
        isEndRunning = true

        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = Long.MAX_VALUE
            interpolator = LinearInterpolator()

            addUpdateListener {
                if ((abs(startCoord.x - endCoord.x) <= speed / 2 && startCoord.y == endCoord.y)
                        || (abs(endCoord.y - startCoord.y) <= speed / 2 && startCoord.x == endCoord.x)) {
                    cancel()
                    return@addUpdateListener
                }
                updateStartCoord()
                snakeLoaderView.postInvalidate()
            }

            doOnEnd {
                // Hide the left out paint
                paintSimple.alpha = 0
                paintBlur.alpha = 0
                drawPaths(canvas,paintSimple,paintBlur)
                onEndAnimation.invoke()
            }
        }.start()
    }

    /**
     * Draw paths on canvas with [paintSimple] and [paintBlur]
     */
    private fun drawPaths(canvas: Canvas, paintSimple: Paint, paintBlur: Paint) {
        canvas.drawPath(path, paintSimple)
        canvas.drawPath(path, paintBlur)
    }


    /**
     * Calculate coordinates for BTT Direction
     */
    private fun calculateCoordsForBTT(diff: Int, index: Int, listOfLimits: ArrayList<Int>) {
        when (index) {
            0 -> endCoord.set(width - diff - borderRect.left, borderRect.top)
            1 -> endCoord.set(
                    borderRect.left,
                    height - (abs(diff) - listOfLimits[0]) - borderRect.top
            )
            2 -> endCoord.set(
                    borderRect.left + abs(diff) - listOfLimits[1],
                    height - borderRect.top
            )
        }
        calculateMiddleCoordsForBTT(index + 1)
    }

    private fun calculateMiddleCoordsForBTT(count: Int) {
        val coords = mutableListOf<Coord>()
        repeat(count) {
            when (it) {
                0 -> coords.add(Coord(width - borderRect.left, borderRect.top))
                1 -> coords.add(Coord(borderRect.left, borderRect.top))
                2 -> coords.add(Coord(borderRect.left, height - borderRect.top))
            }
        }
        middleCoords.addAll(coords)
    }

    /**
     * Calculate coordinates for RTL Direction
     */
    private fun calculateCoordsForRTL(diff: Int, index: Int, listOfLimits: ArrayList<Int>) {
        when (index) {
            0 -> endCoord.set(width - borderRect.left, height - diff)
            1 -> endCoord.set(
                    width - borderRect.left - (abs(diff) - listOfLimits[0]),
                    borderRect.top
            )
            2 -> endCoord.set(borderRect.left, height - (abs(diff) - listOfLimits[1]))
        }
        calculateMiddleCoordsForRTL(index + 1)
    }

    /**
     * Calculate Middle coords for RTL direction
     */
    private fun calculateMiddleCoordsForRTL(count: Int) {
        val coords = mutableListOf<Coord>()
        repeat(count) {
            when (it) {
                0 -> coords.add(Coord(width - borderRect.left, height - borderRect.top))
                1 -> coords.add(Coord(width - borderRect.left, borderRect.top))
                2 -> coords.add(Coord(borderRect.left, borderRect.top))
            }
        }
        middleCoords.addAll(coords)
    }

    /**
     * Calculate coordinates for Top to bottom direction
     */
    private fun calculateCoordsForTTB(diff: Int, index: Int, listOfLimits: ArrayList<Int>) {
        when (index) {
            0 -> endCoord.set(borderRect.left + diff, height - borderRect.top)
            1 -> endCoord.set(
                    width - borderRect.left,
                    height - (abs(diff) - listOfLimits[0]) - borderRect.top
            )
            2 -> endCoord.set(width - (abs(diff) - listOfLimits[0]), borderRect.top)
        }
        calculateMiddleCoordsForTTB(index + 1)
    }

    /**
     * Calculate Middle coordinates for Top to Bottom Direction
     */
    private fun calculateMiddleCoordsForTTB(count: Int) {
        val coords = mutableListOf<Coord>()
        repeat(count) {
            when (it) {
                0 -> coords.add(Coord(borderRect.left, height - borderRect.top))
                1 -> coords.add(Coord(width, height - borderRect.top))
                2 -> coords.add(Coord(width, borderRect.top))
            }
        }
        middleCoords.addAll(coords)
    }

    /**
     * Calculate end coordinates for LTR Direction
     */
    private fun calculateCoordsForLTR(diff: Int, index: Int, listOfLimits: ArrayList<Int>) {
        when (index) {
            0 -> endCoord.set(borderRect.left, borderRect.top + diff)
            1 -> endCoord.set(
                    borderRect.left + abs(diff) - listOfLimits[0],
                    height - borderRect.top
            )
            2 -> endCoord.set(width, height - abs(diff) - listOfLimits[1] - borderRect.top)
        }
        calculateMiddleCoordsForLTR(index + 1)
    }

    /**
     * Calculate Middle coords for LTR Direction
     *
     */
    private fun calculateMiddleCoordsForLTR(count: Int) {
        val coords = mutableListOf<Coord>()
        repeat(count) {
            when (it) {
                0 -> coords.add(Coord(borderRect.left, borderRect.top))
                1 -> coords.add(Coord(borderRect.left, height - borderRect.top))
                2 -> coords.add(Coord(width, height - borderRect.top))
            }
        }
        middleCoords.addAll(coords)
    }

    private fun createListOfLimits(width: Int, height: Int, moveDir: MoveDir): ArrayList<Int> {
        val list = arrayListOf<Int>()
        when (moveDir) {
            MoveDir.LEFT_TO_RIGHT, MoveDir.RIGHT_TO_LEFT -> {
                list.add(height)
                list.add(height + width)
                list.add(2 * height + width)
            }

            MoveDir.TOP_TO_BOTTOM, MoveDir.BOTTOM_TO_TOP -> {
                list.add(width)
                list.add(width + height)
                list.add(2 * width + height)
            }
        }
        return list
    }

    private enum class MoveDir {
        LEFT_TO_RIGHT, TOP_TO_BOTTOM, RIGHT_TO_LEFT, BOTTOM_TO_TOP
    }

    private data class Coord(var x: Int = 0, var y: Int = 0) {
        fun set(x: Int, y: Int) {
            this.x = x
            this.y = y
        }
    }
}