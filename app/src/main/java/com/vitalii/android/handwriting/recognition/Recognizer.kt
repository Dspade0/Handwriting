package com.vitalii.android.handwriting.recognition

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

class Recognizer(val pointNum: Int = 32,
                 val runsParam: Double = 0.5) {

    private var templates = ArrayList<PointCloud>()

    fun addTemplate(name: String, points: ArrayList<StrokePoint>) {
        val normalizedPoints = normalize(points, pointNum)
        val template = PointCloud(name, normalizedPoints)
        templates.add(template)
    }

    fun setTemplates(templates: Array<PointCloud>) {
        this.templates = ArrayList(templates.toMutableList())
    }

    fun recognize(points: ArrayList<StrokePoint>): String {
        var scores = Float.POSITIVE_INFINITY
        var result = "No matching"
        val cloud = normalize(points, pointNum)
        if (cloud.size != pointNum) return "No matching"
        templates.forEach { template ->
            val value = greedyCloudMatch(cloud, template)
            if (value < scores) {
                scores = value
                result = template.name
            }
        }
        return "$result : $scores"
    }

    private fun greedyCloudMatch(cloud: ArrayList<StrokePoint>,
                                 template: PointCloud): Float {
        val step = floor(pointNum.toDouble().pow(runsParam)).toInt()
        var minimum = Float.POSITIVE_INFINITY
        for (i in 0 until pointNum step step) {
            val d1 = cloudDistance(cloud, template.points, i)
            val d2 = cloudDistance(template.points, cloud, i)
            minimum = min(min(d1, d2), minimum)
        }
        return minimum
    }

    private fun cloudDistance(cloud: ArrayList<StrokePoint>,
                              template: ArrayList<StrokePoint>,
                              startIndex: Int): Float {
        val matched = Array(pointNum, { false })
        var result = 0f
        var i = startIndex
        do {
            var min = Float.POSITIVE_INFINITY
            var minIndex = -1
            for (j in 0 until pointNum) {
                if (matched[j]) continue
                val d = euclideanDistance(cloud[i], template[j])
                if (d < min) {
                    min = d
                    minIndex = j
                }
            }
            if (minIndex != -1 ) {
                matched[minIndex] = true
                val weight = 1 - ((i - startIndex) % pointNum) / pointNum
                result += weight * min
            }
            i = (i + 1) % pointNum
        } while (i != startIndex)
        return result
    }
}