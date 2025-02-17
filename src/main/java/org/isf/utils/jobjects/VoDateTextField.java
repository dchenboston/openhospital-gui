/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;

import static org.isf.utils.Constants.DATE_FORMATTER;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YY;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.DATE_FORMAT_MM_DD_YY;
import static org.isf.utils.Constants.DATE_FORMAT_MM_DD_YYYY;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.time.Converters;

/**
 * Returns a JTextField for date it can manage dates in format dd/mm/yyyy, dd/mm/yy, mm/dd/yyyy or mm/dd/yy
 *
 * @author Rick
 */
public class VoDateTextField extends JTextField {

	private static final long serialVersionUID = 1L;

	private String type;
	private String currentDate = "nothing";

	private void setType(String type) {
		this.type = type;
	}

	public class ManagedData extends DefaultStyledDocument {

		private static final long serialVersionUID = 1L;
		private int maxChars = 0;

		private ManagedData() {
			if (type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_MM_DD_YY)) {
				this.maxChars = 8;
			} else if (type.equals(DATE_FORMAT_DD_MM_YYYY) || type.equals(DATE_FORMAT_MM_DD_YYYY)) {
				this.maxChars = 10;
			}
		}

		@Override
		public void insertString(int off, String text2, AttributeSet att)
				throws BadLocationException {
			int charsInDocument = getLength();
			int newLength = text2.length();
			if (charsInDocument + newLength > maxChars) {
				int availableChars = maxChars - charsInDocument;
				if (availableChars > 0) {
					String parteNuovoTesto = text2.substring(0, availableChars);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text2, att);
			}
		}
	}

	/**
	 * Constructor with no default date
	 */
	public VoDateTextField(String type, int cols) throws IllegalArgumentException {
		super(cols);
		if (!(type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_DD_MM_YYYY) ||
				type.equals(DATE_FORMAT_MM_DD_YY) || type.equals(DATE_FORMAT_MM_DD_YYYY))) {
			throw new IllegalArgumentException();
		}
		setType(type);
		this.setDocument(new ManagedData());
		this.setFont(new Font("monospaced", Font.PLAIN, 12));
		check();
	}

	/**
	 * Constructor with default date
	 */
	public VoDateTextField(String type, String todayDate, int cols) throws IllegalArgumentException {
		super(cols);
		if (!(type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_DD_MM_YYYY)
				|| type.equals(DATE_FORMAT_MM_DD_YY) || type.equals(DATE_FORMAT_MM_DD_YYYY))) {
			throw new IllegalArgumentException();
		}
		setType(type);
		this.setDocument(new ManagedData());
		this.setFont(new Font("monospaced", Font.PLAIN, 12));
		this.setText(todayDate);
		this.currentDate = todayDate;
		check();
	}

	public VoDateTextField(String type, LocalDate todayDate, int cols) throws IllegalArgumentException {
		super(cols);
		if (!(type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_DD_MM_YYYY)
				|| type.equals(DATE_FORMAT_MM_DD_YY) || type.equals(DATE_FORMAT_MM_DD_YYYY))) {
			throw new IllegalArgumentException();
		}
		setType(type);
		this.setDocument(new ManagedData());
		this.setFont(new Font("monospaced", Font.PLAIN, 12));
		this.setText(getConvertedString(todayDate));
		this.setDate(todayDate);
		check();
	}

	/**
	 * When focus is lost check if the date is correct or not
	 */
	private void check() {

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) throws IllegalArgumentException {
				// if date field is not mandatory, can be left empty
				if (getText().length() == 0) {
					return;
				}
				if (getText().length() != type.length()) {
					MessageDialog.error(null, MessageBundle.formatMessage("angal.vodatetext.isnotavaliddate.fmt.msg", getText()));
					if (currentDate.equals("nothing")) {
						setText("");
					} else {
						setText(currentDate);
					}
					return;
				}
				char separator = getText().charAt(2);
				if (((separator == '/') || (separator == '-')) && (getText().charAt(5) == separator)) {
					try {
						GregorianCalendar gc = new GregorianCalendar();
						gc.setLenient(false); //must do this
						if (type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_MM_DD_YY)) {
							gc.set(Calendar.YEAR, Integer.parseInt(getText().substring(6, 8)) + 2000);
						} else if (type.equals(DATE_FORMAT_DD_MM_YYYY) || type.equals(DATE_FORMAT_MM_DD_YYYY)) {
							gc.set(Calendar.YEAR, Integer.parseInt(getText().substring(6, 10)));
						}

						if (type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_DD_MM_YYYY)) {
							gc.set(Calendar.MONTH, Integer.parseInt(getText().substring(3, 5)) - 1);
						} else {
							gc.set(Calendar.MONTH, Integer.parseInt(getText().substring(0, 2)) - 1);
						}

						if (type.equals(DATE_FORMAT_DD_MM_YY) || type.equals(DATE_FORMAT_DD_MM_YYYY)) {
							gc.set(Calendar.DATE, Integer.parseInt(getText().substring(0, 2)));
						} else {
							gc.set(Calendar.DATE, Integer.parseInt(getText().substring(3, 5)));
						}
						gc.getTime(); //exception thrown here (if needed)
						currentDate = getText();
					} catch (Exception e1) {
						MessageDialog.error(null, MessageBundle.formatMessage("angal.vodatetext.isnotavaliddate.fmt.msg", getText()));
						if (currentDate.equals("nothing")) {
							setText("");
						} else {
							setText(currentDate);
						}
					}
				} else {
					MessageDialog.error(null, MessageBundle.formatMessage("angal.vodatetext.isnotavaliddatepleaseuseortoseparate.fmt.msg", getText()));
				}
			}
		});
	}

	/**
	 * Returns a GregorianCalendar for date use
	 */
	public GregorianCalendar getDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setLenient(false);
		if (type.equals(DATE_FORMAT_DD_MM_YYYY)) {
			calendar.set(Integer.parseInt(getText().substring(6, 10)), Integer.parseInt(getText().substring(3, 5)) - 1,
					Integer.parseInt(getText().substring(0, 2)));
		} else if (type.equals(DATE_FORMAT_MM_DD_YYYY)) {
			calendar.set(Integer.parseInt(getText().substring(6, 10)), Integer.parseInt(getText().substring(0, 2)) - 1,
					Integer.parseInt(getText().substring(3, 5)));
		} else if (type.equals(DATE_FORMAT_DD_MM_YY)) {
			calendar.set(Integer.parseInt(getText().substring(6, 8)) + 2000, Integer.parseInt(getText().substring(3, 5)) - 1,
					Integer.parseInt(getText().substring(0, 2)));
		} else if (type.equals(DATE_FORMAT_MM_DD_YY)) {
			calendar.set(Integer.parseInt(getText().substring(6, 8)) + 2000, Integer.parseInt(getText().substring(0, 2)) - 1,
					Integer.parseInt(getText().substring(3, 5)));
		}
		return calendar;
	}

	public LocalDate getLocalDate() {
		return Converters.convertToLocalDateTime(getDate()).toLocalDate();
	}

	public void setDate(LocalDate date) {
		currentDate = date.format(DATE_FORMATTER);
	}

	private String getConvertedString(LocalDate date) {
		return date.format(DATE_FORMATTER);
	}

}
