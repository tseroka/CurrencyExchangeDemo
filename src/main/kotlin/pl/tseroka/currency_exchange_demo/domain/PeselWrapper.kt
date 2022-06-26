package pl.tseroka.currency_exchange_demo.domain

import pl.foltak.polishidnumbers.pesel.Pesel
import java.time.LocalDate

class PeselWrapper(val value: String) {
    private val pesel = Pesel(value)

    val birthDate: LocalDate
        get() = pesel.birthDate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeselWrapper

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "PeselWrapper(value='$value')"
    }
}
