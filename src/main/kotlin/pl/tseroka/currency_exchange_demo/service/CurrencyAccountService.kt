package pl.tseroka.currency_exchange_demo.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.tseroka.currency_exchange_demo.config.DateTimeProvider
import pl.tseroka.currency_exchange_demo.domain.CurrencyAccount
import pl.tseroka.currency_exchange_demo.domain.CurrencyAccountRepository
import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import pl.tseroka.currency_exchange_demo.domain.PeselWrapper
import pl.tseroka.currency_exchange_demo.service.command.CreateCurrencyAccountCommand
import pl.tseroka.currency_exchange_demo.service.command.ExchangeCurrencyCommand
import pl.tseroka.currency_exchange_demo.service.dto.CurrencyAccountInfoDto
import pl.tseroka.currency_exchange_demo.service.query.CurrencyAccountInfoByPeselQuery
import java.math.BigDecimal
import java.math.MathContext
import java.time.temporal.ChronoUnit

@Service
class CurrencyAccountService(
    private val dateTimeProvider: DateTimeProvider,
    private val currencyAccountRepository: CurrencyAccountRepository,
    private val nbpClient: NbpClient
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CurrencyAccountService::class.java)
        private const val ALLOWED_AGE_THRESHOLD: Long = 18
    }

    fun createNewAccount(command: CreateCurrencyAccountCommand) {
        log.info("Received request to create new currency account: {}", command)
        validateAllowedAge(command.pesel)
        validateUniqueness(command.pesel)
        currencyAccountRepository.save(
            CurrencyAccount.create(
                name = command.name,
                surname = command.surname,
                pesel = command.pesel,
                initialPlnBalance =  command.initialPlnBalance
            )
        )
        log.info("Successfully created new currency account: {}", command)
    }


    private fun validateAllowedAge(pesel: String) {
        log.debug("Validating allowed age for PESEL number: {}", pesel)
        val age = ChronoUnit.YEARS.between(
            PeselWrapper(pesel).birthDate, dateTimeProvider.currentDateTime().toLocalDate()
        )
        if (age < ALLOWED_AGE_THRESHOLD) {
            throw IllegalArgumentException("Cannot create currency account for user which is equal to or above allowed age")
        }
    }

    private fun validateUniqueness(pesel: String) {
        log.debug("Validating uniqueness of PESEL number: {}", pesel)
        if (currencyAccountRepository.findByPesel(pesel) != null) {
            throw IllegalStateException("PESEL number already in use by other account")
        }
    }

    fun getAccountInfoByPesel(query: CurrencyAccountInfoByPeselQuery): CurrencyAccountInfoDto {
        return getAccountByPesel(query.pesel).let { account ->
            CurrencyAccountInfoDto(
                name = account.name,
                surname = account.surname,
                pesel = account.pesel.value,
                currenciesBalances = account.currenciesBalances
            )
        }
    }

    private fun getAccountByPesel(pesel: String): CurrencyAccount {
        return currencyAccountRepository.findByPesel(pesel)
            ?: throw NoSuchElementException("There is no account assigned to specified PESEL")
    }

    fun exchange(command: ExchangeCurrencyCommand) {
        require(command.baseCurrency != command.targetCurrency) {
            "Base and target currency cannot be the same"
        }
        val account = getAccountByPesel(command.pesel)
        account.exchange(
            base = command.baseCurrency,
            amount = command.amountToExchange,
            target = command.targetCurrency,
            exchangeRate = getExchangeRate(command.baseCurrency, command.targetCurrency)
        )
        currencyAccountRepository.save(account)
    }

    private fun getExchangeRate(base: CurrencyCode, target: CurrencyCode): BigDecimal {
        return if (target != CurrencyCode.PLN) {
            BigDecimal.ONE.divide(nbpClient.getExchangeRate(target), MathContext.DECIMAL128)
        } else {
            nbpClient.getExchangeRate(base)
        }
    }
}
