/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.video

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

class VideoProfileImpl : VideoProfile {
    private var type: String? = null
    private var dimension: String? = null
    private var audioBitrate: Long = 0
    private var videoBitrate: Long = 0
    private var videoBitrateMin: Long = 0
    private var videoBitrateMax: Long = 0
    private var videoBitrateTolerance: Long = 0

    // private boolean sameQualityAsSource =false;
    private var framerate = 0.0
    private var audioSamplerate = 0.0
    private var aspectRatio = 0
    private var scanMode = 0
    private var videoCodec: String? = null
    private var audioCodec: String? = null

    // private long bufferSize;
    private var bufferSize: Long = 0
    private var pass = 0

    constructor(type: String?, dimension: String?, audioBitrate: Long, videoBitrate: Long, videoBitrateMin: Long, videoBitrateMax: Long, videoBitrateTolerance: Long,
                framerate: Double, aspectRatio: Int, scanMode: Int, audioCodec: String?, videoCodec: String?, audioSamplerate: Double) : super() {
        this.type = type
        this.dimension = dimension
        this.audioBitrate = audioBitrate
        this.videoBitrate = videoBitrate
        this.videoBitrateMin = videoBitrateMin
        this.videoBitrateMax = videoBitrateMax
        this.videoBitrateTolerance = videoBitrateTolerance
        this.framerate = framerate
        this.aspectRatio = aspectRatio
        this.scanMode = scanMode
        this.audioCodec = audioCodec
        this.videoCodec = videoCodec
        this.audioSamplerate = audioSamplerate
    }

    constructor() {}

    @Override
    fun duplicate(): VideoProfile? {
        return VideoProfileImpl(type, dimension, audioBitrate, videoBitrate, videoBitrateMin, videoBitrateMax, videoBitrateTolerance, framerate, aspectRatio, scanMode,
                audioCodec, videoCodec, audioSamplerate)
    }

    /**
     * set the type of the output format (see constants "TYPE_xxx" of this class)
     *
     * @param type
     */
    @Override
    fun setType(type: String?) {
        this.type = type
    }

    /**
     * @return the type
     */
    @Override
    fun getType(): String? {
        return type
    }

    /**
     * @return the dimension
     */
    @Override
    fun getDimension(): String? {
        return dimension
    }

    @Override
    fun setDimension(width: Int, height: Int) {
        checkDimension(width, "width")
        checkDimension(height, "height")
        dimension = width.toString() + "X" + height
    }

    private fun checkDimension(value: Int, label: String?) {
        // if(((value/2)*2)!=value)
        // throw new VideoException("dimension ["+value+"] "+label+" must be the muliple of 2 (2,4,8,16
        // ...)");
    }

    /**
     * @return the bitrate
     */
    @Override
    fun getVideoBitrate(): Double {
        return videoBitrate.toDouble()
    }

    /**
     * set video bitrate in kbit/s (default 200)
     *
     * @param bitrate the bitrate to set
     */
    @Override
    fun setVideoBitrate(bitrate: Long) {
        videoBitrate = bitrate
    }

    /**
     * @return the framerate
     */
    @Override
    fun getFramerate(): Double {
        return framerate
    }

    /**
     * sets the framerate (default 25)
     *
     * @param framerate the framerate to set
     */
    @Override
    fun setFramerate(framerate: Double) {
        this.framerate = framerate
    }

    /**
     * @return the aspectRatio
     */
    @Override
    fun getAspectRatio(): Int {
        return aspectRatio
    }

    /**
     * sets the aspectRatio (VideoOutput.ASPECT_RATIO_xxx)
     *
     * @param aspectRatio the aspectRatio to set
     */
    @Override
    fun setAspectRatio(aspectRatio: Int) {
        this.aspectRatio = aspectRatio
    }

    @Override
    fun setAspectRatio(strAspectRatio: String?) {
        var strAspectRatio = strAspectRatio
        strAspectRatio = strAspectRatio.trim().toLowerCase()
        if ("16:9".equals(strAspectRatio)) aspectRatio = ASPECT_RATIO_16_9 else if ("4:3".equals(strAspectRatio)) aspectRatio = ASPECT_RATIO_4_3
    }

    /**
     * @return the bitrateMin
     */
    @Override
    fun getVideoBitrateMin(): Double {
        return videoBitrateMin.toDouble()
    }

    /**
     * set min video bitrate tolerance (in kbit/s)
     *
     * @param bitrateMin the bitrateMin to set
     */
    @Override
    fun setVideoBitrateMin(bitrateMin: Long) {
        videoBitrateMin = bitrateMin
    }

    /**
     * @return the bitrateMax
     */
    @Override
    fun getVideoBitrateMax(): Double {
        return videoBitrateMax.toDouble()
    }

    /**
     * set max video bitrate tolerance (in kbit/s)
     *
     * @param bitrateMax the bitrateMax to set
     */
    @Override
    fun setVideoBitrateMax(bitrateMax: Long) {
        videoBitrateMax = bitrateMax
    }

    /**
     * @return the bitrateTolerance
     */
    @Override
    fun getVideoBitrateTolerance(): Double {
        return videoBitrateTolerance.toDouble()
    }

    /**
     * set video bitrate tolerance (in kbit/s)
     *
     * @param bitrateTolerance the bitrateTolerance to set
     */
    @Override
    fun setVideoBitrateTolerance(bitrateTolerance: Long) {
        videoBitrateTolerance = bitrateTolerance
    }

    /**
     * @return the audioBitrate
     */
    @Override
    fun getAudioBitrate(): Double {
        return audioBitrate.toDouble()
    }

    /**
     * @return the scanMode
     */
    @Override
    fun getScanMode(): Int {
        return scanMode
    }

    /**
     * @param scanMode the scanMode to set
     */
    @Override
    fun setScanMode(scanMode: Int) {
        this.scanMode = scanMode
    }

    /**
     * @param audioBitrate the audioBitrate to set
     */
    @Override
    fun setAudioBitrate(audioBitrate: Long) {
        this.audioBitrate = audioBitrate
    }

    @Override
    fun setAudioCodec(codec: String?) {
        audioCodec = codec
    }

    @Override
    fun setVideoCodec(codec: String?) {
        videoCodec = codec
    }

    /**
     * @return the videoCodec
     */
    @Override
    fun getVideoCodec(): String? {
        return videoCodec
    }

    /**
     * @return the audioCodec
     */
    @Override
    fun getAudioCodec(): String? {
        return audioCodec
    }

    /**
     * @return the audioSamplerate
     */
    @Override
    fun getAudioSamplerate(): Double {
        return audioSamplerate
    }

    /**
     * @param audioSamplerate the audioSamplerate to set
     */
    @Override
    fun setAudioSamplerate(audioSamplerate: Double) {
        this.audioSamplerate = audioSamplerate
    }

    /**
     * @return the bufferSize
     */
    @Override
    fun getBufferSize(): Long {
        return bufferSize
    }

    /**
     * @param bufferSize the bufferSize to set
     */
    @Override
    fun setBufferSize(bufferSize: Long) {
        this.bufferSize = bufferSize
    }

    /**
     * @return the pass
     */
    @Override
    fun getPass(): Int {
        return pass
    }

    /**
     * @param pass the pass to set
     */
    @Override
    fun setPass(pass: Int) {
        this.pass = pass
    }
}