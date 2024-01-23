package com.identic.fluentforge.reader.epub.models

data class EpubChapter(
    val absPath: String,
    val title: String,
    val body: String
)