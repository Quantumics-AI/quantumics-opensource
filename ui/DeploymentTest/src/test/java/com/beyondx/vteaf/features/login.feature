@DeploymentTest
Feature: Deployment Test

#  @SignUp @SignUpNI @Ignore
#  Scenario: I should sign up into the quantumics application
#    Given My Application 'signup' is open
#    Then I check the page title is 'Quantumics.AI'
##    Then I should see 'SignUp_Logo' present in page
##    And I should see 'SignUp_Heading' present in page
#    And I should see 'SignUp_FirstName' present in page
#    And I enter 'Test' in 'SignUp_FirstName' field
#    And I should see 'SignUp_LastName' present in page
#    And I enter 'Deployment' in 'SignUp_LastName' field
#    And I should see 'SignUp_Email' present in page
#    And I enter random email address in field 'SignUp_Email'
#    And I should see 'SignUp_Email' present in page
#    And I should see 'SignUp_PhoneNumber' present in page
#    And I enter '9999999991' in 'SignUp_PhoneNumber' field
#    And I should see 'SignUp_CompanyName' present in page
#    And I enter 'Automation Test' in 'SignUp_CompanyName' field
#    And I should see 'SignUp_Checkbox' present in page
#    And I click 'SignUp_Checkbox' by js
#    And I should see 'SignUp_Btn' present in page
#    And I click 'SignUp_Btn' by js
#    And I wait for '2' sec
##    And I should see 'SignUp_signupText' present in page
##    And I should see 'SignUp_loginBtn' present in page
##    And I click 'SignUp_loginBtn'
#    And I click 'SignUp_EmailLogin'
#    And I wait for '2' sec
#    Then I should see 'Login_Email' present in page
#    And I should see 'Login_Password' present in page
#    And I should see 'Login_SignInBtn' present in page
#    When I enter '$$randmail' in 'Login_Email' field
#    When I enter 'Welcome@123' in 'Login_Password' field
#    And I click 'Login_SignInBtn'
#    And I wait for '2' sec
##    And I should see 'SignUp_ChangePasswordTitle' present in page
##    And I should see 'SignUp_NewPasswordField' present in page
#    And I enter 'Test@123' in 'SignUp_NewPasswordField' field
#    And I should see 'SignUp_ConfirmPasswordField' present in page
#    And I enter 'Test@123' in 'SignUp_ConfirmPasswordField' field
#    And I should see 'SignUp_UpdatePassword' present in page
#    And I click 'SignUp_UpdatePassword'
#    And I wait for '2' sec
#    Then I should see 'Login_Email' present in page
#    And I should see 'Login_Password' present in page
#    And I should see 'Login_SignInBtn' present in page
#    When I enter '$$randmail' in 'Login_Email' field
#    When I enter 'Test@123' in 'Login_Password' field
#    And I click 'Login_SignInBtn'
#    And I wait for '3' sec
#    And I should see 'Dashboard_NewProject' present in page
#    And I wait for '1' sec
#    And I click 'Dashboard_NewProject'
#    And I wait for '2' sec
#    Then I should see 'Project_Heading' present in page
#    Then I should see 'Project_ProjectName' present in page
#    And I enter 'randAutomationProject' in 'Project_ProjectName' field
#    Then I should see 'Project_ProjectDesc' present in page
#    And I enter 'Demo Automation Project' in 'Project_ProjectDesc' field
#    Then I should see 'Project_CreateProjectBtn' present in page
#    And I click 'Project_CreateProjectBtn'
#    And I wait for '3' sec
#    And I should see 'ProjectModules' present in page
#    And I should see 'ProjectContent_Close' present in page
#    And I click 'ProjectContent_Close'
#    And I wait for '1' sec
#    Then I should see 'ProjectSelect' present in page
#    When I click 'ProjectSelect'


  @LoginTest @Test123 @DEVTEST
  Scenario Outline: I validate the elements in the Login Screen
    Given My Application 'login' is open
    Then I check the page title is 'Quantumics.AI'
    Then I should see 'Login_Email' present in page
    And I should see 'Login_Password' present in page
    And I should see 'Login_SignInBtn' present in page
    When I enter '<Username>' in 'Login_Email' field
    When I enter '<Password>' in 'Login_Password' field
    And I click 'Login_SignInBtn'
    And I wait for '5' sec
    Then I should see 'ProjectSelect' present in page
    When I click 'ProjectSelect'

  Examples:
    |Username|Password|
    |integrationtesting@quantumics.ai|33G+2SnQ|



  @testtttt
  Scenario Outline: I should be able to inject a folder
    And I should see 'Inject_icon' present in page
    And I click 'Inject_icon'
    And I wait for '2' sec
