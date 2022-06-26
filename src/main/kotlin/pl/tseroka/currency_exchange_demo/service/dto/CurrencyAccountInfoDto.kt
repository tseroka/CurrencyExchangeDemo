package pl.tseroka.currency_exchange_demo.service.dto

import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import java.math.BigDecimal

data class CurrencyAccountInfoDto(
    val name: String,
    val surname: String,
    val pesel: String,
    val currenciesBalances: Map<CurrencyCode, BigDecimal>
)