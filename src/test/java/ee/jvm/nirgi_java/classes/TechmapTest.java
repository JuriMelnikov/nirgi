package ee.jvm.nirgi_java.classes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TechmapTest {

    private Techmap withPrice(String price) {
        Techmap techmap = new Techmap();
        techmap.setPrice(price);
        return techmap;
    }

    @Test
    void formatsThousandthsOfEuroAsEuros() {
        assertEquals("1.500 €", withPrice("1500").getPriceInEuros());
        assertEquals("0.050 €", withPrice("50").getPriceInEuros());
        assertEquals("12.000 €", withPrice("12000").getPriceInEuros());
    }

    @Test
    void returnsEmptyStringForMissingPrice() {
        assertEquals("", withPrice(null).getPriceInEuros());
        assertEquals("", withPrice("  ").getPriceInEuros());
    }

    @Test
    void returnsRawValueWhenNotANumber() {
        assertEquals("abc", withPrice("abc").getPriceInEuros());
    }
}