#    And I should see 'Injest_addFolder' present in page
#    And I wait for '2' sec
#    And I click 'Injest_addFolder'
    And I should see 'Inject_ConnectToSource' present in page
    And I click 'Inject_ConnectToSource'
    And I wait for '2' sec
    And I should see 'Injest_LocalFile' present in page
    And I click 'Injest_LocalFile'
    And I wait for '2' sec
    And I should see 'Inject_folderName' present in page
    And I should see 'Inject_folderDesc' present in page
    And I should see 'Inject_CreateFolder' present in page
#    And I enter 'Likes' in 'Inject_folderName' field
    And I enter 'rand<foldername>' in 'Inject_folderName' field and store at '<foldername>'
    And I enter 'Test Folder Desc' in 'Inject_folderDesc' field
    And I wait for '2' sec
    And I click 'Inject_CreateFolder'
    And I wait for '2' sec
    And I upload the file '<filename>'
    And I wait for '5' sec
    And I should see 'Injest_continue' present in page
    And I click 'Injest_continue'
    And I wait for '5' sec
    And I click 'Injest_continue1' by js
    And I wait for '5' sec
    And I click 'Injest_completed' by js
    And I wait for '10' sec

    Examples:
    |foldername|filename|
    |likes|Like|
    |users|Users|


  @testtttt
  Scenario Outline: I perform convert to uppercase cleansing operation
    Then I should see 'CleanseModule' present in page
    And I wait for '2' sec
    When I click 'CleanseModule'
    And I wait for '5' sec
#    And I should see 'Cleansed_AddBtn' present in page
#    And I click 'Cleansed_AddBtn'
    Then I should see 'RawData' present in page
    When I click 'RawData'
    And I wait for '2' sec
    Then I should see 'SelectFolder' present in page
    And I enter '$$<FolderName>' in 'SelectFolder' field
    And I wait for '2' sec
    Then I press key: enter
    And I wait for '2' sec
    When I click 'selectFile'
    And I wait for '2' sec
    And I click 'CleanseData'
    And I wait for '2' sec
    Then I should delete the existing rule
    And I wait for '3' sec
    And I take value count before uppercase at '<ColumnName>' and store
    And I wait for '1' sec
    And I click 'FormatValue'
    And I wait for '1' sec
    And I click 'UppercaseRule'
    And I wait for '1' sec
    And I enter '<ColumnName>' in 'selectColumn' field
    And I wait for '1' sec
    And I press key: enter
    And I wait for '1' sec
    And I click 'AddButton'
    And I wait for '1' sec
    And I take value count after uppercase and store
    And I wait for '1' sec
    And I compare the two given columns
    And I wait for '2' sec
    And I click 'AutomateBar'
    And I wait for '2' sec
    And I select '$$<FolderName>' folder at Automate
    And I click 'selectFileToRun'
    And I wait for '2' sec
    And I click 'RunJob' by js
    And I wait for '2' sec
    And I click '$$<FolderName>' history
    And I refresh the page till status message
    And I click 'DownloadBtn'
    And I wait for '5' sec
    And I verify the downloaded cleansed file is present
    And I click 'HistoryCloseBtn' by js
    And I wait for '2' sec

    Examples:
    |FolderName|ColumnName|
    |likes|Like|

  @Test123 @testtttt
    Scenario Outline: I validate engineering flow with full outer join raw with cleansed
      When I click 'EngineeringModule'
      Then I should see 'CreateFlow' present in page
      When I click 'CreateFlow'
#      And I enter 'rand<FlowName>' in 'FlowName' field
      And I enter 'rand<FlowName>' in 'FlowName' field and store at '<FlowName>'
      And I click 'SaveButton'
      And I wait for '2' sec
      And I click edit on '$$<FlowName>' folder
      And I wait for '2' sec
      And I click 'Raw' by js
      And I wait for '2' sec
      And I select '$$<folder>' folder at Engineering
      And I select '<File>' file at Engineering
      And I wait for '2' sec
      And I click 'Cleansed' by js
      And I wait for '2' sec
      And I select '$$<FolderName>' folder at Engineering
      And I select '<FileName>' file at Engineering
      And I wait for '2' sec
