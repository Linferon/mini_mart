package util;

public class ConsoleUtil {
    private ConsoleUtil() {}

    public static void println(Object obj) {
        System.out.println(obj);
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    public static void printDivider() {
        System.out.println("--------------------------------");
    }

    public static void printHeader(String title) {
        printDivider();
        println(title);
        printDivider();
    }

    public static void printError(String s) {
        System.out.println(s);
    }
}