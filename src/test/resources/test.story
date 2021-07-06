Feature: Select the Origination Queue

Scenario: Select the Origination Queue

Given the nav list is ready
Then select the Queues area
And wait for the page to be ready

Given the queue list is ready
Then select the Origination queue
And wait for the queue to be ready
And sort by Request in descending order
And wait for the page to be ready
