package pl.tseroka.currency_exchange_demo.config

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.ZonedDateTime

@Component
class DateTimeProvider {
    private val clock: Clock = Clock.systemUTC()

    fun currentDateTime(): ZonedDateTime {
        return ZonedDateTime.now(clock)
    }
}