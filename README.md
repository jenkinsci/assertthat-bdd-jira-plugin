# assertthat-bdd-jira

Jenkins plugin for interaction with [AssertThat BDD Jira plugin](https://marketplace.atlassian.com/apps/1219033/assertthat-bdd-test-management-in-jira?hosting=cloud&tab=overview).

Main features are:

- Download feature files before test run
- Filter features to download based on mode (automated/manual/both), or/and JQL
- Upload cucumber json after the run to AsserTthat BDD Jira plugin

## Usage in Job 

Downloading feature files: 

Uploading reports:

## Usage in pipeline Job

```
pipeline {
    agent any 
    stages {
        stage('Features') { 
            steps {
                //Download feature files
                assertthatBddFeatures(credentialsId: '10001-creds', jql: 'project=DEMO', mode: 'automated', outputFolder: 'features', projectId: '10005')
            }
        }
        stage('Run tests') { 
            steps {
                //Run tests here
            }
        }

    }
    post{
        always{
                //Upload test results
                assertthatBddReport(credentialsId: '10005-creds', jsonReportFolder: 'report', jsonReportIncludePattern: '**/*.json', projectId: '10005', runName: 'Smoke test run') 
        }
    }

```
