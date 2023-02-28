/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package tachyon.commons.collection

import java.util.Random

/**
 * Hashing utilities.
 *
 * Little endian implementations of Murmur3 hashing.
 */
class Hashing private constructor() {
    /**
     * Holds references to things that can't be initialized until after VM is fully booted.
     */
    private object Holder {
        /**
         * Used for generating per-instance hash seeds.
         *
         * We try to improve upon the default seeding.
         */
        val SEED_MAKER: Random = Random(Double.doubleToRawLongBits(ThreadLocalRandom.current().nextDouble()) xor System.identityHashCode(Hashing::class.java) xor System.currentTimeMillis()
                xor System.nanoTime() xor Runtime.getRuntime().freeMemory())
        /**
         * Access to `String.hash32()`
         *
         * static final JavaLangAccess LANG_ACCESS;
         *
         * static { LANG_ACCESS = SharedSecrets.getJavaLangAccess(); if (null == LANG_ACCESS) { throw new
         * Error("Shared secrets not initialized"); } }
         */
    }

    companion object {
        fun murmur3_32(data: ByteArray): Int {
            return murmur3_32(0, data, 0, data.size)
        }

        fun murmur3_32(seed: Int, data: ByteArray): Int {
            return murmur3_32(seed, data, 0, data.size)
        }

        @SuppressWarnings("fallthrough")
        fun murmur3_32(seed: Int, data: ByteArray, offset: Int, len: Int): Int {
            var offset = offset
            var h1 = seed
            var count = len

            // body
            while (count >= 4) {
                var k1: Int = data[offset] and 0x0FF or (data[offset + 1] and 0x0FF shl 8) or (data[offset + 2] and 0x0FF shl 16) or (data[offset + 3] shl 24)
                count -= 4
                offset += 4
                k1 *= -0x3361d2af
                k1 = Integer.rotateLeft(k1, 15)
                k1 *= 0x1b873593
                h1 = h1 xor k1
                h1 = Integer.rotateLeft(h1, 13)
                h1 = h1 * 5 + -0x19ab949c
            }

            // tail
            if (count > 0) {
                var k1 = 0
                when (count) {
                    3 -> {
                        k1 = k1 xor (data[offset + 2] and 0xff shl 16)
                        k1 = k1 xor (data[offset + 1] and 0xff shl 8)
                        k1 = k1 xor (data[offset] and 0xff)
                        k1 *= -0x3361d2af
                        k1 = Integer.rotateLeft(k1, 15)
                        k1 *= 0x1b873593
                        h1 = h1 xor k1
                    }
                    2 -> {
                        k1 = k1 xor (data[offset + 1] and 0xff shl 8)
                        k1 = k1 xor (data[offset] and 0xff)
                        k1 *= -0x3361d2af
                        k1 = Integer.rotateLeft(k1, 15)
                        k1 *= 0x1b873593
                        h1 = h1 xor k1
                    }
                    1 -> {
                        k1 = k1 xor (data[offset] and 0xff)
                        k1 *= -0x3361d2af
                        k1 = Integer.rotateLeft(k1, 15)
                        k1 *= 0x1b873593
                        h1 = h1 xor k1
                    }
                    else -> {
                        k1 *= -0x3361d2af
                        k1 = Integer.rotateLeft(k1, 15)
                        k1 *= 0x1b873593
                        h1 = h1 xor k1
                    }
                }
            }

            // finalization
            h1 = h1 xor len

            // finalization mix force all bits of a hash block to avalanche
            h1 = h1 xor (h1 ushr 16)
            h1 *= -0x7a143595
            h1 = h1 xor (h1 ushr 13)
            h1 *= -0x3d4d51cb
            h1 = h1 xor (h1 ushr 16)
            return h1
        }

        fun murmur3_32(data: CharArray): Int {
            return murmur3_32(0, data, 0, data.size)
        }

        @JvmOverloads
        fun murmur3_32(seed: Int, data: CharArray, offset: Int = 0, len: Int = data.size): Int {
            var h1 = seed
            var off = offset
            var count = len

            // body
            while (count >= 2) {
                var k1: Int = data[off++] and 0xFFFF or (data[off++] shl 16)
                count -= 2
                k1 *= -0x3361d2af
                k1 = Integer.rotateLeft(k1, 15)
                k1 *= 0x1b873593
                h1 = h1 xor k1
                h1 = Integer.rotateLeft(h1, 13)
                h1 = h1 * 5 + -0x19ab949c
            }

            // tail
            if (count > 0) {
                var k1 = data[off].toInt()
                k1 *= -0x3361d2af
                k1 = Integer.rotateLeft(k1, 15)
                k1 *= 0x1b873593
                h1 = h1 xor k1
            }

            // finalization
            h1 = h1 xor len * (Character.SIZE / Byte.SIZE)

            // finalization mix force all bits of a hash block to avalanche
            h1 = h1 xor (h1 ushr 16)
            h1 *= -0x7a143595
            h1 = h1 xor (h1 ushr 13)
            h1 *= -0x3d4d51cb
            h1 = h1 xor (h1 ushr 16)
            return h1
        }

        fun murmur3_32(data: IntArray): Int {
            return murmur3_32(0, data, 0, data.size)
        }

        @JvmOverloads
        fun murmur3_32(seed: Int, data: IntArray, offset: Int = 0, len: Int = data.size): Int {
            var h1 = seed
            var off = offset
            val end = offset + len

            // body
            while (off < end) {
                var k1 = data[off++]
                k1 *= -0x3361d2af
                k1 = Integer.rotateLeft(k1, 15)
                k1 *= 0x1b873593
                h1 = h1 xor k1
                h1 = Integer.rotateLeft(h1, 13)
                h1 = h1 * 5 + -0x19ab949c
            }

            // tail (always empty, as body is always 32-bit chunks)

            // finalization
            h1 = h1 xor len * (Integer.SIZE / Byte.SIZE)

            // finalization mix force all bits of a hash block to avalanche
            h1 = h1 xor (h1 ushr 16)
            h1 *= -0x7a143595
            h1 = h1 xor (h1 ushr 13)
            h1 *= -0x3d4d51cb
            h1 = h1 xor (h1 ushr 16)
            return h1
        }

        fun randomHashSeed(instance: Object?): Int {
            val seed: Int
            seed = if (SystemUtil.isBooted()) {
                Holder.SEED_MAKER.nextInt()
            } else {
                // lower quality "random" seed value--still better than zero and not
                // not practically reversible.
                val hashing_seed = intArrayOf(System.identityHashCode(Hashing::class.java), System.identityHashCode(instance), System.identityHashCode(Thread.currentThread()),
                        Thread.currentThread().getId() as Int, (System.currentTimeMillis() ushr 2) as Int,  // resolution is poor
                        (System.nanoTime() ushr 5) as Int,  // resolution is poor
                        (Runtime.getRuntime().freeMemory() ushr 4) as Int // alloc min
                )
                murmur3_32(hashing_seed)
            }

            // force to non-zero.
            return if (0 != seed) seed else 1
        }
    }

    /**
     * Static utility methods only.
     */
    init {
        throw Error("No instances")
    }
}