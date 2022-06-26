package pl.tseroka.currency_exchange_demo.domain

import pl.foltak.polishidnumbers.pesel.InvalidPeselException
import spock.lang.Specification

class PeselTests extends Specification {

    def "creating Pesel class instance with valid pesel number should thrown no exception"() {
        when:
        def pesel = new PeselWrapper(peselValue)
        then:
        noExceptionThrown()
        pesel.value == peselValue

        where:
        peselValue    | _
        "56041500234" | _
        "09220946873" | _
        "04270342310" | _
        "44031921501" | _
        "23090980562" | _
        "73041810358" | _
        "14261743514" | _
        "81102156282" | _
        "53112535748" | _
        "83030431448" | _
        "08302443747" | _
    }

    def "creating Pesel class instance with invalid pesel number should thrown no exception"() {
        when:
        new PeselWrapper(peselValue)
        then:
        thrown(InvalidPeselException)

        where:
        peselValue     | _
        "012910672042" | _
        "15221768143D" | _
        "132908674196" | _
        "1324264882Z"  | _
        "7705040665F"  | _
        "9102225532X"  | _
        "51092177319"  | _
        "67102235447"  | _
        "18302741579"  | _
        "222616510632" | _
        ""             | _
        "    "         | _
        "   e   "      | _
    }
}