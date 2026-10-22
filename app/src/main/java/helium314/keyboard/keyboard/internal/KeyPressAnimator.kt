// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard.internal

import android.view.Choreographer
import helium314.keyboard.keyboard.Key

/**
 * Spring based press feedback for keys drawn on a canvas.
 *
 * For every key that is pressed or still settling there are two springs:
 *  - depth: 0 = resting, 1 = fully pressed; the key is drawn slightly smaller while pressed and
 *    bounces back on release
 *  - highlight: opacity of the pressed background; set immediately on press, so feedback is shown in the
 *    very first frame, and faded out on release
 *
 * The animation is purely visual: key events are handled by PointerTracker independently of it.
 * States are identified by key position, so an animation continues when the keyboard is replaced
 * by an equivalent one (e.g. shifted layout) while the key is pressed.
 * All springs are advanced in a single frame callback that only runs while something is moving.
 */
class KeyPressAnimator(private val invalidator: KeyInvalidator) {
    fun interface KeyInvalidator {
        fun invalidateKey(key: Key)
    }

    var enabled = true
        set(value) {
            field = value
            if (!value) clear()
        }

    private class KeyState(var key: Key) {
        val depth = Spring(SpringParams.FAST_EFFECTS.stiffness, SpringParams.FAST_EFFECTS.dampingRatio)
        val highlight = Spring(SpringParams.DEFAULT_EFFECTS.stiffness, SpringParams.DEFAULT_EFFECTS.dampingRatio)
        fun isAtRest() = depth.target == 0f && highlight.target == 0f && depth.isAtRest() && highlight.isAtRest()
    }

    private val states = HashMap<Long, KeyState>()
    private var frameScheduled = false
    private var lastFrameNanos = 0L

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos -> onFrame(frameTimeNanos) }

    fun press(key: Key) {
        if (!enabled) return
        val state = stateFor(key)
        state.depth.set(SpringParams.FAST_EFFECTS)
        state.depth.target = 1f
        state.highlight.snapTo(1f)
        invalidator.invalidateKey(key)
        scheduleFrame()
    }

    fun release(key: Key) {
        if (!enabled) return
        val state = states[positionId(key)] ?: return
        state.key = key
        state.depth.set(SpringParams.FAST_SPATIAL)
        state.depth.target = 0f
        state.highlight.target = 0f
        scheduleFrame()
    }

    /** short dip and bounce, e.g. to show a state change of the shift key */
    fun pulse(key: Key) {
        if (!enabled) return
        val state = stateFor(key)
        state.depth.set(SpringParams.FAST_SPATIAL)
        state.depth.velocity += PULSE_VELOCITY
        scheduleFrame()
    }

    /** how far the key is pressed in, 0 when resting (may be slightly negative when bouncing back) */
    fun getDepth(key: Key): Float = states[positionId(key)]?.depth?.value ?: 0f

    /** opacity of the pressed background while it fades out, 0 when there is no animation */
    fun getHighlight(key: Key): Float = states[positionId(key)]?.highlight?.value ?: 0f

    fun isAnimating(): Boolean = states.isNotEmpty()

    fun clear() {
        states.clear()
        if (frameScheduled) Choreographer.getInstance().removeFrameCallback(frameCallback)
        frameScheduled = false
    }

    private fun stateFor(key: Key): KeyState {
        val id = positionId(key)
        val state = states[id]
        if (state != null) {
            state.key = key
            return state
        }
        return KeyState(key).also { states[id] = it }
    }

    private fun scheduleFrame() {
        if (frameScheduled) return
        frameScheduled = true
        lastFrameNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun onFrame(frameTimeNanos: Long) {
        frameScheduled = false
        // first frame after a pause: assume one frame passed, so the press is visible immediately
        val seconds = if (lastFrameNanos == 0L) DEFAULT_FRAME_SECONDS else (frameTimeNanos - lastFrameNanos) / 1e9f
        lastFrameNanos = frameTimeNanos
        var moving = false
        val iterator = states.values.iterator()
        while (iterator.hasNext()) {
            val state = iterator.next()
            // keys that are held down and fully pressed don't need any frames
            if (state.depth.isAtRest() && state.highlight.isAtRest() && state.depth.target != 0f) continue
            state.depth.step(seconds)
            state.highlight.step(seconds)
            invalidator.invalidateKey(state.key)
            if (state.isAtRest()) iterator.remove()
            else moving = true
        }
        if (moving) {
            frameScheduled = true
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    companion object {
        private const val DEFAULT_FRAME_SECONDS = 1 / 60f
        private const val PULSE_VELOCITY = 14f

        private fun positionId(key: Key): Long = (key.x.toLong() shl 32) or (key.y.toLong() and 0xffffffffL)
    }
}
