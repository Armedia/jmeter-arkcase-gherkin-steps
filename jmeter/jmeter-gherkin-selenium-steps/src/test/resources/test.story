Feature: Move from Transcribe to Fulfill

Scenario: Move from Transcribe to Fulfill

Given the nav list is ready
Then select the Queues area

Given the queue list is ready
Then select the Fulfill queue
And wait for the queue to be ready
And sort by Request in descending order
And wait for the page to be ready

Given the Fulfill queue is selected
Then click on the Start Working button, switch to the new window
And wait for the page to be ready
And wait for PDFTron

Given the page is ready
When activating the Shipping & Billing tab
Then expand the Requester Specifics section
And click on the Search Existing Requesters button

When the search dialog is active
Then search for TEST-3

Given the search returned results
Then select row 1
And click on the Add button

Then activate the Details section
And expand the Authorization Specifics section
And set the Request Type field to Authorization
And set the Request Subtype field to Continuing Care

Then sleep for 2 seconds
And click on the Complete Task button, wait for the window to close
And switch to the main page