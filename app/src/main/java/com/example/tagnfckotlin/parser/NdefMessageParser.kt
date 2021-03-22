package com.example.tagnfckotlin.parser

import com.example.tagnfckotlin.record.SmartPoster
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.example.tagnfckotlin.record.ParsedNdefRecord
import com.example.tagnfckotlin.record.TextRecord
import com.example.tagnfckotlin.record.UriRecord
import java.util.*

//class com.example.tagnfckotlin.parser.NdefMessageParser {
//}

/*
 * Copyright (C) 2010 The Android Open Source Project
 * Modified by Sylvain Saurel for a tutorial
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
//package com.ssaurel.nfcreader.parser;

object NdefMessageParser {
    fun parse(message: NdefMessage): List<ParsedNdefRecord> {
        return com.example.tagnfckotlin.parser.NdefMessageParser.getRecords(message.records)
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