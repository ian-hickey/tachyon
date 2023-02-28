/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.sql.old

import java.io.IOException

// Referenced classes of package Zql:
//            TokenMgrError, ZqlJJParserConstants, SimpleCharStream, Token
class ZqlJJParserTokenManager(simplecharstream: SimpleCharStream?) : ZqlJJParserConstants {
    fun setDebugStream(printstream: PrintStream?) {
        debugStream = printstream
    }

    private fun jjStopStringLiteralDfa_0(i: Int, l: Long, l1: Long): Int {
        when (i) {
            0 -> {
                if (l1 and 0x4000000000L != 0L) return 0
                if (l1 and 0x10020000000L != 0L) return 47
                if (l1 and 0x80000000000L != 0L) return 3
                if (l and -32L != 0L || l1 and 4095L != 0L) {
                    jjmatchedKind = 82
                    return 48
                }
                // else{
                return -1
            }
            1 -> {
                if (l and 0x1a003f00004300L != 0L) return 48
                if (l and -0x1a003f00004320L != 0L || l1 and 4095L != 0L) {
                    if (jjmatchedPos != 1) {
                        jjmatchedKind = 82
                        jjmatchedPos = 1
                    }
                    return 48
                }
                // else{
                return -1
            }
            2 -> {
                if (l and -0x140a272110004800L != 0L || l1 and 4094L != 0L) {
                    if (jjmatchedPos != 2) {
                        jjmatchedKind = 82
                        jjmatchedPos = 2
                    }
                    return 48
                }
                return if (l and 0x14002700100006e0L == 0L && l1 and 1L == 0L) -1 else 48
            }
            3 -> {
                if (l and 0x1c488d024508000L != 0L || l1 and 1536L != 0L) return 48
                if (l and -0x15ceabf13450c800L != 0L || l1 and 2558L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 3
                    return 48
                }
                // else{
                return -1
            }
            4 -> {
                if (l and -0x5fcffbffb7f80000L != 0L || l1 and 2314L != 0L) return 48
                if (l and 0x4a01500e83a73800L != 0L || l1 and 244L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 4
                    return 48
                }
                // else{
                return -1
            }
            5 -> {
                if (l and 0x4200100c01853800L != 0L || l1 and 196L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 5
                    return 48
                }
                return if (l and 0x801400282220000L == 0L && l1 and 48L == 0L) -1 else 48
            }
            6 -> {
                if (l and 4096L != 0L) {
                    if (jjmatchedPos != 6) {
                        jjmatchedKind = 82
                        jjmatchedPos = 6
                    }
                    return 11
                }
                if (l and 0x100400052800L != 0L || l1 and 192L != 0L) return 48
                if (l and 0x4200000801800000L != 0L || l1 and 4L != 0L) {
                    if (jjmatchedPos != 6) {
                        jjmatchedKind = 82
                        jjmatchedPos = 6
                    }
                    return 48
                }
                // else{
                return -1
            }
            7 -> {
                if (l and 0x4200000000800000L != 0L) return 48
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 7
                    return 11
                }
                if (l1 and 64L != 0L) return 11
                if (l and 0x801000000L != 0L || l1 and 4L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 7
                    return 48
                }
                // else{
                return -1
            }
            8 -> {
                if (l1 and 4L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 8
                    return 48
                }
                if (l and 0x801000000L != 0L) return 48
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 8
                    return 11
                }
                // else{
                return -1
            }
            9 -> {
                if (l1 and 4L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 9
                    return 48
                }
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 9
                    return 11
                }
                // else{
                return -1
            }
            10 -> {
                if (l1 and 4L != 0L) return 48
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 10
                    return 11
                }
                // else{
                return -1
            }
            11 -> {
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 11
                    return 11
                }
                // else{
                return -1
            }
            12 -> {
                if (l and 4096L != 0L) {
                    jjmatchedKind = 82
                    jjmatchedPos = 12
                    return 11
                }
                // else{
                return -1
            }
        }
        return -1
    }

    private fun jjStartNfa_0(i: Int, l: Long, l1: Long): Int {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(i, l, l1), i + 1)
    }

    private fun jjStopAtPos(i: Int, j: Int): Int {
        jjmatchedKind = j
        jjmatchedPos = i
        return i + 1
    }

    private fun jjStartNfaWithStates_0(i: Int, j: Int, k: Int): Int {
        jjmatchedKind = j
        jjmatchedPos = i
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            return i + 1
        }
        return jjMoveNfa_0(k, i + 1)
    }

    private fun jjMoveStringLiteralDfa0_0(): Int {
        return when (curChar) {
            33 -> jjMoveStringLiteralDfa1_0(0L, 0x40000000L)
            35 -> jjStopAtPos(0, 95)
            40 -> jjStopAtPos(0, 88)
            41 -> jjStopAtPos(0, 90)
            42 -> {
                jjmatchedKind = 103
                jjMoveStringLiteralDfa1_0(0L, 0x100000000000L)
            }
            43 -> jjStopAtPos(0, 101)
            44 -> jjStopAtPos(0, 89)
            45 -> jjStartNfaWithStates_0(0, 102, 0)
            46 -> {
                jjmatchedKind = 93
                jjMoveStringLiteralDfa1_0(0L, 0x10000000000L)
            }
            47 -> jjStartNfaWithStates_0(0, 107, 3)
            59 -> jjStopAtPos(0, 91)
            60 -> {
                jjmatchedKind = 99
                jjMoveStringLiteralDfa1_0(0L, 0x1100000000L)
            }
            61 -> jjStopAtPos(0, 92)
            62 -> {
                jjmatchedKind = 97
                jjMoveStringLiteralDfa1_0(0L, 0x400000000L)
            }
            63 -> jjStopAtPos(0, 105)
            65, 97 -> jjMoveStringLiteralDfa1_0(2016L, 0L)
            66, 98 -> jjMoveStringLiteralDfa1_0(30720L, 0L)
            67, 99 -> jjMoveStringLiteralDfa1_0(0xf8000L, 0L)
            68, 100 -> jjMoveStringLiteralDfa1_0(0xf00000L, 0L)
            69, 101 -> jjMoveStringLiteralDfa1_0(0x7000000L, 0L)
            70, 102 -> jjMoveStringLiteralDfa1_0(0x38000000L, 0L)
            71, 103 -> jjMoveStringLiteralDfa1_0(0x40000000L, 0L)
            72, 104 -> jjMoveStringLiteralDfa1_0(0x80000000L, 0L)
            73, 105 -> jjMoveStringLiteralDfa1_0(0x3f00000000L, 0L)
            76, 108 -> jjMoveStringLiteralDfa1_0(0xc000000000L, 0L)
            77, 109 -> jjMoveStringLiteralDfa1_0(0xf0000000000L, 0L)
            78, 110 -> jjMoveStringLiteralDfa1_0(0x1f00000000000L, 0L)
            79, 111 -> jjMoveStringLiteralDfa1_0(0x1e000000000000L, 0L)
            80, 112 -> jjMoveStringLiteralDfa1_0(0x20000000000000L, 0L)
            81, 113 -> jjMoveStringLiteralDfa1_0(0x40000000000000L, 0L)
            82, 114 -> jjMoveStringLiteralDfa1_0(0x780000000000000L, 0L)
            83, 115 -> jjMoveStringLiteralDfa1_0(-0x800000000000000L, 1L)
            84, 116 -> jjMoveStringLiteralDfa1_0(0L, 6L)
            85, 117 -> jjMoveStringLiteralDfa1_0(0L, 24L)
            86, 118 -> jjMoveStringLiteralDfa1_0(0L, 224L)
            87, 119 -> jjMoveStringLiteralDfa1_0(0L, 3840L)
            124 -> jjMoveStringLiteralDfa1_0(0L, 0x40000000000L)
            34, 36, 37, 38, 39, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 64, 74, 75, 88, 89, 90, 91, 92, 93, 94, 95, 96, 106, 107, 120, 121, 122, 123 -> jjMoveNfa_0(2, 0)
            else -> jjMoveNfa_0(2, 0)
        }
    }

    private fun jjMoveStringLiteralDfa1_0(l: Long, l1: Long): Int {
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(0, l, l1)
            return 1
        }
        when (curChar) {
            43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 63, 64, 66, 67, 68, 71, 74, 75, 81, 87, 90, 91, 92, 93, 94, 95, 96, 98, 99, 100, 103, 106, 107, 113, 119, 122, 123 -> {
            }
            42 -> {
                if (l1 and 0x10000000000L != 0L) return jjStopAtPos(1, 104)
                if (l1 and 0x100000000000L != 0L) return jjStopAtPos(1, 108)
            }
            61 -> {
                if (l1 and 0x40000000L != 0L) return jjStopAtPos(1, 94)
                if (l1 and 0x400000000L != 0L) return jjStopAtPos(1, 98)
                if (l1 and 0x1000000000L != 0L) return jjStopAtPos(1, 100)
            }
            62 -> if (l1 and 0x100000000L != 0L) return jjStopAtPos(1, 96)
            65, 97 -> return jjMoveStringLiteralDfa2_0(l, 0x110080100000L, l1, 226L)
            69, 101 -> return jjMoveStringLiteralDfa2_0(l, 0x1980000000600800L, l1, 0L)
            70, 102 -> if (l and 0x2000000000000L != 0L) return jjStartNfaWithStates_0(1, 49, 48)
            72, 104 -> return jjMoveStringLiteralDfa2_0(l, 0x2000000000008000L, l1, 256L)
            73, 105 -> return jjMoveStringLiteralDfa2_0(l, 0x64000801000L, l1, 512L)
            76, 108 -> return jjMoveStringLiteralDfa2_0(l, 0x8000020L, l1, 0L)
            77, 109 -> return jjMoveStringLiteralDfa2_0(l, 0x4000000000000000L, l1, 0L)
            78, 110 -> {
                if (l and 0x100000000L != 0L) {
                    jjmatchedKind = 32
                    jjmatchedPos = 1
                }
                return jjMoveStringLiteralDfa2_0(l, 0x4001e000000c0L, l1, 8L)
            }
            79, 111 -> return jjMoveStringLiteralDfa2_0(l, 0x6006880100f2000L, l1, 1024L)
            80, 112 -> return jjMoveStringLiteralDfa2_0(l, 0L, l1, 16L)
            82, 114 -> {
                if (l and 0x8000000000000L != 0L) {
                    jjmatchedKind = 51
                    jjmatchedPos = 1
                }
                return jjMoveStringLiteralDfa2_0(l, 0x30000060000000L, l1, 2052L)
            }
            83, 115 -> {
                if (l and 256L != 0L) {
                    jjmatchedKind = 8
                    jjmatchedPos = 1
                } else if (l and 0x2000000000L != 0L) return jjStartNfaWithStates_0(1, 37, 48)
                return jjMoveStringLiteralDfa2_0(l, 512L, l1, 0L)
            }
            84, 116 -> return jjMoveStringLiteralDfa2_0(l, (-0x8000000000000000L).toLong(), l1, 0L)
            85, 117 -> return jjMoveStringLiteralDfa2_0(l, 0x41800000000000L, l1, 1L)
            86, 118 -> return jjMoveStringLiteralDfa2_0(l, 1024L, l1, 0L)
            88, 120 -> return jjMoveStringLiteralDfa2_0(l, 0x7000000L, l1, 0L)
            89, 121 -> if (l and 16384L != 0L) return jjStartNfaWithStates_0(1, 14, 48)
            124 -> if (l1 and 0x40000000000L != 0L) return jjStopAtPos(1, 106)
            else -> {
            }
        }
        return jjStartNfa_0(0, l, l1)
    }

    private fun jjMoveStringLiteralDfa2_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(0, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(1, l1, l3)
            return 2
        }
        when (curChar) {
            70, 72, 74, 80, 81, 90, 91, 92, 93, 94, 95, 96, 102, 104, 106, 112, 113 -> {
            }
            65, 97 -> return jjMoveStringLiteralDfa3_0(l1, -0x1e7fffffffff8000L, l3, 4L)
            66, 98 -> return jjMoveStringLiteralDfa3_0(l1, 0L, l3, 2L)
            67, 99 -> {
                return if (l1 and 512L != 0L) jjStartNfaWithStates_0(2, 9, 48) else jjMoveStringLiteralDfa3_0(l1, 0x8001000000L, l3, 0L)
                // else
            }
            68, 100 -> {
                return if (l1 and 64L != 0L) jjStartNfaWithStates_0(2, 6, 48) else jjMoveStringLiteralDfa3_0(l1, 0x10080000000000L, l3, 16L)
                // else
            }
            69, 101 -> return jjMoveStringLiteralDfa3_0(l1, 0L, l3, 256L)
            71, 103 -> if (l1 and 1024L != 0L) return jjStartNfaWithStates_0(2, 10, 48)
            73, 105 -> return jjMoveStringLiteralDfa3_0(l1, 0x60000006000000L, l3, 2056L)
            75, 107 -> return jjMoveStringLiteralDfa3_0(l1, 0x4000000000L, l3, 0L)
            76, 108 -> {
                return if (l1 and 32L != 0L) jjStartNfaWithStates_0(2, 5, 48) else jjMoveStringLiteralDfa3_0(l1, 0xa04800000200000L, l3, 32L)
            }
            77, 109 -> {
                return if (l3 and 1L != 0L) jjStartNfaWithStates_0(2, 64, 48) else jjMoveStringLiteralDfa3_0(l1, 0x1000000030000L, l3, 0L)
            }
            78, 110 -> {
                if (l1 and 0x20000000000L != 0L) {
                    jjmatchedKind = 41
                    jjmatchedPos = 2
                }
                return jjMoveStringLiteralDfa3_0(l1, 0x40000041000L, l3, 0L)
            }
            79, 111 -> return jjMoveStringLiteralDfa3_0(l1, 0x68002000L, l3, 0L)
            82, 114 -> {
                return if (l1 and 0x10000000L != 0L) jjStartNfaWithStates_0(2, 28, 48) else jjMoveStringLiteralDfa3_0(l1, 0L, l3, 1216L)
            }
            83, 115 -> return jjMoveStringLiteralDfa3_0(l1, 0x200c00000L, l3, 0L)
            84, 116 -> {
                if (l1 and 0x200000000000L != 0L) return jjStartNfaWithStates_0(2, 45, 48)
                return if (l1 and 0x1000000000000000L != 0L) jjStartNfaWithStates_0(2, 60, 48) else jjMoveStringLiteralDfa3_0(l1, 0x101c00100800L, l3, 512L)
            }
            85, 117 -> return jjMoveStringLiteralDfa3_0(l1, 0x80000L, l3, 0L)
            86, 118 -> return jjMoveStringLiteralDfa3_0(l1, 0x80000000L, l3, 0L)
            87, 119 -> {
                return if (l1 and 0x400000000000000L != 0L) jjStartNfaWithStates_0(2, 58, 48) else jjMoveStringLiteralDfa3_0(l1, 0x400000000000L, l3, 0L)
            }
            88, 120 -> if (l1 and 0x10000000000L != 0L) return jjStartNfaWithStates_0(2, 40, 48)
            89, 121 -> if (l1 and 128L != 0L) return jjStartNfaWithStates_0(2, 7, 48)
            else -> {
            }
        }
        return jjStartNfa_0(1, l1, l3)
    }

    private fun jjMoveStringLiteralDfa3_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(1, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(2, l1, l3)
            return 3
        }
        when (curChar) {
            70, 71, 74, 80, 81, 86, 88, 90, 91, 92, 93, 94, 95, 96, 102, 103, 106, 112, 113, 118, 120 -> {
            }
            65, 97 -> return jjMoveStringLiteralDfa4_0(l1, 0x400008001000L, l3, 16L)
            66, 98 -> return jjMoveStringLiteralDfa4_0(l1, 0x1000000000000L, l3, 0L)
            67, 99 -> {
                return if (l1 and 0x400000L != 0L) jjStartNfaWithStates_0(3, 22, 48) else jjMoveStringLiteralDfa4_0(l1, 0L, l3, 192L)
            }
            68, 100 -> if (l1 and 0x80000000000000L != 0L) return jjStartNfaWithStates_0(3, 55, 48)
            69, 101 -> {
                if (l1 and 0x100000L != 0L) return jjStartNfaWithStates_0(3, 20, 48)
                if (l1 and 0x4000000000L != 0L) return jjStartNfaWithStates_0(3, 38, 48)
                return if (l1 and 0x80000000000L != 0L) jjStartNfaWithStates_0(3, 43, 48) else jjMoveStringLiteralDfa4_0(l1, 0x810000e00200000L, l3, 0L)
            }
            72, 104 -> if (l3 and 512L != 0L) return jjStartNfaWithStates_0(3, 73, 48)
            73, 105 -> return jjMoveStringLiteralDfa4_0(l1, 0x80000000L, l3, 0L)
            75, 107 -> {
                if (l1 and 0x8000000000L != 0L) return jjStartNfaWithStates_0(3, 39, 48)
                if (l3 and 1024L != 0L) return jjStartNfaWithStates_0(3, 74, 48)
            }
            76, 108 -> {
                if (l1 and 0x800000000000L != 0L) return jjStartNfaWithStates_0(3, 47, 48)
                return if (l1 and 0x100000000000000L != 0L) jjStartNfaWithStates_0(3, 56, 48) else jjMoveStringLiteralDfa4_0(l1, 0x4200000001002000L, l3, 2L)
            }
            77, 109 -> {
                return if (l1 and 0x20000000L != 0L) jjStartNfaWithStates_0(3, 29, 48) else jjMoveStringLiteralDfa4_0(l1, 0x30000L, l3, 0L)
            }
            78, 110 -> return jjMoveStringLiteralDfa4_0(l1, 0xc0000L, l3, 4L)
            79, 111 -> {
                return if (l1 and 0x1000000000L != 0L) jjStartNfaWithStates_0(3, 36, 48) else jjMoveStringLiteralDfa4_0(l1, 0x20000000000000L, l3, 8L)
            }
            82, 114 -> {
                return if (l1 and 32768L != 0L) jjStartNfaWithStates_0(3, 15, 48) else jjMoveStringLiteralDfa4_0(l1, -0x6000000000000000L, l3, 256L)
            }
            83, 115 -> return jjMoveStringLiteralDfa4_0(l1, 0x2000000L, l3, 0L)
            84, 116 -> {
                if (l1 and 0x4000000L != 0L) return jjStartNfaWithStates_0(3, 26, 48)
                return if (l1 and 0x40000000000000L != 0L) jjStartNfaWithStates_0(3, 54, 48) else jjMoveStringLiteralDfa4_0(l1, 0x800000L, l3, 2048L)
            }
            85, 117 -> return jjMoveStringLiteralDfa4_0(l1, 0x140040000000L, l3, 32L)
            87, 119 -> return jjMoveStringLiteralDfa4_0(l1, 2048L, l3, 0L)
            89, 121 -> if (l1 and 0x4000000000000L != 0L) return jjStartNfaWithStates_0(3, 50, 48)
            else -> {
            }
        }
        return jjStartNfa_0(2, l1, l3)
    }

    private fun jjMoveStringLiteralDfa4_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(2, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(3, l1, l3)
            return 4
        }
        when (curChar) {
            68, 70, 74, 75, 77, 79, 81, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 100, 102, 106, 107, 109, 111, 113 -> {
            }
            66, 98 -> return jjMoveStringLiteralDfa5_0(l1, 0x200000000000000L, l3, 0L)
            67, 99 -> return jjMoveStringLiteralDfa5_0(l1, 0x800000000000000L, l3, 0L)
            69, 101 -> {
                if (l1 and 0x2000000000000000L != 0L) return jjStartNfaWithStates_0(4, 61, 48)
                if (l3 and 2L != 0L) return jjStartNfaWithStates_0(4, 65, 48)
                if (l3 and 256L != 0L) return jjStartNfaWithStates_0(4, 72, 48)
                return if (l3 and 2048L != 0L) jjStartNfaWithStates_0(4, 75, 48) else jjMoveStringLiteralDfa5_0(l1, 0x1000000052800L, l3, 32L)
            }
            71, 103 -> return jjMoveStringLiteralDfa5_0(l1, 0x400000000L, l3, 0L)
            72, 104 -> return jjMoveStringLiteralDfa5_0(l1, 0L, l3, 192L)
            73, 105 -> return jjMoveStringLiteralDfa5_0(l1, 0x400000820000L, l3, 0L)
            76, 108 -> return jjMoveStringLiteralDfa5_0(l1, 0x4000000000000000L, l3, 0L)
            78, 110 -> {
                return if (l3 and 8L != 0L) jjStartNfaWithStates_0(4, 67, 48) else jjMoveStringLiteralDfa5_0(l1, 0x80000000L, l3, 0L)
            }
            80, 112 -> if (l1 and 0x40000000L != 0L) return jjStartNfaWithStates_0(4, 30, 48)
            82, 114 -> {
                if (l1 and 0x10000000000000L != 0L) return jjStartNfaWithStates_0(4, 52, 48)
                return if (l1 and 0x20000000000000L != 0L) jjStartNfaWithStates_0(4, 53, 48) else jjMoveStringLiteralDfa5_0(l1, 0x100a00001000L, l3, 0L)
            }
            83, 115 -> {
                return if (l1 and 0x40000000000L != 0L) jjStartNfaWithStates_0(4, 42, 48) else jjMoveStringLiteralDfa5_0(l1, 0L, l3, 4L)
            }
            84, 116 -> {
                if (l1 and 0x80000L != 0L) return jjStartNfaWithStates_0(4, 19, 48)
                if (l1 and 0x8000000L != 0L) return jjStartNfaWithStates_0(4, 27, 48)
                return if (l1 and (-0x8000000000000000L).toLong() != 0L) jjStartNfaWithStates_0(4, 63, 48) else jjMoveStringLiteralDfa5_0(l1, 0x2200000L, l3, 16L)
            }
            85, 117 -> return jjMoveStringLiteralDfa5_0(l1, 0x1000000L, l3, 0L)
            else -> {
            }
        }
        return jjStartNfa_0(3, l1, l3)
    }

    private fun jjMoveStringLiteralDfa5_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(3, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(4, l1, l3)
            return 5
        }
        when (curChar) {
            66, 68, 70, 72, 74, 75, 76, 77, 79, 80, 81, 85, 86, 87, 88, 90, 91, 92, 93, 94, 95, 96, 98, 100, 102, 104, 106, 107, 108, 109, 111, 112, 113, 117, 118, 119, 120 -> {
            }
            65, 97 -> return jjMoveStringLiteralDfa6_0(l1, 0x200100000002000L, l3, 196L)
            67, 99 -> return jjMoveStringLiteralDfa6_0(l1, 0x40000L, l3, 0L)
            69, 101 -> {
                if (l1 and 0x200000L != 0L) return jjStartNfaWithStates_0(5, 21, 48)
                return if (l3 and 16L != 0L) jjStartNfaWithStates_0(5, 68, 48) else jjMoveStringLiteralDfa6_0(l1, 0x400000800L, l3, 0L)
            }
            71, 103 -> if (l1 and 0x80000000L != 0L) return jjStartNfaWithStates_0(5, 31, 48)
            73, 105 -> return jjMoveStringLiteralDfa6_0(l1, 0x4000000000000000L, l3, 0L)
            78, 110 -> return jjMoveStringLiteralDfa6_0(l1, 0x810000L, l3, 0L)
            82, 114 -> if (l1 and 0x1000000000000L != 0L) return jjStartNfaWithStates_0(5, 48, 48)
            83, 115 -> {
                if (l1 and 0x2000000L != 0L) return jjStartNfaWithStates_0(5, 25, 48)
                return if (l3 and 32L != 0L) jjStartNfaWithStates_0(5, 69, 48) else jjMoveStringLiteralDfa6_0(l1, 0x801000000L, l3, 0L)
            }
            84, 116 -> {
                if (l1 and 0x20000L != 0L) return jjStartNfaWithStates_0(5, 17, 48)
                if (l1 and 0x200000000L != 0L) return jjStartNfaWithStates_0(5, 33, 48)
                if (l1 and 0x400000000000L != 0L) return jjStartNfaWithStates_0(5, 46, 48)
                if (l1 and 0x800000000000000L != 0L) return jjStartNfaWithStates_0(5, 59, 48)
            }
            89, 121 -> return jjMoveStringLiteralDfa6_0(l1, 4096L, l3, 0L)
            else -> {
            }
        }
        return jjStartNfa_0(4, l1, l3)
    }

    private fun jjMoveStringLiteralDfa6_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(4, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(5, l1, l3)
            return 6
        }
        when (curChar) {
            68, 70, 71, 72, 74, 75, 77, 79, 80, 81, 83, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 96, 97, 98, 100, 102, 103, 104, 106, 107, 109, 111, 112, 113, 115 -> {
            }
            95 -> return jjMoveStringLiteralDfa7_0(l1, 4096L, l3, 0L)
            67, 99 -> return jjMoveStringLiteralDfa7_0(l1, 0x200000000800000L, l3, 4L)
            69, 101 -> return jjMoveStringLiteralDfa7_0(l1, 0x800000000L, l3, 0L)
            73, 105 -> return jjMoveStringLiteralDfa7_0(l1, 0x1000000L, l3, 0L)
            76, 108 -> if (l1 and 0x100000000000L != 0L) return jjStartNfaWithStates_0(6, 44, 48)
            78, 110 -> {
                if (l1 and 2048L != 0L) return jjStartNfaWithStates_0(6, 11, 48)
                return if (l1 and 8192L != 0L) jjStartNfaWithStates_0(6, 13, 48) else jjMoveStringLiteralDfa7_0(l1, 0x4000000000000000L, l3, 0L)
            }
            82, 114 -> {
                if (l1 and 0x400000000L != 0L) return jjStartNfaWithStates_0(6, 34, 48)
                if (l3 and 128L != 0L) {
                    jjmatchedKind = 71
                    jjmatchedPos = 6
                }
                return jjMoveStringLiteralDfa7_0(l1, 0L, l3, 64L)
            }
            84, 116 -> {
                if (l1 and 0x10000L != 0L) return jjStartNfaWithStates_0(6, 16, 48)
                if (l1 and 0x40000L != 0L) return jjStartNfaWithStates_0(6, 18, 48)
            }
            else -> {
            }
        }
        return jjStartNfa_0(5, l1, l3)
    }

    private fun jjMoveStringLiteralDfa7_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(5, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(6, l1, l3)
            return 7
        }
        when (curChar) {
            50 -> if (l3 and 64L != 0L) return jjStartNfaWithStates_0(7, 70, 11)
            67, 99 -> return jjMoveStringLiteralDfa8_0(l1, 0x800000000L, l3, 0L)
            73, 105 -> return jjMoveStringLiteralDfa8_0(l1, 4096L, l3, 0L)
            75, 107 -> if (l1 and 0x200000000000000L != 0L) return jjStartNfaWithStates_0(7, 57, 48)
            84, 116 -> {
                if (l1 and 0x800000L != 0L) return jjStartNfaWithStates_0(7, 23, 48)
                return if (l1 and 0x4000000000000000L != 0L) jjStartNfaWithStates_0(7, 62, 48) else jjMoveStringLiteralDfa8_0(l1, 0L, l3, 4L)
            }
            86, 118 -> return jjMoveStringLiteralDfa8_0(l1, 0x1000000L, l3, 0L)
            else -> {
            }
        }
        return jjStartNfa_0(6, l1, l3)
    }

    private fun jjMoveStringLiteralDfa8_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(6, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(7, l1, l3)
            return 8
        }
        when (curChar) {
            69, 101 -> if (l1 and 0x1000000L != 0L) return jjStartNfaWithStates_0(8, 24, 48)
            73, 105 -> return jjMoveStringLiteralDfa9_0(l1, 0L, l3, 4L)
            78, 110 -> return jjMoveStringLiteralDfa9_0(l1, 4096L, l3, 0L)
            84, 116 -> if (l1 and 0x800000000L != 0L) return jjStartNfaWithStates_0(8, 35, 48)
            else -> {
            }
        }
        return jjStartNfa_0(7, l1, l3)
    }

    private fun jjMoveStringLiteralDfa9_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(7, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(8, l1, l3)
            return 9
        }
        when (curChar) {
            79, 111 -> return jjMoveStringLiteralDfa10_0(l1, 0L, l3, 4L)
            84, 116 -> return jjMoveStringLiteralDfa10_0(l1, 4096L, l3, 0L)
        }
        return jjStartNfa_0(8, l1, l3)
    }

    private fun jjMoveStringLiteralDfa10_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(8, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(9, l1, l3)
            return 10
        }
        when (curChar) {
            69, 101 -> return jjMoveStringLiteralDfa11_0(l1, 4096L, l3, 0L)
            78, 110 -> if (l3 and 4L != 0L) return jjStartNfaWithStates_0(10, 66, 48)
        }
        return jjStartNfa_0(9, l1, l3)
    }

    private fun jjMoveStringLiteralDfa11_0(l: Long, l1: Long, l2: Long, l3: Long): Int {
        var l1 = l1
        var l3 = l3
        if (l.let { l1 = l1 and it; l1 } or l2.let { l3 = l3 and it; l3 } == 0L) return jjStartNfa_0(9, l, l2)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(10, l1, 0L)
            return 11
        }
        when (curChar) {
            71, 103 -> return jjMoveStringLiteralDfa12_0(l1, 4096L)
        }
        return jjStartNfa_0(10, l1, 0L)
    }

    private fun jjMoveStringLiteralDfa12_0(l: Long, l1: Long): Int {
        var l1 = l1
        if (l.let { l1 = l1 and it; l1 } == 0L) return jjStartNfa_0(10, l, 0L)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(11, l1, 0L)
            return 12
        }
        when (curChar) {
            69, 101 -> return jjMoveStringLiteralDfa13_0(l1, 4096L)
        }
        return jjStartNfa_0(11, l1, 0L)
    }

    private fun jjMoveStringLiteralDfa13_0(l: Long, l1: Long): Int {
        var l1 = l1
        if (l.let { l1 = l1 and it; l1 } == 0L) return jjStartNfa_0(11, l, 0L)
        curChar = try {
            input_stream!!.readChar()
        } catch (ioexception: IOException) {
            jjStopStringLiteralDfa_0(12, l1, 0L)
            return 13
        }
        when (curChar) {
            82, 114 -> if (l1 and 4096L != 0L) return jjStartNfaWithStates_0(13, 12, 11)
        }
        return jjStartNfa_0(12, l1, 0L)
    }

    private fun jjCheckNAdd(i: Int) {
        if (jjrounds!![i] != jjround) {
            jjstateSet!![jjnewStateCnt++] = i
            jjrounds[i] = jjround
        }
    }

    private fun jjAddStates(i: Int, j: Int) {
        var i = i
        do jjstateSet!![jjnewStateCnt++] = jjnextStates!![i] while (i++ != j)
    }

    private fun jjCheckNAddTwoStates(i: Int, j: Int) {
        jjCheckNAdd(i)
        jjCheckNAdd(j)
    }

    private fun jjCheckNAddStates(i: Int, j: Int) {
        var i = i
        do jjCheckNAdd(jjnextStates!![i]) while (i++ != j)
    }

    private fun jjCheckNAddStates(i: Int) {
        jjCheckNAdd(jjnextStates!![i])
        jjCheckNAdd(jjnextStates[i + 1])
    }

    private fun jjMoveNfa_0(i: Int, j: Int): Int {
        var j = j
        var k = 0
        jjnewStateCnt = 47
        var l = 1
        jjstateSet!![0] = i
        var i1 = 0x7fffffff
        do {
            if (++jjround == 0x7fffffff) ReInitRounds()
            if (curChar < '@') {
                val l1 = 1L shl curChar.toLong().toInt()
                do when (jjstateSet[--l]) {
                    2 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddStates(0, 6)
                    } else if (curChar == '.') jjCheckNAddTwoStates(27, 37) else if (curChar == '"') jjCheckNAddTwoStates(24, 25) else if (curChar == '\'') jjCheckNAddTwoStates(19, 20) else if (curChar == ':') jjstateSet[jjnewStateCnt++] = 13 else if (curChar == '/') jjstateSet[jjnewStateCnt++] = 3 else if (curChar == '-') jjstateSet[jjnewStateCnt++] = 0
                    11, 48 -> if (0x3ff001000000000L and l1 != 0L) {
                        if (i1 > 82) i1 = 82
                        jjCheckNAdd(11)
                    }
                    47 -> {
                        if (0x3ff000000000000L and l1 != 0L) {
                            if (i1 > 76) i1 = 76
                            jjCheckNAdd(37)
                        }
                        if (0x3ff000000000000L and l1 != 0L) {
                            if (i1 > 76) i1 = 76
                            jjCheckNAddTwoStates(27, 28)
                        }
                    }
                    0 -> if (curChar == '-') {
                        if (i1 > 80) i1 = 80
                        jjCheckNAdd(1)
                    }
                    1 -> if (-9217L and l1 != 0L) {
                        if (i1 > 80) i1 = 80
                        jjCheckNAdd(1)
                    }
                    3 -> if (curChar == '*') jjCheckNAddTwoStates(4, 5)
                    4 -> if (-0x40000000001L and l1 != 0L) jjCheckNAddTwoStates(4, 5)
                    5 -> if (curChar == '*') jjCheckNAddStates(7, 9)
                    6 -> if (-0x840000000001L and l1 != 0L) jjCheckNAddTwoStates(7, 5)
                    7 -> if (-0x40000000001L and l1 != 0L) jjCheckNAddTwoStates(7, 5)
                    8 -> if (curChar == '/' && i1 > 81) i1 = 81
                    9 -> if (curChar == '/') jjstateSet[jjnewStateCnt++] = 3
                    12 -> if (curChar == ':') jjstateSet[jjnewStateCnt++] = 13
                    14 -> if (0x3ff001000000000L and l1 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjAddStates(10, 11)
                    }
                    15 -> if (curChar == '.') jjstateSet[jjnewStateCnt++] = 16
                    17 -> if (0x3ff001000000000L and l1 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjstateSet[jjnewStateCnt++] = 17
                    }
                    18 -> if (curChar == '\'') jjCheckNAddTwoStates(19, 20)
                    19 -> if (-0x8000000001L and l1 != 0L) jjCheckNAddTwoStates(19, 20)
                    20 -> if (curChar == '\'') {
                        if (i1 > 86) i1 = 86
                        jjstateSet[jjnewStateCnt++] = 21
                    }
                    21 -> if (curChar == '\'') jjCheckNAddTwoStates(22, 20)
                    22 -> if (-0x8000000001L and l1 != 0L) jjCheckNAddTwoStates(22, 20)
                    23 -> if (curChar == '"') jjCheckNAddTwoStates(24, 25)
                    24 -> if (-0x400002401L and l1 != 0L) jjCheckNAddTwoStates(24, 25)
                    25 -> if (curChar == '"' && i1 > 87) i1 = 87
                    26 -> if (curChar == '.') jjCheckNAddTwoStates(27, 37)
                    27 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddTwoStates(27, 28)
                    }
                    29 -> if (0x280000000000L and l1 != 0L) jjAddStates(12, 13)
                    30 -> if (curChar == '.') jjCheckNAdd(31)
                    31 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(31)
                    }
                    32 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddStates(14, 16)
                    }
                    33 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(33)
                    }
                    34 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddTwoStates(34, 35)
                    }
                    35 -> if (curChar == '.') jjCheckNAdd(36)
                    36 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(36)
                    }
                    37 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(37)
                    }
                    38 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddStates(0, 6)
                    }
                    39 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddTwoStates(39, 28)
                    }
                    40 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddStates(17, 19)
                    }
                    41 -> if (curChar == '.') jjCheckNAdd(42)
                    42 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddTwoStates(42, 28)
                    }
                    43 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAddTwoStates(43, 44)
                    }
                    44 -> if (curChar == '.') jjCheckNAdd(45)
                    45 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(45)
                    }
                    46 -> if (0x3ff000000000000L and l1 != 0L) {
                        if (i1 > 76) i1 = 76
                        jjCheckNAdd(46)
                    }
                } while (l != k)
            } else if (curChar < '\u0080') {
                val l2 = 1L shl (curChar.toInt() and 0x3f)
                do when (jjstateSet[--l]) {
                    2, 10 -> if (0x7fffffe07fffffeL and l2 != 0L) {
                        if (i1 > 82) i1 = 82
                        jjCheckNAddTwoStates(10, 11)
                    }
                    48 -> {
                        if (0x7fffffe87fffffeL and l2 != 0L) {
                            if (i1 > 82) i1 = 82
                            jjCheckNAdd(11)
                        }
                        if (0x7fffffe07fffffeL and l2 != 0L) {
                            if (i1 > 82) i1 = 82
                            jjCheckNAddTwoStates(10, 11)
                        }
                    }
                    1 -> {
                        if (i1 > 80) i1 = 80
                        jjstateSet[jjnewStateCnt++] = 1
                    }
                    4 -> jjCheckNAddTwoStates(4, 5)
                    6, 7 -> jjCheckNAddTwoStates(7, 5)
                    11 -> if (0x7fffffe87fffffeL and l2 != 0L) {
                        if (i1 > 82) i1 = 82
                        jjCheckNAdd(11)
                    }
                    13 -> if (0x7fffffe07fffffeL and l2 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjCheckNAddStates(20, 22)
                    }
                    14 -> if (0x7fffffe87fffffeL and l2 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjCheckNAddTwoStates(14, 15)
                    }
                    16 -> if (0x7fffffe07fffffeL and l2 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjCheckNAddTwoStates(16, 17)
                    }
                    17 -> if (0x7fffffe87fffffeL and l2 != 0L) {
                        if (i1 > 85) i1 = 85
                        jjCheckNAdd(17)
                    }
                    19 -> jjCheckNAddTwoStates(19, 20)
                    22 -> jjCheckNAddTwoStates(22, 20)
                    24 -> jjAddStates(23, 24)
                    28 -> if (0x2000000020L and l2 != 0L) jjAddStates(25, 27)
                } while (l != k)
            } else {
                val j1 = curChar.toInt() and 0xff shr 6
                val l3 = 1L shl (curChar.toInt() and 0x3f)
                do when (jjstateSet[--l]) {
                    1 -> if (jjbitVec0!![j1] and l3 != 0L) {
                        if (i1 > 80) i1 = 80
                        jjstateSet[jjnewStateCnt++] = 1
                    }
                    4 -> if (jjbitVec0!![j1] and l3 != 0L) jjCheckNAddTwoStates(4, 5)
                    6, 7 -> if (jjbitVec0!![j1] and l3 != 0L) jjCheckNAddTwoStates(7, 5)
                    19 -> if (jjbitVec0!![j1] and l3 != 0L) jjCheckNAddTwoStates(19, 20)
                    22 -> if (jjbitVec0!![j1] and l3 != 0L) jjCheckNAddTwoStates(22, 20)
                    24 -> if (jjbitVec0!![j1] and l3 != 0L) jjAddStates(23, 24)
                } while (l != k)
            }
            if (i1 != 0x7fffffff) {
                jjmatchedKind = i1
                jjmatchedPos = j
                i1 = 0x7fffffff
            }
            j++
            if (jjnewStateCnt.also { l = it } == 47 - k.also { jjnewStateCnt = it }.also { k = it }) return j
            curChar = try {
                input_stream!!.readChar()
            } catch (ioexception: IOException) {
                return j
            }
        } while (true)
    }

    constructor(simplecharstream: SimpleCharStream?, i: Int) : this(simplecharstream) {
        SwitchTo(i)
    }

    fun ReInit(simplecharstream: SimpleCharStream?) {
        jjnewStateCnt = 0
        jjmatchedPos = jjnewStateCnt
        curLexState = defaultLexState
        input_stream = simplecharstream
        ReInitRounds()
    }

    private fun ReInitRounds() {
        jjround = -0x7fffffff
        var i = 47
        while (i-- > 0) {
            jjrounds!![i] = -0x80000000
        }
    }

    fun ReInit(simplecharstream: SimpleCharStream?, i: Int) {
        ReInit(simplecharstream)
        SwitchTo(i)
    }

    fun SwitchTo(i: Int) {
        if (i >= 1 || i < 0) {
            throw TokenMgrError("Error: Ignoring invalid lexical state : $i. State unchanged.", 2)
        }
        // else{
        curLexState = i
        return
        // }
    }

    private fun jjFillToken(): Token? {
        val token: Token = Token.newToken(jjmatchedKind)
        token!!.kind = jjmatchedKind
        val s = jjstrLiteralImages!![jjmatchedKind]
        token!!.image = s ?: input_stream!!.GetImage()
        token!!.beginLine = input_stream.getBeginLine()
        token!!.beginColumn = input_stream.getBeginColumn()
        token!!.endLine = input_stream.getEndLine()
        token!!.endColumn = input_stream.getEndColumn()
        return token
    }

    val nextToken: lucee.runtime.sql.old.Token?
        get() {
            var token: Token? = null
            var i = 0
            do {
                try {
                    curChar = input_stream!!.BeginToken()
                } catch (ioexception: IOException) {
                    jjmatchedKind = 0
                    val token1: Token? = jjFillToken()
                    token1!!.specialToken = token
                    return token1
                }
                try {
                    input_stream!!.backup(0)
                    while (curChar <= ' ' && 0x100002600L and 1L shl curChar.toLong().toInt() != 0L) {
                        curChar = input_stream!!.BeginToken()
                    }
                } catch (ioexception1: IOException) {
                    continue
                }
                jjmatchedKind = 0x7fffffff
                jjmatchedPos = 0
                i = jjMoveStringLiteralDfa0_0()
                if (jjmatchedKind == 0x7fffffff) break
                if (jjmatchedPos + 1 < i) input_stream!!.backup(i - jjmatchedPos - 1)
                if (jjtoToken!![jjmatchedKind shr 6] and 1L shl (jjmatchedKind and 0x3f) != 0L) {
                    val token2: Token? = jjFillToken()
                    token2!!.specialToken = token
                    return token2
                }
                if (jjtoSpecial!![jjmatchedKind shr 6] and 1L shl (jjmatchedKind and 0x3f) != 0L) {
                    val token3: Token? = jjFillToken()
                    if (token == null) {
                        token = token3
                    } else {
                        token3!!.specialToken = token
                        token.next = token3
                        token = token.next
                    }
                }
            } while (true)
            var j: Int = input_stream.getEndLine()
            var k: Int = input_stream.getEndColumn()
            var s: String? = null
            var flag = false
            try {
                input_stream!!.readChar()
                input_stream!!.backup(1)
            } catch (ioexception2: IOException) {
                flag = true
                s = if (i > 1) input_stream!!.GetImage() else ""
                if (curChar == '\n' || curChar == '\r') {
                    j++
                    k = 0
                } else {
                    k++
                }
            }
            if (!flag) {
                input_stream!!.backup(1)
                s = if (i > 1) input_stream!!.GetImage() else ""
            }
            throw TokenMgrError(flag, curLexState, j, k, s, curChar, 0)
        }
    var debugStream: PrintStream?
    private var input_stream: SimpleCharStream?
    private val jjrounds: IntArray?
    private val jjstateSet: IntArray?
    protected var curChar = 0.toChar()
    var curLexState: Int
    var defaultLexState: Int
    var jjnewStateCnt = 0
    var jjround = 0
    var jjmatchedPos = 0
    var jjmatchedKind = 0

    companion object {
        val jjbitVec0: LongArray? = longArrayOf(0L, 0L, -1L, -1L)
        val jjnextStates: IntArray? = intArrayOf(39, 40, 41, 28, 43, 44, 46, 5, 6, 8, 14, 15, 30, 32, 33, 34, 35, 40, 41, 28, 13, 14, 15, 24, 25, 29, 30, 32)
        val jjstrLiteralImages: Array<String?>? = arrayOf("", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, "(", ",", ")", ";", "=", ".", "!=", "#", "<>", ">", ">=", "<", "<=", "+", "-", "*", ".*", "?", "||",
                "/", "**")
        val lexStateNames: Array<String?>? = arrayOf("DEFAULT")
        val jjtoToken: LongArray? = longArrayOf(-31L, 0x1fffffe41fffL)
        val jjtoSkip: LongArray? = longArrayOf(30L, 0x30000L)
        val jjtoSpecial: LongArray? = longArrayOf(0L, 0x30000L)
    }

    init {
        debugStream = System.out
        jjrounds = IntArray(47)
        jjstateSet = IntArray(94)
        curLexState = 0
        defaultLexState = 0
        input_stream = simplecharstream
    }
}