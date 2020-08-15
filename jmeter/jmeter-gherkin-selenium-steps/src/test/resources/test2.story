Feature: Move from Origination to Data Capture

Scenario: Fill in the data

Given the Origination queue is active
Then click on the Start Working button, switch to the new window
And wait for the page to be ready
And wait for PDFTron

Given the page is ready
When activating the Create tab
Then expand the Patient Specifics section
And populate the section with:
|name|value|
|Address|2000 Riveredge Parkway|
|City|Atlanta|
|State|Georgia|
|ZIP|30328|
And wait until the [Address Info - Patient Specifics] dialog is shown
And click on the Accept button, wait for the dialog to close
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

Then add a note with [Volume Building]

Then expand the Request Activity section
And set the Pending State field to Approved by Legal

Scenario: Data is still pending

Given data is missing
Then fail with the message [More data needs to be entered, skipping this request]

Scenario: Advance to Data Capture

Given the data is complete
Then click on the Data Capture Request button
And wait for the page to be ready
