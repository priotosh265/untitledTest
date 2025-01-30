Feature: Myntra T-Shirt Scraper

  Scenario: Scrape discounted Van Heusen T-Shirts
    Given I open Myntra website
    When I navigate to Men's T-Shirts category
    And I apply the brand filter "Van Heusen"
    Then I extract and print the discounted T-Shirts sorted by highest discount