#      And I click 'JoinFunction' by actions
      And I click 'JoinFunction' by js
      And I wait for '5' sec
      And I connect file 1 with join
      And I wait for '2' sec
      And I connect file 2 with join
      And I wait for '2' sec
      And I enter '<Type>' in 'selectJoinType' field
      And I wait for '1' sec
      And I enter '<FirstFileColumn>' in 'selectFirstFileColumn' field
      And I enter '<SecFileColumn>' in 'selectSecondFileColumn' field
      And I wait for '1' sec
      And I click 'clickApply'
      And I wait for '1' sec
      And I click 'clickPreview'
      And I wait for '12' sec
      And I click 'clickSave'
      And I wait for '5' sec
      And I fetch values from expected '<ExpectedValueForJoin>' and store
      And I click 'AutomateBar'
      And I wait for '2' sec
      And I click 'Engineering_Jobs'
      And I wait for '2' sec
      And I enter '$$<FlowName>' in 'searchTerm' field
      And I wait for '2' sec
      And I select folder '$$<FlowName>' at engineering
      And I wait for '2' sec
      And I click 'FirstFile'
      And I click 'SecondFile'
      And I wait for '2' sec
      And I click 'RunJobAtEngineeringFiles'
      And I wait for '2' sec
      And I select history '$$<FlowName>' at engineering
      And I refresh the page till status message
      And I click 'DownloadEngineering'
      And I wait for '5' sec
      And I verify the downloaded engineering file is present
      And I click 'HistoryCloseBtn' by js
      And I wait for '2' sec
      Then I should see 'DashboardModule' present in page

      Examples:
    |FlowName             |folder       |File    |FolderName   |FileName|Type |FirstFileColumn|SecFileColumn|ExpectedValueForJoin  |
    |AutomationEngineering|users|Users.csv|likes    |Like.csv|Full Outer Join|ID |     UserID        |FullouterJoin-Output-RawWithCleansed|

      @Test123 @Dash @testtttt
      Scenario: I create a new dashboard with the given enginnering file
        When I click 'DashboardModule'
        And I wait for '10' sec
        And I switch to iframe 'redash'
        And I wait for '2' sec
        Then I should see 'Dashboard_CreateButton' present in page
        And I wait for '1' sec
        When I click 'Dashboard_CreateButton'
        And I wait for '1' sec
        Then I should see 'Dashboard_NewQuery' present in page
        Then I should see 'Dashboard_NewDashboard' present in page
        When I click 'Dashboard_NewQuery'
        And I wait for '5' sec
#        And I click 'Dashboard_Name' by js
#        And I enter 'AutomationEngineering32001' in 'Dashboard_NameInput' by js
#        And I press key: enter
        And I should see 'Dashboard_Engineered' present in page
        When I click 'Dashboard_Engineered'
        And I wait for '2' sec
        And I select the query from '$$AutomationEngineering'
#        And I select the query from 'automationengineering1647'
        And I wait for '2' sec
        Then I should see 'Dashboard_CloseBtn' present in page
        And I should see 'Dashboard_FormatBtn' present in page
        And I should see 'Dashboard_Autocomplete' present in page
        And I should see 'Dashboard_Save' present in page
        And I should see 'Dashboard_Execute' present in page
        When I click 'Dashboard_Execute' by js
        And I wait for '1' sec
        Then I should see 'Dashboard_Table' present in page
        And I should see 'Dashboard_Add_Visualizations' present in page
        When I click 'Dashboard_Add_Visualizations' by js
        And I wait for '1' sec
        And I should see 'Dashboard_editorControl' present in page
        And I should see 'Dashboard_ChartType' present in page
        And I should see 'Dashboard_XColumn' present in page
        And I click 'Dashboard_XColumn' by js
        And I wait for '1' sec
        And I click 'Dashboard_XColumn_select' by js
        And I wait for '1' sec
        And I scroll till element 'Dashboard_YColumn'
        And I should see 'Dashboard_YColumn' present in page
        And I click 'Dashboard_YColumn' by js
        And I wait for '1' sec
        And I should see 'Dashboard_YColumn_select' present in page
        And I click 'Dashboard_YColumn_select' by js
        And I should see 'Dashboard_GroupBy' present in page
        And I click 'Dashboard_GroupBy' by js
        And I wait for '1' sec
        And I should see 'Dashboard_GroupBy_select' present in page
        And I click 'Dashboard_GroupBy_select' by js
        And I wait for '5' sec
        And I scroll till element 'Dashboard_Save_1'
        And I wait for '2' sec
        And I click 'Dashboard_Save_1' by js
