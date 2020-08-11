Scenario: Move from Data Capture to Archive

Given the nav list is ready
And the Data Capture queue is not active
Then wait for the page to be ready

When activating the Documents tab
Then expand the Abstract section
And upload the following files
|file|
|path....|
Then click on the Upload Files button

When activating the Shipping & Billing tab
Then expand the Requester Specifics section
And click on the Search Existing Requesters button
And wait for the search dialog
And search for TEST-3

Given the search returned results
Then select row 1
And click on the Add button

When expanding the Shipping Specifics section
Then set the Delivery Method field to Electronic
And set the eDelivery Method field to Secure Email
And set the Email Address field to myrtle.engram@armedia.com

Then activate the Details tab
And expand the Authorization Specifics section
And set the Request Type field to Authorization
And set the Request Subtype field to Continuing Care
And click on the Bill Request button

# TODO: How to confirm it made it into the archive?
