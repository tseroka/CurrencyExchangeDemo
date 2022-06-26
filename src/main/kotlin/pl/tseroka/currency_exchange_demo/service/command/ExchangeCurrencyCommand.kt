package pl.tseroka.currency_exchange_demo.service.command

import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import java.math.BigDecimal

data class ExchangeCurrencyCommand(
    val baseCurrency: CurrencyCode,
    val amountToExchange: BigDecimal,
    val targetCurrency: CurrencyCode,
    val pesel: String
)
