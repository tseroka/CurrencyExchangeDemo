package pl.tseroka.currency_exchange_demo.domain

interface CurrencyAccountRepository {
    fun save(account: CurrencyAccount): CurrencyAccount

    fun findByPesel(peselValue: String): CurrencyAccount?

    fun clear()
}
