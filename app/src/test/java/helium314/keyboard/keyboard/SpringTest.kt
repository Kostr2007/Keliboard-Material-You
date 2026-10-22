// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import helium314.keyboard.keyboard.internal.Spring
import helium314.keyboard.keyboard.internal.SpringInterpolator
import helium314.keyboard.keyboard.internal.SpringParams
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpringTest {
    private fun spring(params: SpringParams, from: Float, to: Float) =
        Spring(params.stiffness, params.dampingRatio).apply { snapTo(from); target = to }

    @Test fun `critically damped spring reaches target without overshoot`() {
        val s = spring(SpringParams.FAST_EFFECTS, 0f, 1f)
        var max = 0f
        repeat(60) {
            s.step(1 / 120f)
            max = maxOf(max, s.value)
        }
        assertTrue(max <= 1f, "overshoot: $max")
        assertEquals(1f, s.value)
        assertTrue(s.isAtRest())
    }

    @Test fun `press feedback is visible in the first frame`() {
        val s = spring(SpringParams.FAST_EFFECTS, 0f, 1f)
        s.step(1 / 60f)
        assertTrue(s.value > 0.15f, "value after one frame: ${s.value}")
    }

    @Test fun `bouncy spring overshoots a little and settles`() {
        val s = spring(SpringParams.FAST_SPATIAL, 1f, 0f)
        var min = 1f
        repeat(120) {
            s.step(1 / 120f)
            min = minOf(min, s.value)
        }
        // damping ratio 0.6 overshoots by about 9.5 %
        assertTrue(min < -0.05f && min > -0.15f, "overshoot: $min")
        assertTrue(s.isAtRest())
        assertEquals(0f, s.value)
    }

    @Test fun `result does not depend on the frame rate`() {
        val slow = spring(SpringParams.FAST_SPATIAL, 1f, 0f)
        val fast = spring(SpringParams.FAST_SPATIAL, 1f, 0f)
        repeat(6) { slow.step(1 / 60f) }
        repeat(12) { fast.step(1 / 120f) }
        assertTrue(abs(slow.value - fast.value) < 1e-4f, "${slow.value} vs ${fast.value}")
        assertTrue(abs(slow.velocity - fast.velocity) < 1e-2f, "${slow.velocity} vs ${fast.velocity}")
    }

    @Test fun `retargeting keeps position and velocity`() {
        // fast taps: release starts while the press animation is still running
        val s = spring(SpringParams.FAST_EFFECTS, 0f, 1f)
        repeat(3) { s.step(1 / 120f) }
        val value = s.value
        val velocity = s.velocity
        s.set(SpringParams.FAST_SPATIAL)
        s.target = 0f
        assertEquals(value, s.value)
        assertEquals(velocity, s.velocity)
        s.step(1 / 120f)
        assertTrue(abs(s.value - value) < 0.1f, "jump from $value to ${s.value}")
    }

    @Test fun `long pauses do not make the spring jump`() {
        val s = spring(SpringParams.FAST_SPATIAL, 1f, 0f)
        s.step(5f)
        // the step is clamped, so the spring is still moving after a stalled frame
        assertTrue(s.value != 0f)
    }

    @Test fun `interpolator starts at 0 and ends at 1`() {
        for (params in listOf(SpringParams.FAST_EFFECTS, SpringParams.FAST_SPATIAL, SpringParams.DEFAULT_EFFECTS)) {
            val interpolator = SpringInterpolator(params)
            assertEquals(0f, interpolator.getInterpolation(0f))
            assertEquals(1f, interpolator.getInterpolation(1f))
            assertTrue(abs(interpolator.getInterpolation(0.99f) - 1f) < 0.01f)
            assertTrue(interpolator.durationMillis in 80L..600L)
        }
    }
}
