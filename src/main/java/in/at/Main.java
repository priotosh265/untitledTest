package in.at;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("https://www.google.com/");
            System.out.println(page.title());
        }
    }
}
