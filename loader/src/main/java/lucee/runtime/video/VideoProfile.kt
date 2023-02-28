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
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

interface VideoProfile {
    fun duplicate(): VideoProfile?
    /**
     * @return the type
     */
    /**
     * set the type of the output format (see constants "TYPE_xxx" of this class)
     *
     * @param type output format type
     */
    var type: String?

    /**
     * @return the dimension
     */
    val dimension: String?
    fun setDimension(width: Int, height: Int)

    /**
     * @return the bitrate
     */
    fun getVideoBitrate(): Double

    /**
     * set video bitrate in kbit/s (default 200)
     *
     * @param bitrate the bitrate to set
     */
    fun setVideoBitrate(bitrate: Long)
    /**
     * @return the framerate
     */
    /**
     * sets the framerate (default 25)
     *
     * @param framerate the framerate to set
     */
    var framerate: Double
    /**
     * @return the aspectRatio
     */
    /**
     * sets the aspectRatio (VideoOutput.ASPECT_RATIO_xxx)
     *
     * @param aspectRatio the aspectRatio to set
     */
    var aspectRatio: Int
    fun setAspectRatio(strAspectRatio: String?)

    /**
     * @return the bitrateMin
     */
    fun getVideoBitrateMin(): Double

    /**
     * set min video bitrate tolerance (in kbit/s)
     *
     * @param bitrateMin the bitrateMin to set
     */
    fun setVideoBitrateMin(bitrateMin: Long)

    /**
     * @return the bitrateMax
     */
    fun getVideoBitrateMax(): Double

    /**
     * set max video bitrate tolerance (in kbit/s)
     *
     * @param bitrateMax the bitrateMax to set
     */
    fun setVideoBitrateMax(bitrateMax: Long)

    /**
     * @return the bitrateTolerance
     */
    fun getVideoBitrateTolerance(): Double

    /**
     * set video bitrate tolerance (in kbit/s)
     *
     * @param bitrateTolerance the bitrateTolerance to set
     */
    fun setVideoBitrateTolerance(bitrateTolerance: Long)

    /**
     * @return the audioBitrate
     */
    fun getAudioBitrate(): Double
    /**
     * @return the scanMode
     */
    /**
     * @param scanMode the scanMode to set
     */
    var scanMode: Int

    /**
     * @param audioBitrate the audioBitrate to set
     */
    fun setAudioBitrate(audioBitrate: Long)

    /**
     * @return the videoCodec
     */
    var videoCodec: String?

    /**
     * @return the audioCodec
     */
    var audioCodec: String?
    /**
     * @return the audioSamplerate
     */
    /**
     * @param audioSamplerate the audioSamplerate to set
     */
    var audioSamplerate: Double
    /**
     * @return the bufferSize
     */
    /**
     * @param bufferSize the bufferSize to set
     */
    var bufferSize: Long
    /**
     * @return the pass
     */
    /**
     * @param pass the pass to set
     */
    var pass: Int

