package pl.tseroka.currency_exchange_demo.service.command

import java.math.BigDecimal

data class CreateCurrencyAccountCommand(
    val name: String,
    val surname: String,
    val pesel: String,
    val initialPlnBalance: BigDecimal?
)
