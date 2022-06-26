package pl.tseroka.currency_exchange_demo.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pl.tseroka.currency_exchange_demo.service.CurrencyAccountService
import pl.tseroka.currency_exchange_demo.service.dto.CurrencyAccountInfoDto
import pl.tseroka.currency_exchange_demo.service.command.CreateCurrencyAccountCommand
import pl.tseroka.currency_exchange_demo.service.command.ExchangeCurrencyCommand
import pl.tseroka.currency_exchange_demo.service.query.CurrencyAccountInfoByPeselQuery

@RestController
@RequestMapping("/api/currency-accounts")
class CurrencyAccountController(private val currencyAccountService: CurrencyAccountService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("new")
    fun createNewAccount(@RequestBody command: CreateCurrencyAccountCommand) {
        currencyAccountService.createNewAccount(command)
    }

    /**
    Post method is used to query because PESEL number is sensitive data and cannot be passed as query parameter
     */
    @PostMapping("get")
    fun getAccountInfoByPesel(@RequestBody query: CurrencyAccountInfoByPeselQuery): CurrencyAccountInfoDto {
        return currencyAccountService.getAccountInfoByPesel(query)
    }

    @PostMapping("exchange")
    fun exchange(@RequestBody command: ExchangeCurrencyCommand) {
        currencyAccountService.exchange(command)
    }
}