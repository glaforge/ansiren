package io.github.glaforge.ansiren;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnsiTest {

    @Test
    public void testRed() {
        String result = Ansi.red("Hello");
        assertEquals("\u001B[31mHello\u001B[0m", result);
    }

    @Test
    public void testBold() {
        String result = Ansi.bold("Hello");
        assertEquals("\u001B[1mHello\u001B[0m", result);
    }

    @Test
    public void testFluent() {
        String result = Ansi.ready().bold().red().append("Hello").reset().toString();
        assertEquals("\u001B[1m\u001B[31mHello\u001B[0m", result);
    }
}
