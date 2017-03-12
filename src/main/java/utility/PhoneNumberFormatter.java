package main.java.utility;

import javafx.scene.control.TextFormatter;

public class PhoneNumberFormatter {

	public PhoneNumberFormatter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Automatic format text field with phone number formatter
	 * 
	 * Credit: http://stackoverflow.com/questions/42149834/javafx-updating-a-phone-number-textfield-with-hashtags-and-numbers-live
	 * 
	 * @param change
	 * @return
	 */
	public static TextFormatter.Change addPhoneNumberMask(TextFormatter.Change change) {

		// Ignore cursor movements, unless the text is empty (in which case
		// we're initializing the field).
		if (!change.isContentChange() && !change.getControlNewText().isEmpty()) {
			return change;
		}

		String text = change.getControlNewText();
		int start = change.getRangeStart();
		int end = change.getRangeEnd();

		int anchor = change.getAnchor();
		int caret = change.getCaretPosition();

		StringBuilder newText = new StringBuilder(text);

		int dash;
		while ((dash = newText.lastIndexOf("-")) >= start) {
			newText.deleteCharAt(dash);
			if (caret > dash) {
				caret--;
			}
			if (anchor > dash) {
				anchor--;
			}
		}

		while (newText.length() < 3) {
			newText.append('#');
		}
		if (newText.length() == 3 || newText.charAt(3) != '-') {
			newText.insert(3, '-');
			if (caret > 3 || (caret == 3 && end <= 3 && change.isDeleted())) {
				caret++;
			}
			if (anchor > 3 || (anchor == 3 && end <= 3 && change.isDeleted())) {
				anchor++;
			}
		}

		while (newText.length() < 7) {
			newText.append('#');
		}
		if (newText.length() == 7 || newText.charAt(7) != '-') {
			newText.insert(7, '-');
			if (caret > 7 || (caret == 7 && end <= 7 && change.isDeleted())) {
				caret++;
			}
			if (anchor > 7 || (anchor == 7 && end <= 7 && change.isDeleted())) {
				anchor++;
			}
		}

		while (newText.length() < 12) {
			newText.append('#');
		}

		if (newText.length() > 12) {
			newText.delete(12, newText.length());
		}

		text = newText.toString();
		anchor = Math.min(anchor, 12);
		caret = Math.min(caret, 12);
		change.setText(text);
		change.setRange(0, change.getControlText().length());
		change.setAnchor(anchor);
		change.setCaretPosition(caret);

		return change;
	}

}
