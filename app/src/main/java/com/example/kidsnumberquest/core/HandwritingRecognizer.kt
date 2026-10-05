package com.example.kidsnumberquest.core

import android.graphics.Path
import kotlin.math.hypot
import kotlin.math.min

data class StrokePoint(val x: Float, val y: Float)
data class Recognition(val value: String?, val confidence: Float)

class HandwritingRecognizer {
    fun recognize(strokes: List<List<StrokePoint>>): Recognition {
        if (strokes.isEmpty() || strokes.sumOf { it.size } < 3) return Recognition(null, 0f)
        val bounds = strokes.flatten(); val minX=bounds.minOf{it.x}; val maxX=bounds.maxOf{it.x}; val width=(maxX-minX).coerceAtLeast(1f)
        val groups = segment(strokes, width)
        val values = groups.map { classify(it) }
        if (values.any { it.value == null }) return Recognition(null, values.map{it.confidence}.average().toFloat())
        val value=values.joinToString(""){it.value!!}
        if (value.toIntOrNull() !in 1..15 && value !in listOf("<", ">")) return Recognition(null, 0.35f)
        return Recognition(value, values.map{it.confidence}.average().toFloat())
    }
    private fun segment(strokes: List<List<StrokePoint>>, width: Float): List<List<StrokePoint>> {
        val all=strokes.flatten(); val mid=all.map{it.x}.average().toFloat()
        val left=strokes.filter{it.map{p->p.x}.average()<mid}.flatten(); val right=strokes.filter{it.map{p->p.x}.average()>=mid}.flatten()
        return if(left.isNotEmpty()&&right.isNotEmpty()&&width>55f) listOf(left,right) else listOf(all)
    }
    private fun classify(points: List<StrokePoint>): Recognition {
        val minX=points.minOf{it.x}; val maxX=points.maxOf{it.x}; val minY=points.minOf{it.y}; val maxY=points.maxOf{it.y}; val w=maxX-minX; val h=maxY-minY
        val dx=points.last().x-points.first().x; val dy=points.last().y-points.first().y
        val straight=hypot(dx.toDouble(),dy.toDouble()).toFloat()/(hypot(w.toDouble(),h.toDouble()).toFloat().coerceAtLeast(1f))
        if(w>h*1.2f && points.size<60) return Recognition(if(dx>0) ">" else "<", .72f)
        val aspect=w/h.coerceAtLeast(1f)
        val top=points.first(); val end=points.last()
        val closed=hypot((end.x-top.x).toDouble(),(end.y-top.y).toDouble()) < min(w,h)*.25
        val value=when {
            closed && aspect in .65f..1.35f -> "0"
            aspect<.35f && !closed -> "1"
            aspect in .45f..1.8f && dy>h*.25f -> "2"
            aspect in .45f..1.8f && dx>0 && dy>0 -> "3"
            aspect in .35f..1.2f && !closed && straight>.8f -> "4"
            aspect in .45f..1.8f && dy>0 && dx<0 -> "5"
            closed && aspect in .5f..1.5f -> "6"
            aspect>.8f && dy>h*.25f && dx>0 -> "7"
            closed -> "8"
            aspect in .5f..1.6f && dy<0 -> "9"
            else -> null
        }
        return Recognition(value, if(value==null) .25f else .60f)
    }
}
