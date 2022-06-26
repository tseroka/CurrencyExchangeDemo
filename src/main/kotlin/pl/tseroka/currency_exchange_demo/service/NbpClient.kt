package pl.tseroka.currency_exchange_demo.service

import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import java.math.BigDecimal

interface NbpClient {
    fun getExchangeRate(target: CurrencyCode): BigDecimal
}
