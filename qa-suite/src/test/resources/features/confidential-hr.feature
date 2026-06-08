Feature: Confidential HR ticket workflow
  As an employee
  I want to raise confidential HR tickets
  So that sensitive matters are handled privately by HR

  @regression @bdd @security @hr
  Scenario: BDD_03 — Employee raises a confidential HR ticket
    Given the user is on the login page
    When the user logs in as employee "any"
    And the user creates a confidential HR ticket with title "Workplace harassment concern"
    Then the ticket creation should redirect to detail or dashboard
