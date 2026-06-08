Feature: Employee login
  As an existing employee
  I want to log in to the application
  So that I can manage my support tickets

  @smoke @bdd @auth
  Scenario: BDD_01 — Employee logs in successfully and lands on employee dashboard
    Given the user is on the login page
    When the user logs in as employee "any"
    Then the employee dashboard should be visible
