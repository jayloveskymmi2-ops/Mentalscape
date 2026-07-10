package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float)

data class Line3D(val p1: Point3D, val p2: Point3D, val color: Color = Color.Cyan, val strokeWidth: Float = 2f)

data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    val size: Float,
    val color: Color,
    val speed: Float
)

object Head3DModel {
    // Generate a 3D cybernetic wireframe human head mesh
    val headLines: List<Line3D> by lazy {
        val lines = mutableListOf<Line3D>()
        val cyan = Color(0xFF00F0FF)
        val magenta = Color(0xFFFF007A)
        val blue = Color(0xFF3B82F6)

        // Scalp & Crown rings
        for (y in -180..180 step 30) {
            val radius = when {
                y < -120 -> 80f * sin(((y + 180) / 60f) * Math.PI.toFloat() / 2)
                y < 0 -> 110f - (y * y / 300f)
                y < 80 -> 100f - (y * 0.4f)
                else -> 80f - ((y - 80) * 1.2f)
            }.coerceAtLeast(10f)

            var prevP: Point3D? = null
            var firstP: Point3D? = null
            for (angleDeg in 0..360 step 30) {
                val rad = Math.toRadians(angleDeg.toDouble())
                val p = Point3D(
                    x = (radius * cos(rad)).toFloat(),
                    y = y.toFloat(),
                    z = (radius * sin(rad)).toFloat()
                )
                if (prevP != null) {
                    val col = if (y in -30..30) cyan else blue
                    lines.add(Line3D(prevP, p, col, if (y in -30..30) 2.5f else 1.5f))
                } else {
                    firstP = p
                }
                prevP = p
            }
            if (prevP != null && firstP != null) {
                lines.add(Line3D(prevP, firstP, blue, 1.5f))
            }
        }

        // Vertical contours (front face, jaw, cheekbones)
        for (angleDeg in 0 until 360 step 30) {
            val rad = Math.toRadians(angleDeg.toDouble())
            var prevP: Point3D? = null
            for (y in -180..180 step 30) {
                val radius = when {
                    y < -120 -> 80f * sin(((y + 180) / 60f) * Math.PI.toFloat() / 2)
                    y < 0 -> 110f - (y * y / 300f)
                    y < 80 -> 100f - (y * 0.4f)
                    else -> 80f - ((y - 80) * 1.2f)
                }.coerceAtLeast(10f)

                val p = Point3D(
                    x = (radius * cos(rad)).toFloat(),
                    y = y.toFloat(),
                    z = (radius * sin(rad)).toFloat()
                )
                if (prevP != null) {
                    lines.add(Line3D(prevP, p, if (angleDeg in 60..120) magenta else cyan, 1.2f))
                }
                prevP = p
            }
        }

        // Eye details (Left & Right eye sockets)
        fun addEye(centerX: Float, centerY: Float, centerZ: Float) {
            val eyeRadius = 18f
            var prev: Point3D? = null
            var first: Point3D? = null
            for (deg in 0..360 step 20) {
                val rad = Math.toRadians(deg.toDouble())
                val p = Point3D(
                    x = centerX + (eyeRadius * cos(rad)).toFloat(),
                    y = centerY + (eyeRadius * sin(rad)).toFloat(),
                    z = centerZ + 12f
                )
                if (prev != null) {
                    lines.add(Line3D(prev, p, Color(0xFF00FFCC), 3f))
                } else {
                    first = p
                }
                prev = p
            }
            if (prev != null && first != null) lines.add(Line3D(prev, first, Color(0xFF00FFCC), 3f))

            // Pupil inner ring
            var pPrev: Point3D? = null
            var pFirst: Point3D? = null
            for (deg in 0..360 step 40) {
                val rad = Math.toRadians(deg.toDouble())
                val p = Point3D(
                    x = centerX + (7f * cos(rad)).toFloat(),
                    y = centerY + (7f * sin(rad)).toFloat(),
                    z = centerZ + 18f
                )
                if (pPrev != null) {
                    lines.add(Line3D(pPrev, p, Color.White, 3.5f))
                } else {
                    pFirst = p
                }
                pPrev = p
            }
            if (pPrev != null && pFirst != null) lines.add(Line3D(pPrev, pFirst, Color.White, 3.5f))
        }

        addEye(centerX = -38f, centerY = 10f, centerZ = 85f) // Right eye socket
        addEye(centerX = 38f, centerY = 10f, centerZ = 85f)  // Left eye socket

        // Nose bridge
        val nTop = Point3D(0f, -10f, 95f)
        val nMid = Point3D(0f, 25f, 115f)
        val nBaseL = Point3D(-15f, 40f, 95f)
        val nBaseR = Point3D(15f, 40f, 95f)
        lines.add(Line3D(nTop, nMid, magenta, 2.5f))
        lines.add(Line3D(nMid, nBaseL, magenta, 2f))
        lines.add(Line3D(nMid, nBaseR, magenta, 2f))

        // Cybernetic Mouth outline
        val mL = Point3D(-28f, 75f, 82f)
        val mR = Point3D(28f, 75f, 82f)
        val mC = Point3D(0f, 78f, 90f)
        lines.add(Line3D(mL, mC, cyan, 2f))
        lines.add(Line3D(mC, mR, cyan, 2f))

        lines
    }

    // Project a 3D point into 2D screen space
    fun project(
        point: Point3D,
        rotXRad: Float,
        rotYRad: Float,
        rotZRad: Float,
        camDist: Float,
        screenWidth: Float,
        screenHeight: Float
    ): Offset {
        // Rotate Y (Yaw)
        val x1 = point.x * cos(rotYRad) + point.z * sin(rotYRad)
        val y1 = point.y
        val z1 = -point.x * sin(rotYRad) + point.z * cos(rotYRad)

        // Rotate X (Pitch)
        val x2 = x1
        val y2 = y1 * cos(rotXRad) - z1 * sin(rotXRad)
        val z2 = y1 * sin(rotXRad) + z1 * cos(rotXRad)

        // Rotate Z (Roll)
        val x3 = x2 * cos(rotZRad) - y2 * sin(rotZRad)
        val y3 = x2 * sin(rotZRad) + y2 * cos(rotZRad)
        val z3 = z2

        // Perspective factor
        val fov = 650f
        val distance = camDist + z3
        val factor = if (distance > 1f) fov / distance else fov

        val screenX = screenWidth / 2f + (x3 * factor)
        val screenY = screenHeight / 2f + (y3 * factor)

        return Offset(screenX, screenY)
    }
}
