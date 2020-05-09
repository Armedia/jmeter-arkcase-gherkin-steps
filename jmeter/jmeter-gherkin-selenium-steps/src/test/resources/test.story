Feature: Select the Transcribe Queue

Scenario: Select the Transcribe Queue

Given the nav list is ready
Then select the Queues tab

Given the queue list is ready
Then select the Transcribe queue
And wait for the page to be ready
And sort by Request in descending order
And wait for the page to be ready