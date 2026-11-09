package net.noblebrains.outbound;

import net.noblebrains.outbound.motd.MotdHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MotdHelperTest {

    @Test
    @DisplayName("Verify formatting '&' color codes to '§'")
    void testColorCodeFormatting() {
        String input = "&aWillkommen &6auf &bOutbound!";
        String expected = "\u00a7aWillkommen \u00a76auf \u00a7bOutbound!";
        assertEquals(expected, MotdHelper.formatMotd(input));
    }

    @Test
    @DisplayName("Verify formatting literal '\\n' to actual newline")
    void testNewlineFormatting() {
        String input = "&aZeile 1\\n&eZeile 2";
        String expected = "\u00a7aZeile 1\n\u00a7eZeile 2";
        assertEquals(expected, MotdHelper.formatMotd(input));
    }

    @Test
    @DisplayName("Verify handling of null and empty input")
    void testNullAndEmpty() {
        assertEquals("", MotdHelper.formatMotd(null));
        assertEquals("", MotdHelper.formatMotd(""));
    }

    @Test
    @DisplayName("Verify '&' without valid color code remains untouched")
    void testNonColorAmpersands() {
        String input = "Brot & Butter &z Test";
        assertEquals("Brot & Butter &z Test", MotdHelper.formatMotd(input));
    }
}
