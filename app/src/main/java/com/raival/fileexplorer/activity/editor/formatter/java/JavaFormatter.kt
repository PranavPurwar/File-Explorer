package com.raival.fileexplorer.activity.editor.formatter.java

import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange
import java.util.*
import java.util.stream.Collectors

class JavaFormatter : Formatter {
    private var receiver: Formatter.FormatResultReceiver? = null
    private var isRunning = false

    override fun format(text: Content, cursorRange: TextRange) {
        isRunning = true
        receiver?.onFormatSucceed(format(text.toString()), cursorRange)
        isRunning = false
    }

    override fun formatRegion(text: Content, rangeToFormat: TextRange, cursorRange: TextRange) {
        isRunning = true
        val line = text.getLine(rangeToFormat.start.line)
        val indents = if (line.trim().isEmpty()) line else line.substring(
            0,
            line.indexOf(line.trim().toString())
        )
        val textToFormat = text.subContent(
            rangeToFormat.start.line,
            rangeToFormat.start.column,
            rangeToFormat.end.line,
            rangeToFormat.end.column
        )
        val formattedRegion = format(textToFormat.toString()).split("\n")
            .stream().map {
                indents.toString() + it
            }.collect(Collectors.joining("\n"))

        text.replace(
            rangeToFormat.start.line,
            rangeToFormat.start.column,
            rangeToFormat.end.line,
            rangeToFormat.end.column,
            formattedRegion
        )

        receiver?.onFormatSucceed(text.toString(), cursorRange)
        isRunning = false
    }

    override fun setReceiver(receiver: Formatter.FormatResultReceiver?) {
        this.receiver = receiver
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    override fun destroy() {
        receiver = null
    }

    private fun format(source: String): String {
        val result = StringBuilder()
        val charArray = trimSource(source).toCharArray()
        val length = charArray.size
        var index = 0
        var indents = 0
        var isSingleLineComment = false
        var isMultiLineComment = false
        var isJavaDoc = false
        var isEscape = false
        var isChar = false
        var isString = false

        while (index < length) {
            val currentChar = charArray[index]
            val nextCharIndex = index + 1
            val isValidNextChar = isValidIndex(nextCharIndex, charArray)
            if (isSingleLineComment) {
                if (currentChar == '\n') {
                    result.append(currentChar)
                    addIndent(result, indents)
                    isSingleLineComment = false
                } else {
                    result.append(currentChar)
                }
            } else if (isEscape) {
                result.append(currentChar)
                isEscape = false
            } else if (currentChar == '\\') {
                result.append(currentChar)
                isEscape = true
            } else if (isChar) {
                if (currentChar == '\'') {
                    result.append(currentChar)
                    isChar = false
                } else {
                    result.append(currentChar)
                }
            } else if (isString) {
                if (currentChar == '\"') {
                    result.append(currentChar)
                    isString = false
                } else {
                    result.append(currentChar)
                }
            } else {
                if (isMultiLineComment) {
                    if (currentChar == '*') {
                        if (isValidNextChar) {
                            val nextChar = charArray[nextCharIndex]
                            if (nextChar == '/') {
                                isMultiLineComment = false
                                isJavaDoc = false
                            }
                        }
                    }
                } else {
                    if (currentChar == '/') {
                        if (isValidNextChar) {
                            val nextChar = charArray[nextCharIndex]
                            if (nextChar == '/') {
                                result.append(currentChar)
                                result.append(nextChar)
                                isSingleLineComment = true
                                index = nextCharIndex + 1
                                continue
                            }
                            if (nextChar == '*') {
                                result.append(currentChar)
                                result.append(nextChar)
                                isMultiLineComment = true
                                index = nextCharIndex + 1
                                continue
                            }
                        }
                    }
                    if (currentChar == '\'') {
                        isChar = true
                    }
                    if (currentChar == '\"') {
                        isString = true
                    }
                }
                if (!isJavaDoc) {
                    if (currentChar == '{') {
                        ++indents
                    }
                    if (currentChar == '}') {
                        --indents
                        if (result[result.length - 1] == '\t') {
                            result.deleteCharAt(result.length - 1)
                        }
                    }
                }
                result.append(currentChar)
                if (currentChar == '\n') {
                    addIndent(result, indents)
                    if (isMultiLineComment) {
                        if (isValidNextChar) {
                            val nextChar = charArray[nextCharIndex]
                            if (nextChar == '*') {
                                isJavaDoc = true
                                result.append(" ")
                            } else {
                                isJavaDoc = false
                            }
                        }
                    }
                    if (isValidNextChar) {
                        val nextChar = charArray[nextCharIndex]
                        if (nextChar == '.' || nextChar == '?' || nextChar == ':' || nextChar == '&' || nextChar == '|' || nextChar == '+') {
                            result.append("\t")
                        }
                    }
                }
            }
            ++index
        }
        return result.toString()
    }

    private fun isValidIndex(index: Int, source: CharArray): Boolean {
        return index < source.size && index > -1
    }

    private fun addIndent(builder: StringBuilder, indents: Int) {
        for (i in 0 until indents) {
            builder.append('\t')
        }
    }

    private fun trimSource(source: String): String {
        return Arrays.stream(source.split("\n").toTypedArray())
            .map { obj: String -> obj.trim { it <= ' ' } }
            .collect(Collectors.joining("\n"))
    }
}