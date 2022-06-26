package pl.tseroka.currency_exchange_demo.infrastructure.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import pl.tseroka.currency_exchange_demo.domain.CurrencyCode
import pl.tseroka.currency_exchange_demo.service.NbpClient
import java.lang.RuntimeException
import java.math.BigDecimal

@Component
class HttpRestNbpClient : NbpClient {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HttpRestNbpClient::class.java)

        private const val NBP_EXCHANGE_RATE_URL = "https://api.nbp.pl/api/exchangerates/rates/A/%s/"
        private const val MAX_NBP_CONNECTION_ATTEMPTS = 5
    }

    private val restTemplate: RestTemplate = RestTemplate()

    override fun getExchangeRate(target: CurrencyCode): BigDecimal {
        var currentAttemptsCount = 0
        while (currentAttemptsCount <= MAX_NBP_CONNECTION_ATTEMPTS) {
            try {
                return getExchangeRateFromNbp(target)
            } catch (ex: Exception) {
                log.error("Exception during NBP connection to fetch exchange rate for PLN/$target", ex)
                currentAttemptsCount += 1
            }
        }
        throw RuntimeException("")
    }

    private fun getExchangeRateFromNbp(target: CurrencyCode): BigDecimal {
        val responseBody: Rates = restTemplate.getForEntity(
            String.format(NBP_EXCHANGE_RATE_URL, target.name), Rates::class.java
        ).body ?: throw IllegalStateException("Could not get response from NBP API")
        return responseBody.getLatestRate()
            ?: throw NoSuchElementException("Could not fetch latest exchange rate for PLN/$target")
    }

    private data class Rates(val rates: List<Rate>) {
        fun getLatestRate(): BigDecimal? = rates.firstOrNull()?.mid
        private data class Rate(val mid: BigDecimal)
    }
}
