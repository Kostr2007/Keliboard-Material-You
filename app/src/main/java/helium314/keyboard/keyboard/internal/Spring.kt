// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard.internal

import android.animation.TimeInterpolator
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Damped spring with unit mass, as used by the Material 3 motion system.
 * [step] solves the spring equation analytically, so the result does not depend on the frame rate
 * and a spring can be retargeted at any time while keeping its velocity (no "stuck" animations
 * when keys are tapped quickly).
 */
class Spring(var stiffness: Float, var dampingRatio: Float) {
    var value = 0f
    var velocity = 0f
    var target = 0f

    fun set(params: SpringParams) {
        stiffness = params.stiffness
        dampingRatio = params.dampingRatio
    }

    fun snapTo(newValue: Float) {
        value = newValue
        target = newValue
        velocity = 0f
    }

    fun isAtRest(): Boolean = abs(value - target) < VALUE_THRESHOLD && abs(velocity) < VELOCITY_THRESHOLD

    /** advance the spring by [seconds], clamped so a stalled frame does not make the spring jump */
    fun step(seconds: Float) {
        val t = min(max(seconds, 0f), MAX_STEP_SECONDS)
        if (t == 0f) return
        val (x, v) = solve(value - target, velocity, t, stiffness, dampingRatio)
        value = x + target
        velocity = v
        if (isAtRest()) {
            value = target
            velocity = 0f
        }
    }

    companion object {
        const val VALUE_THRESHOLD = 0.001f
        const val VELOCITY_THRESHOLD = 0.01f
        private const val MAX_STEP_SECONDS = 0.064f

        /**
         * Displacement and velocity of a spring after [t] seconds, starting at displacement [x0]
         * from its rest position with velocity [v0].
         */
        fun solve(x0: Float, v0: Float, t: Float, stiffness: Float, dampingRatio: Float): Pair<Float, Float> {
            val w0 = sqrt(stiffness.toDouble())
            val z = dampingRatio.toDouble()
            val x = x0.toDouble()
            val v = v0.toDouble()
            val time = t.toDouble()
            return when {
                z > 1.0 -> { // overdamped
                    val root = w0 * sqrt(z * z - 1)
                    val gammaPlus = -z * w0 + root
                    val gammaMinus = -z * w0 - root
                    val b = (gammaMinus * x - v) / (gammaMinus - gammaPlus)
                    val a = x - b
                    val ePlus = exp(gammaPlus * time)
                    val eMinus = exp(gammaMinus * time)
                    (a * eMinus + b * ePlus).toFloat() to (a * gammaMinus * eMinus + b * gammaPlus * ePlus).toFloat()
                }
                z == 1.0 -> { // critically damped
                    val a = x
                    val b = v + w0 * x
                    val e = exp(-w0 * time)
                    ((a + b * time) * e).toFloat() to ((a + b * time) * e * -w0 + b * e).toFloat()
                }
                else -> { // underdamped, bounces around the rest position
                    val wd = w0 * sqrt(1 - z * z)
                    val sinCoefficient = (z * w0 * x + v) / wd
                    val e = exp(-z * w0 * time)
                    val c = cos(wd * time)
                    val s = sin(wd * time)
                    val position = e * (x * c + sinCoefficient * s)
                    val velocity = position * -z * w0 + e * (-wd * x * s + wd * sinCoefficient * c)
                    position.toFloat() to velocity.toFloat()
                }
            }
        }
    }
}

/** Spring parameters, see the motion tokens of Material 3 Expressive. */
data class SpringParams(val stiffness: Float, val dampingRatio: Float) {
    /** approximate time until the spring has settled when moving from 0 to 1 */
    val settleSeconds: Float get() {
        val decay = dampingRatio * sqrt(stiffness)
        // for the critically damped case the polynomial factor delays settling a bit
        return (ln(1f / Spring.VALUE_THRESHOLD) / decay) * (if (dampingRatio >= 1f) 1.3f else 1f)
    }

    companion object {
        /** quick and without overshoot, for things that should follow the finger (key press) */
        @JvmField val FAST_EFFECTS = SpringParams(3800f, 1f)
        /** small, visible bounce for spatial changes (key release, popups) */
        @JvmField val FAST_SPATIAL = SpringParams(800f, 0.6f)
        /** calmer bounce for larger elements */
        @JvmField val DEFAULT_SPATIAL = SpringParams(380f, 0.8f)
        /** smooth fade without overshoot (colors, opacity) */
        @JvmField val DEFAULT_EFFECTS = SpringParams(1600f, 1f)
    }
}

/**
 * Spring as [TimeInterpolator] for View animations (ViewPropertyAnimator etc.), going from 0 to 1.
 * Use [durationMillis] as animation duration, so the whole spring motion is played.
 */
class SpringInterpolator(private val params: SpringParams) : TimeInterpolator {
    val durationMillis: Long = (params.settleSeconds * 1000).toLong().coerceIn(80, 600)

    override fun getInterpolation(input: Float): Float {
        if (input >= 1f) return 1f
        val (x, _) = Spring.solve(-1f, 0f, input * durationMillis / 1000f, params.stiffness, params.dampingRatio)
        return 1f + x
    }
}
