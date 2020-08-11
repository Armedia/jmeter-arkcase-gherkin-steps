Feature: Move from Origination to Data Capture

Scenario: Select the Origination Queue

Given the nav list is ready
Then select the Queues area
And wait for the page to be ready

Given the queue list is ready
Then select the Origination queue
And wait for the queue to be ready
And sort by Request in descending order
And wait for the page to be ready

Then click on the Start Working button, switch to the new window
And wait for the page to be ready
And wait for PDFTron

Given the page is ready
When activating the Documents tab
Then expand the Authorization section
And upload the following files
|file|
|path....|
And click on the Upload files button

When activating the Create tab
Then expand the Patient Specifics section
And populate the section with:
|name|value|
|Address|2000 Riveredge Parkway|
|City|Atlanta|
|State|Georgia|
|ZIP|30328|
And wait until the dialog is shown
And click on the Accept button
And wait for the page to be ready
And fill in the section with:
|name|value|
|Phone Number|111-111-1111|
|Alt Phone Number|111-111-1111|
|Email Address|first.last@email.com|
|Certification Type|Driver's License|
|Certification Number|GA123456|
|Expiration Date|07312090|

Then expand the Request Specifics section
And set the Bill to Client field to selected
And set the Record Type field to Medical

Then add a note with the content [Volume Building]

Then expand the Request Activity section
And fill in the section with:
|name|value|
|Pending State|Approved by Legal|
And click on the Data Capture Request button