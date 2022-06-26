package pl.tseroka.currency_exchange_demo.infrastructure.memory

import org.springframework.stereotype.Repository
import pl.tseroka.currency_exchange_demo.domain.CurrencyAccount
import pl.tseroka.currency_exchange_demo.domain.CurrencyAccountRepository
import pl.tseroka.currency_exchange_demo.domain.PeselWrapper

@Repository
class InMemoryCurrencyAccountRepository : CurrencyAccountRepository {
    private val accountsByPesel = mutableMapOf<PeselWrapper, CurrencyAccount>()

    override fun save(account: CurrencyAccount): CurrencyAccount {
        accountsByPesel[account.pesel] = account
        return account.deepCopy()
    }

    override fun findByPesel(peselValue: String): CurrencyAccount? {
        return accountsByPesel[PeselWrapper(peselValue)]?.deepCopy()
    }

    override fun clear() {
        accountsByPesel.clear()
    }
}
