package stepDefinitions;

import com.microsoft.playwright.*;
import io.cucumber.java.en.*;
import org.junit.Assert;

import java.util.*;

public class ScraperSteps {
    Playwright playwright;
    Browser browser;
    Page page;

    List<TShirt> tshirtData = new ArrayList<>();

    @Given("I open Myntra website")
    public void openMyntraWebsite() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        page.navigate("https://www.myntra.com");


        // Assertion: Check if Myntra homepage loaded successfully
        Assert.assertTrue("Myntra homepage not loaded!", page.title().contains("Myntra"));

    }

    @When("I navigate to Men's T-Shirts category")
    public void navigateToTShirts() {
//        page.waitForSelector("[data-reactid='20']");
        page.locator("[data-reactid='20']").hover();
        page.locator("text=T-Shirts").nth(0).click();

        // Assertion: Verify page navigation to T-Shirts category
        Assert.assertTrue("Failed to navigate to T-Shirts category!",
                page.url().contains("men-tshirts"));
    }

    @And("I apply the brand filter {string}")
    public void applyBrandFilter(String brand) {
//        page.locator("input[value='" + brand + "']").click();
        page.locator(".filter-search-filterSearchBox").nth(0).click();
        page.locator(".filter-search-inputBox").nth(0).fill(brand);
        page.keyboard().press("Enter");

        // Check if the brand filter exists in the filter list
        int filterCount = page.locator(".vertical-filters-label >> text=" + brand).count();

        if (filterCount == 0) {
            System.out.println("Brand filter '" + brand + "' not found. Skipping further steps.");
            Assert.fail("Brand not found: " + brand);
            browser.close();  // Stop execution if the brand filter is not available
            return;
        }

        // Apply the brand filter
        page.locator(".vertical-filters-label >> text=" + brand).nth(0).click();

//        page.locator(".vertical-filters-label >> text="+brand).nth(0).click();
//        page.waitForTimeout(3000); // Wait for filtering to apply
    }

    @Then("I extract and print the discounted T-Shirts sorted by highest discount")
    public void extractDiscountedTShirts() {
        List<TShirt> tshirtData = new ArrayList<>();

        for (int pageNumber = 1; pageNumber <= 13; pageNumber++) {
            List<ElementHandle> items = page.querySelectorAll(".product-base");

            for (ElementHandle item : items) {
                try {
                    String price = item.querySelector(".product-price").innerText();

                    // Extract discount text safely
                    String discountText = item.querySelector(".product-discountPercentage") != null
                            ? item.querySelector(".product-discountPercentage").innerText()
                            : "0%";

                    // Remove non-numeric characters (like brackets and "OFF")
                    String discount = discountText.replaceAll("[^0-9]", "");

                    String link = "https://www.myntra.com/" + item.querySelector("a").getAttribute("href");

                    tshirtData.add(new TShirt(price, discount, link));
                }catch (Exception e) {
                    System.out.println("Skipping item due to missing data.");
                }
            }

            // Navigate to the next page (if available)
            if (pageNumber < 13) {
                try {
                    page.locator(".pagination-next").click();  // Click 'Next' button
                    page.waitForLoadState();  // Ensure page loads completely before scraping next page
                } catch (Exception e) {
                    System.out.println("Failed to navigate to the next page. Stopping at page: " + pageNumber);
                    break;
                }
            }
        }
        Assert.assertFalse("No T-Shirts found after applying filter!", tshirtData.isEmpty());


        // Sort by highest discount first
        tshirtData.sort((a, b) -> Integer.compare(
                Integer.parseInt(b.discount),
                Integer.parseInt(a.discount)
        ));

        // Assertion: Ensure sorting worked correctly
        if (tshirtData.size() > 1) {
            Assert.assertTrue("Sorting failed!",
                    Integer.parseInt(tshirtData.get(0).discount) >= Integer.parseInt(tshirtData.get(1).discount));
        }

        // Print sorted data
        tshirtData.forEach(System.out::println);

        browser.close();
    }
}

    // Helper Class for T-Shirt Data
class TShirt {
    String price;
    String discount;
    String link;

    public TShirt(String price, String discount, String link) {
        this.price = price;
        this.discount = discount;
        this.link = link;
    }

    @Override
    public String toString() {
        return "Price: " + price + ", Discount: " + discount + ", Link: " + link;
    }
}
