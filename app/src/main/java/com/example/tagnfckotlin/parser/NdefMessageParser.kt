package com.example.tagnfckotlin.parser

import com.example.tagnfckotlin.record.SmartPoster
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.example.tagnfckotlin.record.ParsedNdefRecord
import com.example.tagnfckotlin.record.TextRecord
import com.example.tagnfckotlin.record.UriRecord
import java.util.*

object NdefMessageParser {
    fun parse(message: NdefMessage): List<ParsedNdefRecord> {
        //return com.example.tagnfckotlin.parser.NdefMessageParser.getRecords(message.records)
        return getRecords(message.records)

    }

    fun getRecords(records: Array<NdefRecord?>?): List<ParsedNdefRecord> {
        val elements: MutableList<ParsedNdefRecord> = ArrayList<ParsedNdefRecord>()
        if (records != null) {
            for (record in records) {
                if (record?.let { UriRecord.isUri(it) } == true) {
                    elements.add(UriRecord.parse(record))
                } else if (record?.let { TextRecord.isText(it) } == true) {
                    elements.add(TextRecord.parse(record))
                } else if (SmartPoster.isPoster(record)) {
                    record?.let { SmartPoster.parse(it) }?.let { elements.add(it) }
                } else {
                    //elements.add(object : ParsedNdefRecord() {
                    elements.add(object : ParsedNdefRecord {
                        override fun str(): String {
                            return record?.let { String(bytes = it?.payload) }.toString()
                        }
                    })
                }
            }
        }
        return elements
    }
}