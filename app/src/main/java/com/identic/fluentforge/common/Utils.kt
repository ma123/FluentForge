package com.identic.fluentforge.common

import kotlin.math.roundToInt

class Utils {
    companion object {
        fun levenshteinDistancePercent(str1: String, str2: String): Int {
            val distance = levenshteinDistance(str1, str2)
            val maxLength = maxOf(str1.length, str2.length)
            return (((maxLength - distance) / maxLength.toDouble()) * 100).roundToInt()
        }

        private fun levenshteinDistance(str1: String, str2: String): Int {
            val filtered = ",.?!"
            val removedStr1 = str1.filterNot { filtered.indexOf(it) > -1 }.lowercase()
            val removedStr2 = str2.filterNot { filtered.indexOf(it) > -1 }.lowercase()

            val lenStr1 = removedStr1.length + 1
            val lenStr2 = removedStr2.length + 1

            val matrix = Array(lenStr1) { IntArray(lenStr2) }

            for (i in 0 until lenStr1) {
                for (j in 0 until lenStr2) {
                    if (i == 0) {
                        matrix[i][j] = j
                    } else if (j == 0) {
                        matrix[i][j] = i
                    } else {
                        matrix[i][j] = minOf(
                            matrix[i - 1][j] + 1,
                            matrix[i][j - 1] + 1,
                            matrix[i - 1][j - 1] + if (removedStr1[i - 1] == removedStr2[j - 1]) 0 else 1
                        )
                    }
                }
            }

            return matrix[lenStr1 - 1][lenStr2 - 1]
        }
    }
}