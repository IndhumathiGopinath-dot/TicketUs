Feature: User authentication
  As a user of the ticket system
  I want to log in and out securely
  So that I can access my tickets

  Background:
    Given the application is running
    And I am on the login page

  Scenario: Successful employee login
    When I login as "john@company.com" with password "password123"
    Then I should land on the employee dashboard
    And I should see at least 1 stat card

  Scenario: Successful admin login
    When I login as "it.admin@company.com" with password "admin123"
    Then I should land on the admin console

  Scenario: Wrong password is rejected
    When I login as "john@company.com" with password "wrongpass"
    Then I should remain on the login page

  Scenario Outline: Multiple invalid credentials
    When I login as "<email>" with password "<password>"
    Then I should remain on the login page

    Examples:
      | email                | password      |
      | nobody@nowhere.com   | anything      |
      | not-an-email         | password123   |
