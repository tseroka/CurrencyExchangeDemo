package pl.tseroka.currency_exchange_demo

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class CurrencyExchangeDemoApplicationTests extends Specification {

	def contextLoads() {
		expect:
		1 == 1
	}
}
