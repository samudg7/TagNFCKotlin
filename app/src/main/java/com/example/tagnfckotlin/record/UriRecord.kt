package com.example.tagnfckotlin.record


import com.example.tagnfckotlin.record.ParsedNdefRecord
import android.net.Uri
import android.nfc.NdefRecord
import com.google.common.base.Preconditions
import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import com.google.common.primitives.Bytes
import java.nio.charset.Charset
import java.util.*

/**
 * A parsed record containing a Uri.
 */
class UriRecord(uri: Uri) : ParsedNdefRecord {
    val uri: Uri
    override fun str(): String {
        return uri.toString()
    }

    companion object {
        private const val TAG = "UriRecord"
        const val RECORD_TYPE = "UriRecord"

        /**
         * NFC Forum "URI Record Type Definition"
         *
         * This is a mapping of "URI Identifier Codes" to URI string prefixes,
         * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
         */
        private val URI_PREFIX_MAP: BiMap<Byte, String> = ImmutableBiMap.builder<Byte, String>()
        .put(0x00 as kotlin.Byte, "")
        .put(0x01 as kotlin.Byte, "http://www.")
        .put(0x02 as kotlin.Byte, "https://www.")
        .put(0x03 as kotlin.Byte, "http://")
        .put(0x04 as kotlin.Byte, "https://")
        .put(0x05 as kotlin.Byte, "tel:")
        .put(0x06 as kotlin.Byte, "mailto:")
        .put(0x07 as kotlin.Byte, "ftp://anonymous:anonymous@")
        .put(0x08 as kotlin.Byte, "ftp://ftp.")
        .put(0x09 as kotlin.Byte, "ftps://")
        .put(0x0A as kotlin.Byte, "sftp://")
        .put(0x0B as kotlin.Byte, "smb://")
        .put(0x0C as kotlin.Byte, "nfs://")
        .put(0x0D as kotlin.Byte, "ftp://")
        .put(0x0E as kotlin.Byte, "dav://")
        .put(0x0F as kotlin.Byte, "news:")
        .put(0x10 as kotlin.Byte, "telnet://")
        .put(0x11 as kotlin.Byte, "imap:")
        .put(0x12 as kotlin.Byte, "rtsp://")
        .put(0x13 as kotlin.Byte, "urn:")
        .put(0x14 as kotlin.Byte, "pop:")
        .put(0x15 as kotlin.Byte, "sip:")
        .put(0x16 as kotlin.Byte, "sips:")
        .put(0x17 as kotlin.Byte, "tftp:")
        .put(0x18 as kotlin.Byte, "btspp://")
        .put(0x19 as kotlin.Byte, "btl2cap://")
        .put(0x1A as kotlin.Byte, "btgoep://")
        .put(0x1B as kotlin.Byte, "tcpobex://")
        .put(0x1C as kotlin.Byte, "irdaobex://")
        .put(0x1D as kotlin.Byte, "file://")
        .put(0x1E as kotlin.Byte, "urn:epc:id:")
        .put(0x1F as kotlin.Byte, "urn:epc:tag:")
        .put(0x20 as kotlin.Byte, "urn:epc:pat:")
        .put(0x21 as kotlin.Byte, "urn:epc:raw:")
        .put(0x22 as kotlin.Byte, "urn:epc:")
        .put(0x23 as kotlin.Byte, "urn:nfc:")
        .build()
        /**
         * Convert [NdefRecord] into a [Uri].
         * This will handle both TNF_WELL_KNOWN / RTD_URI and TNF_ABSOLUTE_URI.
         *
         * @throws IllegalArgumentException if the NdefRecord is not a record
         * containing a URI.
         */
        fun parse(record: NdefRecord): UriRecord {
            val tnf = record.tnf
            if (tnf == NdefRecord.TNF_WELL_KNOWN) {
                return parseWellKnown(record)
            } else if (tnf == NdefRecord.TNF_ABSOLUTE_URI) {
                return parseAbsolute(record)
            }
            throw IllegalArgumentException("Unknown TNF $tnf")
        }

        /** Parse and absolute URI record  */
        private fun parseAbsolute(record: NdefRecord): UriRecord {
            val payload = record.payload
            val uri = Uri.parse(String(payload, Charset.forName("UTF-8")))
            return UriRecord(uri)
        }

        /** Parse an well known URI record  */
        private fun parseWellKnown(record: NdefRecord): UriRecord {
            Preconditions.checkArgument(Arrays.equals(record.type, NdefRecord.RTD_URI))
            val payload = record.payload
            /*
         * payload[0] contains the URI Identifier Code, per the
         * NFC Forum "URI Record Type Definition" section 3.2.2.
         *
         * payload[1]...payload[payload.length - 1] contains the rest of
         * the URI.
         */
            val prefix: String? = URI_PREFIX_MAP.get(payload[0])
            val fullUri: ByteArray = Bytes.concat(prefix?.toByteArray(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1,
                    payload.size))
            val uri = Uri.parse(String(fullUri, Charset.forName("UTF-8")))
            return UriRecord(uri)
        }

        fun isUri(record: NdefRecord): Boolean {
            return try {
                parse(record)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        private val EMPTY = ByteArray(0)
    }

    init {
        this.uri = Preconditions.checkNotNull(uri)
    }
}