package de.stefanbissell.bots.numbsi

fun <T> List<T>.limit(count: Int) =
    if (size <= count) {
        this
    } else {
        subList(0, count)
    }
