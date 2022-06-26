package pl.tseroka.currency_exchange_demo.domain

import java.math.BigDecimal
import java.math.MathContext

typealias CurrenciesBalances = MutableMap<CurrencyCode, BigDecimal>

class CurrencyAccount(name: String, surname: String, val pesel: PeselWrapper, val currenciesBalances: CurrenciesBalances) {

    companion object {
        fun create(name: String, surname: String, pesel: String, initialPlnBalance: BigDecimal?): CurrencyAccount {
            return CurrencyAccount(
                name = name, surname = surname, pesel = PeselWrapper(pesel),
                currenciesBalances = mutableMapOf(
                    CurrencyCode.PLN to (initialPlnBalance ?: BigDecimal.ZERO),
                    CurrencyCode.USD to BigDecimal.ZERO
                )
            )
        }
    }

    var name: String = name
        private set
    var surname: String = surname
        private set

    fun exchange(base: CurrencyCode, amount: BigDecimal, target: CurrencyCode, exchangeRate: BigDecimal) {
        if (currenciesBalances.containsKey(base) && currenciesBalances.containsKey(target)) {
            val currentBalance = currenciesBalances[base]!!
            if (currentBalance < amount) {
                throw IllegalArgumentException("Cannot exchange more of the $base currency than is possessed")
            }
            currenciesBalances[base] = currentBalance.minus(amount)
            currenciesBalances[target] = currenciesBalances[target]!!.plus(amount.multiply(exchangeRate, MathContext.DECIMAL128))
        } else {
            throw NoSuchElementException("Cannot find currencies for $base/$target exchange")
        }
    }

    fun deepCopy(): CurrencyAccount {
        return CurrencyAccount(
            this.name,
            this.surname,
            PeselWrapper(this.pesel.value),
            currenciesBalances = this.currenciesBalances.entries.asSequence().map { Pair(it.key, it.value) }.toMap().toMutableMap()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CurrencyAccount

        if (pesel != other.pesel) return false

        return true
    }

    override fun hashCode(): Int {
        return pesel.hashCode()
    }
}
