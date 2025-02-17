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
package org.isf.xmpp.gui;

import static org.isf.utils.Constants.TIME_FORMAT_HH_MM_SS;

import java.awt.Color;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatMessages extends JTextPane {

	private static final long serialVersionUID = 1L;
	private Document sDoc;
	private Color greenColor = new Color(0, 100, 0);
	private Color blueColor = new Color(176, 23, 31);
	private Color redColor = new Color(25, 25, 112);
	private SimpleAttributeSet keyWord;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_HH_MM_SS);

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessages.class);

	public ChatMessages() {
		setEditable(false);
		setMinimumSize(getSize());
		keyWord = new SimpleAttributeSet();
	}

	//print general notification
	public void printNotification(String notification) throws BadLocationException {
		StyleConstants.setForeground(keyWord, greenColor);
		sDoc = getDocument();
		sDoc.insertString(sDoc.getEndPosition().getOffset(), "*** " + notification + "\n", keyWord);
	}

	//print notification of file transfer
	public void printNotification(String name, String fileTransfer, JButton accept, JButton reject) {

		Document doc = getDocument();
		int position = doc.getEndPosition().getOffset();
		StyleConstants.setForeground(keyWord, greenColor);
		try {
			doc.insertString(position, "\n*** " + fileTransfer + "\n", keyWord);
		} catch (BadLocationException badLocationException) {
			LOGGER.error(badLocationException.getMessage(), badLocationException);
		}
		position = doc.getEndPosition().getOffset();
		select(position, position);

		insertComponent(accept);
		insertComponent(reject);
	}

	//print send and received messages
	public void printMessage(String user, String message, boolean incomingType) throws BadLocationException {
		StyleConstants.setBold(keyWord, true);
		if (incomingType) {
			StyleConstants.setForeground(keyWord, blueColor);
		} else {
			StyleConstants.setForeground(keyWord, redColor);
		}
		sDoc = getDocument();
		sDoc.insertString(sDoc.getEndPosition().getOffset(), "(" + simpleDateFormat.format(new Date()) + ") " + user + " : ", keyWord);
		StyleConstants.setBold(keyWord, false);
		StyleConstants.setForeground(keyWord, Color.black);
		sDoc.insertString(sDoc.getEndPosition().getOffset(), message + "\n", keyWord);
	}

	public void printReport(String name, String report) {
		final String fromDate;
		final String toDate;
		final String typeReport;

		Document doc = getDocument();
		ImageIcon open = new ImageIcon("rsc/icons/open.png");
		final JButton view = new JButton(open);
		view.setMargin(new Insets(1, 1, 1, 1));
		view.setOpaque(false);
		view.setBorderPainted(false);
		view.setContentAreaFilled(false);

		String[] reports = new String[4];
		int i = 0;
		StringTokenizer st = new StringTokenizer(report);
		while (st.hasMoreTokens()) {
			reports[i] = st.nextToken();
			i++;
		}
		fromDate = reports[1];
		LOGGER.debug("fromDate: {}", reports[1]);
		toDate = reports[2];
		LOGGER.debug("toDate: {}", reports[2]);
		typeReport = reports[3];
		LOGGER.debug("typeReport: {}", reports[3]);
		int position = doc.getEndPosition().getOffset();
		StyleConstants.setForeground(keyWord, greenColor);

		try {
			doc.insertString(position, MessageBundle.formatMessage("angal.xmpp.wantstosharewithyouthisreport.fmt.msg", name, typeReport), keyWord);
		} catch (BadLocationException badLocationException) {
			LOGGER.error(badLocationException.getMessage(), badLocationException);
		}
		position = doc.getEndPosition().getOffset();
		select(position, position);

		insertComponent(view);
		view.addActionListener(actionEvent -> {
			new GenericReportFromDateToDate(fromDate, toDate, typeReport, typeReport, false);
			view.setEnabled(false);
		});
	}

}
