package com.vitalii.android.handwriting.recognition

import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

fun normalize(points: ArrayList<StrokePoint>, toNumPoints: Int): ArrayList<StrokePoint> =
    resample(points, toNumPoints).let { scale(it) }

fun resample(points: ArrayList<StrokePoint>, toNumPoints: Int): ArrayList<StrokePoint> {
    if (points.isEmpty()) return points
    val pts = ArrayList<StrokePoint>(points)
    val interval = pathLength(pts) / (toNumPoints - 1)
    if (interval == 0f) return points
    var restDistance = 0f
    val newPoints = ArrayList<StrokePoint>().apply { add(pts.first()) }
    var i = 1
    while (i < pts.size) {
        val p1 = pts[i - 1]
        val p2 = pts[i]
        if (p1.strokeId == p2.strokeId) {
            val d = euclideanDistance(p1, p2)
            if (restDistance + d >= interval) {
                val newPoint = StrokePoint().apply {
                    x = p1.x + (interval - restDistance) / d * (p2.x - p1.x)
                    y = p1.y + (interval - restDistance) / d * (p2.y - p1.y)
                    strokeId = p2.strokeId
                }
                newPoints.add(newPoint)
                pts.add(i, newPoint)
                restDistance = 0f
            } else {
                restDistance += d
            }
        }
        ++i
    }
    if (newPoints.size == toNumPoints - 1) newPoints.add(pts.last())
    return newPoints
}

fun scale(points: ArrayList<StrokePoint>) : ArrayList<StrokePoint> {
    if (points.isEmpty()) return points

    val xMin = points.minBy { it.x }!!.x
    val yMin = points.minBy { it.y }!!.y
    val xMax = points.maxBy { it.x }!!.x
    val yMax = points.maxBy { it.y }!!.y
    val scaleX = xMax - xMin
    val scaleY = yMax - yMin

    return points.map {
        val x = if (scaleX < 100) (it.x - xMin) / max(scaleX, scaleY)
        else (it.x - xMin) / scaleX * 2 / 3

        val y = (it.y - yMin) / scaleY
        StrokePoint(x, y, it.strokeId)
    } as ArrayList<StrokePoint>
}

fun euclideanDistance(p1: StrokePoint, p2: StrokePoint) =
        sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))

fun pathLength(points: ArrayList<StrokePoint>): Float {
    if (points.size < 2) return 0f
    var d = 0f
    for (i in 1 until points.size) {
        if (points[i - 1].strokeId == points[i].strokeId) {
            d += euclideanDistance(points[i - 1], points[i])
        }
    }
    return d
}
