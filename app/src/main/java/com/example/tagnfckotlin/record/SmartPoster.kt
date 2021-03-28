package com.example.tagnfckotlin.record

//import com.google.common.collect.ImmutableMap
//import com.google.common.collect.Iterables
//class com.example.tagnfckotlin.record.SmartPoster {
//}

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.example.tagnfckotlin.parser.NdefMessageParser
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import java.util.*


/**
 * A representation of an NFC Forum "Smart Poster".
 */
class SmartPoster(uri: UriRecord?, title: TextRecord?, action: com.example.tagnfckotlin.record.SmartPoster.RecommendedAction?, type: String) : ParsedNdefRecord {
    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The Title record for the service (there can be many of these in
     * different languages, but a language MUST NOT be repeated). This record is
     * optional."
     */
    private val mTitleRecord: TextRecord?

    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The URI record. This is the core of the Smart Poster, and all other
     * records are just metadata about this record. There MUST be one URI record
     * and there MUST NOT be more than one."
     */
    private val mUriRecord: UriRecord

    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The Action record. This record describes how the service should be
     * treated. For example, the action may indicate that the device should save
     * the URI as a bookmark or open a browser. The Action record is optional.
     * If it does not exist, the device may decide what to do with the service.
     * If the action record exists, it should be treated as a strong suggestion;
     * the UI designer may ignore it, but doing so will induce a different user
     * experience from device to device."
     */
    private val mAction: com.example.tagnfckotlin.record.SmartPoster.RecommendedAction

    /**
     * NFC Forum Smart Poster Record Type Definition section 3.2.1.
     *
     * "The Type record. If the URI references an external entity (e.g., via a
     * URL), the Type record may be used to declare the MIME type of the entity.
     * This can be used to tell the mobile device what kind of an object it can
     * expect before it opens the connection. The Type record is optional."
     */
    private val mType: String
    val uriRecord: UriRecord
        get() = mUriRecord

    /**
     * Returns the title of the smart poster. This may be `null`.
     */
    val title: TextRecord?
        get() = mTitleRecord

     override fun str(): String? {
        return if (mTitleRecord != null) {
            mTitleRecord.str().toString() + "\n" + mUriRecord.str()
        } else ({
            mUriRecord.str()
        }).toString()
    }

    enum class RecommendedAction(private val byte: Byte) {
        UNKNOWN((-1).toByte()), DO_ACTION(0.toByte()), SAVE_FOR_LATER(1.toByte()), OPEN_FOR_EDITING(
                2.toByte());

        companion object {
            var LOOKUP: ImmutableMap<Byte, RecommendedAction>? = null

            init {
                val builder: ImmutableMap.Builder<Byte, RecommendedAction> = ImmutableMap.builder()
                for (action in values()) {
                    //com.example.tagnfckotlin.record.builder.put(com.example.tagnfckotlin.record.action.getByte(), com.example.tagnfckotlin.record.action)
                    builder.put(action.getByte(), action)

                }
                LOOKUP = builder.build()
            }

        }

        private var mAction: Byte = 0
        private fun RecommendedAction(`val`: Byte) {
            mAction = `val`
        }
        private fun getByte(): Byte {
            
            return mAction
        }

    }

    companion object {
        fun parse(record: NdefRecord?): SmartPoster {
            Preconditions.checkArgument(record!!.tnf == NdefRecord.TNF_WELL_KNOWN)
            Preconditions.checkArgument(Arrays.equals(record.type, NdefRecord.RTD_SMART_POSTER))
            return try {
                val subRecords = NdefMessage(record.payload)
                parse(subRecords.records)
            } catch (e: FormatException) {
                throw IllegalArgumentException(e)
            }
        }

        fun parse(recordsRaw: Array<NdefRecord?>?): com.example.tagnfckotlin.record.SmartPoster {
            return try {
                val records: Iterable<ParsedNdefRecord> = NdefMessageParser.getRecords(recordsRaw)
                val uri: UriRecord = Iterables.getOnlyElement(Iterables.filter(records, UriRecord::class.java))
                val title: TextRecord? = com.example.tagnfckotlin.record.SmartPoster.Companion.getFirstIfExists(records, TextRecord::class.java)
                val action: RecommendedAction? = com.example.tagnfckotlin.record.SmartPoster.Companion.parseRecommendedAction(recordsRaw)
                val type: String? = com.example.tagnfckotlin.record.SmartPoster.Companion.parseType(recordsRaw)
                com.example.tagnfckotlin.record.SmartPoster(uri, title, action, type.toString())
            } catch (e: NoSuchElementException) {
                throw IllegalArgumentException(e)
            }
        }

        fun isPoster(record: NdefRecord?): Boolean {
            return try {
                //com.example.tagnfckotlin.record.SmartPoster.Companion.parse(record)
                parse(record)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        /**
         * Returns the first element of `elements` which is an instance of
         * `type`, or `null` if no such element exists.
         */
        private fun <T> getFirstIfExists(elements: Iterable<*>, type: Class<T>): T? {
            val filtered: Iterable<T> = Iterables.filter(elements, type)
            var instance: T? = null
            if (!Iterables.isEmpty(filtered)) {
                instance = Iterables.get(filtered, 0)
            }
            return instance
        }

        private fun getByType(type: ByteArray, records: Array<NdefRecord?>?): NdefRecord? {
            for (record in records!!) {
                if (Arrays.equals(type, record?.type)) {
                    return record
                }
            }
            return null
        }

        private val ACTION_RECORD_TYPE = byteArrayOf('a'.toByte(), 'c'.toByte(), 't'.toByte())
        private fun parseRecommendedAction(records: Array<NdefRecord?>?): RecommendedAction? {
            val record: NdefRecord = com.example.tagnfckotlin.record.SmartPoster.Companion.getByType(com.example.tagnfckotlin.record.SmartPoster.Companion.ACTION_RECORD_TYPE, records)
                    ?: return com.example.tagnfckotlin.record.SmartPoster.RecommendedAction.UNKNOWN
            val action = record.payload[0]
            return if (com.example.tagnfckotlin.record.SmartPoster.RecommendedAction.Companion.LOOKUP?.containsKey(action) == true) {
                com.example.tagnfckotlin.record.SmartPoster.RecommendedAction.Companion.LOOKUP!!.get(action)
            } else com.example.tagnfckotlin.record.SmartPoster.RecommendedAction.UNKNOWN
        }
        
        private val TYPE_TYPE = byteArrayOf('t'.toByte())
        private fun parseType(records: Array<NdefRecord?>?): String? {
            val type: NdefRecord = com.example.tagnfckotlin.record.SmartPoster.Companion.getByType(com.example.tagnfckotlin.record.SmartPoster.Companion.TYPE_TYPE, records)
                    ?: return null
            return String(type.payload, Charsets.UTF_8)
        }
    }

    init {
        mUriRecord = Preconditions.checkNotNull(uri)!!
        mTitleRecord = title
        mAction = Preconditions.checkNotNull(action)!!
        mType = type
    }
}