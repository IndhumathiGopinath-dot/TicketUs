Feature: Ticket creation with auto-priority routing
  As an employee
  I want the system to detect urgency from the words I use
  So that critical issues get attention quickly

  Background:
    Given I am logged in as an employee

  Scenario: Outage keyword auto-routes to URGENT priority
    When I create an "IT" ticket titled "Email server outage" with description "Mail completely down"
    Then the ticket should be created with status "OPEN"
    And the ticket priority should be "URGENT"

  Scenario: Password reset routes to LOW priority
    When I create an "IT" ticket titled "password reset request" with description "Forgot my login"
    Then the ticket should be created with status "OPEN"
    And the ticket priority should be "LOW"

  Scenario: HR confidential ticket is flagged
    When I create a confidential "HR" ticket titled "Payroll error" with description "Wrong amount" of request type "Payroll query"
    Then the ticket should be created with status "OPEN"
    And the ticket priority should be "URGENT"

  Scenario Outline: Priority routing across categories
    When I create an "<category>" ticket titled "<title>" with description "<desc>"
    Then the ticket priority should be "<priority>"

    Examples:
      | category | title                       | desc                            | priority |
      | IT       | server down emergency       | servers stopped responding      | URGENT   |
      | IT       | Documentation update        | Please review when convenient   | NORMAL   |