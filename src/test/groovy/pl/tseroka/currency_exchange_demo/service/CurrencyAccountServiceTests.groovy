package pl.tseroka.currency_exchange_demo.service

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.tseroka.currency_exchange_demo.config.DateTimeProvider
import pl.tseroka.currency_exchange_demo.domain.CurrencyAccountRepository
import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import pl.tseroka.currency_exchange_demo.service.command.CreateCurrencyAccountCommand
import pl.tseroka.currency_exchange_demo.service.command.ExchangeCurrencyCommand
import pl.tseroka.currency_exchange_demo.service.query.CurrencyAccountInfoByPeselQuery
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest
class CurrencyAccountServiceTests extends Specification {

    @Autowired
    CurrencyAccountRepository currencyAccountRepository

    @Autowired
    CurrencyAccountService currencyAccountService

    @SpringBean
    NbpClient nbpClient = Mock()

    @SpringBean
    DateTimeProvider dateTimeProvider = Mock()

    def setup() {
        currencyAccountRepository.clear()
    }

    def "test creating currency account"() {
        given: "today is future far, far away"
        dateTimeProvider.currentDateTime() >> ZonedDateTime.now().plusYears(999999)

        when: "account is created"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Jan", "Nowak", "56041500234", BigDecimal.TEN)
        )
        and: "other account is created"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Anna", "Kowalska", "44031921501", BigDecimal.ONE)
        )

        then: "account can be fetched by PESEL number"
        currencyAccountService.getAccountInfoByPesel(new CurrencyAccountInfoByPeselQuery("56041500234")).with {
            name == "Jan"
            surname == "Nowak"
            pesel == "56041500234"
            currenciesBalances == Map.of(CurrencyCode.PLN, BigDecimal.TEN, CurrencyCode.USD, BigDecimal.ZERO)
        }
    }

    def "creating account for PESEL number which already has corresponding account should throw exception"() {
        given: "today is future far, far away"
        dateTimeProvider.currentDateTime() >> ZonedDateTime.now().plusYears(999999)
        and: "account is saved"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Jan", "Nowak", "56041500234", BigDecimal.TEN)
        )

        when: "account with the same PESEL number is saved"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Anna", "Kowalska", "56041500234", BigDecimal.ONE)
        )

        then: "exception is thrown"
        IllegalStateException exception = thrown()
        exception.message == "PESEL number already in use by other account"
    }

    def "creating account for under age person"() {
        given: "today is end of 2019"
        dateTimeProvider.currentDateTime() >> ZonedDateTime.of(LocalDate.of(2019, 12, 31), LocalTime.MAX, ZoneId.systemDefault())

        when: "account creation for person born in 2010 is requested"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Anna", "Kowalska", "10210156424", BigDecimal.ONE)
        )
        then: "exception is thrown"
        IllegalArgumentException exception = thrown()
        exception.message == "Cannot create currency account for user which is equal to or above allowed age"
    }

    def "test exchanging currencies"() {
        given: "today is future far, far away"
        dateTimeProvider.currentDateTime() >> ZonedDateTime.now().plusYears(999999)
        and: "account with 100 PLN balance is saved"
        currencyAccountService.createNewAccount(
                new CreateCurrencyAccountCommand("Anna", "Nowak", "56041500234", BigDecimal.valueOf(100))
        )

        when: "polish economy collapsed the dollar soared"
        nbpClient.getExchangeRate(CurrencyCode.USD) >> BigDecimal.valueOf(25)
        and: "half of PLN balance is exchanged for USD"
        currencyAccountService.exchange(new ExchangeCurrencyCommand(CurrencyCode.PLN, BigDecimal.valueOf(50), CurrencyCode.USD, "56041500234"))
        then: "balance on account is 2 USD and 50 PLN "
        currencyAccountService.getAccountInfoByPesel(new CurrencyAccountInfoByPeselQuery("56041500234")).with {
            currenciesBalances == Map.of(CurrencyCode.PLN, BigDecimal.valueOf(50), CurrencyCode.USD, BigDecimal.valueOf(2))
        }

        when: "polish economy conquered the world and USA economy, dollar has dropped"
        nbpClient.getExchangeRate(CurrencyCode.USD) >> BigDecimal.valueOf(0.1)
        and: "all USD balance is exchanged for PLN"
        currencyAccountService.exchange(new ExchangeCurrencyCommand(CurrencyCode.USD, BigDecimal.valueOf(2), CurrencyCode.PLN, "56041500234"))
        then: "balance on account is 2 USD and 50 PLN "
        currencyAccountService.getAccountInfoByPesel(new CurrencyAccountInfoByPeselQuery("56041500234")).with {
            currenciesBalances == Map.of(CurrencyCode.PLN, BigDecimal.valueOf(50.2), CurrencyCode.USD, BigDecimal.valueOf(0))
        }

        when: "nothing has changed since then"
        nbpClient.getExchangeRate(CurrencyCode.USD) >> BigDecimal.valueOf(0.1)
        and: "owner forgot she got rid of all dollars and tries to exchange 100 USD for PLN"
        currencyAccountService.exchange(new ExchangeCurrencyCommand(CurrencyCode.USD, BigDecimal.valueOf(100), CurrencyCode.PLN, "56041500234"))
        then: "exception is thrown"
        IllegalArgumentException exception = thrown()
        exception.message == "Cannot exchange more of the USD currency than is possessed"
    }
}