Feature: Self-service via knowledge base
  As an employee
  I want to browse and search the knowledge base
  So that I can resolve common issues without raising a ticket

  @regression @bdd @knowledge
  Scenario: BDD_04 — Employee searches the knowledge base
    Given the user is on the login page
    When the user logs in as employee "any"
    And the user opens the knowledge base
    And the user searches knowledge base for "password"
    Then the knowledge base page should remain visible
