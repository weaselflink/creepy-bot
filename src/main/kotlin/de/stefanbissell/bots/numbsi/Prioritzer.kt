package de.stefanbissell.bots.numbsi

import java.util.function.Predicate

fun List<BotUnit>.prioritize(
    vararg conditions: Predicate<BotUnit>
): List<BotUnit> {
    return conditions
        .map { condition ->
            filter { condition.test(it) }
        }
        .lastOrNull {
            it.isNotEmpty()
        }
        ?: this
}

