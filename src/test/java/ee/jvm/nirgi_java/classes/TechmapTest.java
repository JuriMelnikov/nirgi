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
    void formatsCentsAsEuros() {
        assertEquals("1.500 €", withPrice("150").getPriceInEuros());
        assertEquals("0.050 €", withPrice("5").getPriceInEuros());
        assertEquals("12.000 €", withPrice("1200").getPriceInEuros());
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
