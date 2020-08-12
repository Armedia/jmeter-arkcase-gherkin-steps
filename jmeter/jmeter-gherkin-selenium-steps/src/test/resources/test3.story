Composite: Then xadd a note with the content [$content]
Alias: When xadding a note with the content [$content]
Given the center Notes tab is visible
Then activate the center Notes tab
And click on the Add Note button
And set the [NOTE] field to [$content]
And click on the Save button
And wait for the dialog to be shown
And click on the Add Note Without Saving button, wait for the dialog to close