package io.github.glaforge.ansiren;

public class Ansi {
    private static final String ESCAPE = "\u001B[";
    private static final String RESET = ESCAPE + "0m";

    private final StringBuilder sb = new StringBuilder();

    public static Ansi ready() {
        return new Ansi();
    }

    public Ansi append(String text) {
        sb.append(text);
        return this;
    }

    // Styles
    public Ansi bold() { return code("1"); }
    public Ansi faint() { return code("2"); }
    public Ansi italic() { return code("3"); }
    public Ansi underline() { return code("4"); }
    public Ansi blink() { return code("5"); }
    public Ansi reverse() { return code("7"); }
    public Ansi conceal() { return code("8"); }

    // Foreground Colors
    public Ansi black() { return code("30"); }
    public Ansi red() { return code("31"); }
    public Ansi green() { return code("32"); }
    public Ansi yellow() { return code("33"); }
    public Ansi blue() { return code("34"); }
    public Ansi magenta() { return code("35"); }
    public Ansi cyan() { return code("36"); }
    public Ansi white() { return code("37"); }

    // Bright Foreground Colors
    public Ansi brightBlack() { return code("90"); }
    public Ansi brightRed() { return code("91"); }
    public Ansi brightGreen() { return code("92"); }
    public Ansi brightYellow() { return code("93"); }
    public Ansi brightBlue() { return code("94"); }
    public Ansi brightMagenta() { return code("95"); }
    public Ansi brightCyan() { return code("96"); }
    public Ansi brightWhite() { return code("97"); }

    // Background Colors
    public Ansi bgBlack() { return code("40"); }
    public Ansi bgRed() { return code("41"); }
    public Ansi bgGreen() { return code("42"); }
    public Ansi bgYellow() { return code("43"); }
    public Ansi bgBlue() { return code("44"); }
    public Ansi bgMagenta() { return code("45"); }
    public Ansi bgCyan() { return code("46"); }
    public Ansi bgWhite() { return code("47"); }

    public Ansi reset() {
        sb.append(RESET);
        return this;
    }

    private Ansi code(String code) {
        sb.append(ESCAPE).append(code).append("m");
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    // Static helpers
    public static String bold(String text) { return ready().bold().append(text).reset().toString(); }
    public static String red(String text) { return ready().red().append(text).reset().toString(); }
    public static String green(String text) { return ready().green().append(text).reset().toString(); }
    public static String yellow(String text) { return ready().yellow().append(text).reset().toString(); }
    public static String blue(String text) { return ready().blue().append(text).reset().toString(); }
    public static String magenta(String text) { return ready().magenta().append(text).reset().toString(); }
    public static String cyan(String text) { return ready().cyan().append(text).reset().toString(); }
    public static String white(String text) { return ready().white().append(text).reset().toString(); }
}
