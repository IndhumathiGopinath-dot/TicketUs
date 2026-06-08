Feature: Ticket creation workflows
  As an employee
  I want to raise support tickets in different categories
  So that the appropriate admin team picks them up

  @regression @bdd @tickets
  Scenario: BDD_02 — Employee creates an IT ticket
    Given the user is on the login page
    When the user logs in as employee "any"
    And the user creates an "IT" ticket with title "Internet connectivity issue"
    Then the ticket creation should redirect to detail or dashboard