    companion object {
        const val TYPE_4XM = "4xm"
        const val TYPE_8BPS = "8bps"
        const val TYPE_AAC = "aac"
        const val TYPE_AASC = "aasc"
        const val TYPE_AC3 = "ac3"
        const val TYPE_ADPCM_4XM = "adpcm_4xm"
        const val TYPE_ADPCM_ADX = "adpcm_adx"
        const val TYPE_ADPCM_CT = "adpcm_ct"
        const val TYPE_ADPCM_EA = "adpcm_ea"
        const val TYPE_ADPCM_IMA_DK3 = "adpcm_ima_dk3"
        const val TYPE_ADPCM_IMA_DK4 = "adpcm_ima_dk4"
        const val TYPE_ADPCM_IMA_QT = "adpcm_ima_qt"
        const val TYPE_ADPCM_IMA_SMJPEG = "adpcm_ima_smjpeg"
        const val TYPE_ADPCM_IMA_WAV = "adpcm_ima_wav"
        const val TYPE_ADPCM_IMA_WS = "adpcm_ima_ws"
        const val TYPE_ADPCM_MS = "adpcm_ms"
        const val TYPE_ADPCM_SBPRO_2 = "adpcm_sbpro_2"
        const val TYPE_ADPCM_SBPRO_3 = "adpcm_sbpro_3"
        const val TYPE_ADPCM_SBPRO_4 = "adpcm_sbpro_4"
        const val TYPE_ADPCM_SWF = "adpcm_swf"
        const val TYPE_ADPCM_XA = "adpcm_xa"
        const val TYPE_ADPCM_YAMAHA = "adpcm_yamaha"
        const val TYPE_ALAC = "alac"
        const val TYPE_AMR_NB = "amr_nb"
        const val TYPE_AMR_WB = "amr_wb"
        const val TYPE_ASV1 = "asv1"
        const val TYPE_ASV2 = "asv2"
        const val TYPE_AVS = "avs"
        const val TYPE_BMP = "bmp"
        const val TYPE_CAMSTUDIO = "camstudio"
        const val TYPE_CAMTASIA = "camtasia"
        const val TYPE_CINEPAK = "cinepak"
        const val TYPE_CLJR = "cljr"
        const val TYPE_COOK = "cook"
        const val TYPE_CYUV = "cyuv"
        const val TYPE_DVBSUB = "dvbsub"
        const val TYPE_DVDSUB = "dvdsub"
        const val TYPE_DVVIDEO = "dvvideo"
        const val TYPE_FFV1 = "ffv1"
        const val TYPE_FFVHUFF = "ffvhuff"
        const val TYPE_FLAC = "flac"
        const val TYPE_FLIC = "flic"
        const val TYPE_FLV = "flv"
        const val TYPE_FRAPS = "fraps"
        const val TYPE_G726 = "g726"
        const val TYPE_H261 = "h261"
        const val TYPE_H263 = "h263"
        const val TYPE_H263I = "h263i"
        const val TYPE_H263P = "h263p"
        const val TYPE_H264 = "h264"
        const val TYPE_HUFFYUV = "huffyuv"
        const val TYPE_IDCINVIDEO = "idcinvideo"
        const val TYPE_INDEO2 = "indeo2"
        const val TYPE_INDEO3 = "indeo3"
        const val TYPE_INTERPLAY_DPCM = "interplay_dpcm"
        const val TYPE_INTERPLAYVIDEO = "interplayvideo"
        const val TYPE_JPEGLS = "jpegls"
        const val TYPE_KMVC = "kmvc"
        const val TYPE_LJPEG = "ljpeg"
        const val TYPE_LOCO = "loco"
        const val TYPE_MACE3 = "mace3"
        const val TYPE_MACE6 = "mace6"
        const val TYPE_MDEC = "mdec"
        const val TYPE_MJPEG = "mjpeg"
        const val TYPE_MJPEGB = "mjpegb"
        const val TYPE_MMVIDEO = "mmvideo"
        const val TYPE_MP2 = "mp2"
        const val TYPE_MP3 = "mp3"
        const val TYPE_MP3ADU = "mp3adu"
        const val TYPE_MP3ON4 = "mp3on4"
        const val TYPE_MPEG1VIDEO = "mpeg1video"
        const val TYPE_MPEG2VIDEO = "mpeg2video"
        const val TYPE_MPEG4 = "mpeg4"
        const val TYPE_MPEG4AAC = "mpeg4aac"
        const val TYPE_MPEGVIDEO = "mpegvideo"
        const val TYPE_MSMPEG4 = "msmpeg4"
        const val TYPE_MSMPEG4V1 = "msmpeg4v1"
        const val TYPE_MSMPEG4V2 = "msmpeg4v2"
        const val TYPE_MSRLE = "msrle"
        const val TYPE_MSVIDEO1 = "msvideo1"
        const val TYPE_MSZH = "mszh"
        const val TYPE_NUV = "nuv"
        const val TYPE_PAM = "pam"
        const val TYPE_PBM = "pbm"
        const val TYPE_PCM_ALAW = "pcm_alaw"
        const val TYPE_PCM_MULAW = "pcm_mulaw"
        const val TYPE_PCM_S16BE = "pcm_s16be"
        const val TYPE_PCM_S16LE = "pcm_s16le"
        const val TYPE_PCM_S24BE = "pcm_s24be"
        const val TYPE_PCM_S24DAUD = "pcm_s24daud"
        const val TYPE_PCM_S24LE = "pcm_s24le"
        const val TYPE_PCM_S32BE = "pcm_s32be"
        const val TYPE_PCM_S32LE = "pcm_s32le"
        const val TYPE_PCM_S8 = "pcm_s8"
        const val TYPE_PCM_U16BE = "pcm_u16be"
        const val TYPE_PCM_U16LE = "pcm_u16le"
        const val TYPE_PCM_U24BE = "pcm_u24be"
        const val TYPE_PCM_U24LE = "pcm_u24le"
        const val TYPE_PCM_U32BE = "pcm_u32be"
        const val TYPE_PCM_U32LE = "pcm_u32le"
        const val TYPE_PCM_U8 = "pcm_u8"
        const val TYPE_PGM = "pgm"
        const val TYPE_PGMYUV = "pgmyuv"
        const val TYPE_PNG = "png"
        const val TYPE_PPM = "ppm"
        const val TYPE_QDM2 = "qdm2"
        const val TYPE_QDRAW = "qdraw"
        const val TYPE_QPEG = "qpeg"
        const val TYPE_QTRLE = "qtrle"
        const val TYPE_RAWVIDEO = "rawvideo"
        const val TYPE_REAL_144 = "real_144"
        const val TYPE_REAL_288 = "real_288"
        const val TYPE_ROQ_DPCM = "roq_dpcm"
        const val TYPE_ROQVIDEO = "roqvideo"
        const val TYPE_RPZA = "rpza"
        const val TYPE_RV10 = "rv10"
        const val TYPE_RV20 = "rv20"
        const val TYPE_SHORTEN = "shorten"
        const val TYPE_SMACKAUD = "smackaud"
        const val TYPE_SMACKVID = "smackvid"
        const val TYPE_SMC = "smc"
        const val TYPE_SNOW = "snow"
        const val TYPE_SOL_DPCM = "sol_dpcm"
        const val TYPE_SONIC = "sonic"
        const val TYPE_SONICLS = "sonicls"
        const val TYPE_SP5X = "sp5x"
        const val TYPE_SVQ1 = "svq1"
        const val TYPE_SVQ3 = "svq3"
        const val TYPE_THEORA = "theora"
        const val TYPE_TRUEMOTION1 = "truemotion1"
        const val TYPE_TRUEMOTION2 = "truemotion2"
        const val TYPE_TRUESPEECH = "truespeech"
        const val TYPE_TTA = "tta"
        const val TYPE_ULTIMOTION = "ultimotion"
        const val TYPE_VC9 = "vc9"
        const val TYPE_VCR1 = "vcr1"
        const val TYPE_VMDAUDIO = "vmdaudio"
        const val TYPE_VMDVIDEO = "vmdvideo"
        const val TYPE_VORBIS = "vorbis"
        const val TYPE_VP3 = "vp3"
        const val TYPE_VQAVIDEO = "vqavideo"
        const val TYPE_WMAV1 = "wmav1"
        const val TYPE_WMAV2 = "wmav2"
        const val TYPE_WMV1 = "wmv1"
        const val TYPE_WMV2 = "wmv2"
        const val TYPE_WNV1 = "wnv1"
        const val TYPE_WS_SND1 = "ws_snd1"
        const val TYPE_XAN_DPCM = "xan_dpcm"
        const val TYPE_XAN_WC3 = "xan_wc3"
        const val TYPE_XL = "xl"
        const val TYPE_XVID = "xvid"
        const val TYPE_ZLIB = "zlib"
        const val TYPE_ZMBV = "zmbv"
        const val ASPECT_RATIO_16_9 = 1
        const val ASPECT_RATIO_4_3 = 2
        const val ASPECT_RATIO_1_33333 = ASPECT_RATIO_4_3
        const val ASPECT_RATIO_1_77777 = ASPECT_RATIO_16_9
        const val SCAN_MODE_INTERLACED = 1
        const val SCAN_MODE_PROGRESSIV = 2
    }
}