#        And I click 'Dashboard_ChartCloseBtn' by js
        And I wait for '3' sec
#        And I click 'Dashboard_ChartCloseBtn_Yes' by js
        And I should see 'Dashboard_Publish' present in page
        And I should see 'Dashboard_showDataOnly' present in page
        And I should see 'Dashboard_DropDown' present in page
        When I click 'Dashboard_DropDown' by js
        Then I should see 'Dashboard_Fork' present in page
        And I should see 'Dashboard_Archive' present in page
        And I should see 'Dashboard_API_Key' present in page
        When I click 'Dashboard_Publish' by js
        And I wait for '2' sec
        Then I click 'Dashboard_CreateButton'
        And I wait for '1' sec
        Then I should see 'Dashboard_NewQuery' present in page
        Then I should see 'Dashboard_NewDashboard' present in page
        And I wait for '1' sec
        Then I click 'Dashboard_NewDashboard'
        And I wait for '1' sec
        And I enter 'randAutomationEngineering' in 'Dashboard_NewName' field
        And I wait for '2' sec
        And I click 'DashboardSaveButton'
        And I wait for '3' sec
        And I should see 'DashboardHeader' present in page
        And I should see 'DashboardSaveStatus' present in page
        And I should see 'Dashboard_AddTextBoxButton' present in page
        And I should see 'Dashboard_AddWidgetButton' present in page
        And I click 'Dashboard_AddWidgetButton' by js
        And I wait for '2' sec
#        And I should see 'Dashboard_SelectOption' present in page
        And I fetch and select the last query
        And I wait for '2' sec
        And I should see 'Dashboard_ChooseVisualization' present in page
        And I click 'Dashboard_ChooseVisualization' by js
        And I wait for '2' sec
        And I should see 'Dashboard_ChooseVisualizationTable' present in page
        And I click 'Dashboard_ChooseVisualizationTable' by js
        And I should see 'Dashboard_ChooseVisualization' present in page
        And I should see 'Dashboard_CancelBtn' present in page
        And I should see 'Dashboard_AddToDashboardBtn' present in page
        And I click 'Dashboard_AddToDashboardBtn' by js
        And I wait for '4' sec
        And I click 'Dashboard_AddWidgetButton' by js
        And I wait for '2' sec
        And I fetch and select the last query
        And I wait for '2' sec
        And I should see 'Dashboard_ChooseVisualization' present in page
        And I click 'Dashboard_ChooseVisualization' by js
        And I wait for '2' sec
        And I should see 'Dashboard_ChooseVisualizationChart' present in page
        And I click 'Dashboard_ChooseVisualizationChart' by js
        And I wait for '2' sec
        And I should see 'Dashboard_CancelBtn' present in page
        And I should see 'Dashboard_AddToDashboardBtn' present in page
        And I click 'Dashboard_AddToDashboardBtn' by js
        And I wait for '3' sec
        And I click 'Dashboard_DoneEditing'
        And I wait for '1' sec
        And I should see 'Dashboard_Publish' present in page
        And I should see 'Dashboard_Refresh' present in page
        And I click 'Dashboard_Publish' by js
        And I wait for '1' sec
        And I click 'Dashboard_ShareForm' by js
        And I wait for '1' sec
        And I click 'Dashboard_AllowPublicAccess' by js
        And I wait for '2' sec
        And I get URL from dashboard and navigate to the URL
        And I wait for '1' sec
#        And I should see 'Dashboard_RedashTable' present in page
#        And I should see 'Dashboard_RedashChart' present in page
        And I wait for '2' sec
        And I navigate back
        And I wait for '1' sec

        @DEVTEST
        Scenario: I Logout of the application
        And I click 'Logout_Img'
        And I should see 'Logout_Btn' present in page
        And I click 'Logout_Btn' by js
