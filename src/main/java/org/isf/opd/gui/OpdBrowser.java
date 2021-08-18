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
package org.isf.opd.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.PageableTableModel;
import org.isf.utils.jobjects.PaginatedTableDecoratorFull;
import org.isf.utils.jobjects.PaginationDataProvider;
import org.isf.utils.jobjects.VoLimitedTextField;

/**
 * ------------------------------------------
 * OpdBrowser - list all OPD. Let the user select an opd to edit or delete
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version
 * 07/11/2006 - ross - renamed from Surgery
 *                   - changed confirm delete message
 * 			         - version is now 1.0
 *    12/2007 - isf bari - multilanguage version
 * 			         - version is now 1.2
 * 21/06/2008 - ross - fixed getFilterButton method, need compare to translated string "female" to get correct filter
 *                   - displayed visitdate in the grid instead of opdDate (=system date)
 *                   - fixed "todate" bug (in case of 31/12: 31/12/2008 became 1/1/2008)
 * 			         - version is now 1.2.1
 * 09/01/2009 - fabrizio - Column full name appears only in OPD extended. Better formatting of OPD date.
 *                         Age column justified to the right. Cosmetic changed to code style.
 * 13/02/2009 - alex - fixed variable visibility in filtering mechanism
 * 06/02/2020 - alex - added search field for diseases
 *                   - version is now 1.2.2
 * ------------------------------------------
 */
public class OpdBrowser extends ModalJFrame implements OpdEdit.SurgeryListener, OpdEditExtended.SurgeryListener, PaginationDataProvider<Opd> {

	private static final long serialVersionUID = 2372745781159245861L;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
	private static final int DEFAULT_PAGE_SIZE = 100;
	private static final String CURRENT_YEAR = Year.now().toString();

	private JPanel jButtonPanel = null;
	private JPanel jContainPanel = null;
	private int pfrmHeight;
	private JButton jNewButton = null;
	private JButton jEditButton = null;
	private JButton jCloseButton = null;
	private JButton jDeleteButton = null;
	private JPanel jSelectionPanel = null;
	private JPanel dateFromPanel = null;
	private JPanel dateToPanel = null;
	private JTextField dayFrom = null;
	private JTextField monthFrom = null;
	private JTextField yearFrom = null;
	private JTextField dayTo = null;
	private JTextField monthTo = null;
	private JTextField yearTo = null;
	private JPanel jSelectionDiseasePanel = null;
	private JPanel jAgeFromPanel = null;
	private VoLimitedTextField jAgeFromTextField = null;
	private JPanel jAgeToPanel = null;
	private VoLimitedTextField jAgeToTextField = null;
	private JPanel jAgePanel = null;
	private JComboBox<DiseaseType> jDiseaseTypeBox;
	private JComboBox jDiseaseBox;
	private JPanel sexPanel = null;
	private JPanel newPatientPanel = null;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private DiseaseType allType= new DiseaseType(
			MessageBundle.getMessage("angal.common.alltypes.txt"),
			MessageBundle.getMessage("angal.common.alltypes.txt"));
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.opdnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientid.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.fullname.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.disease.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.diseasetype.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientstatus.col").toUpperCase()
	};
	private List<Opd> pSur;
	private int pSurSize;
	private JTable jTable = null;
	private OpdBrowsingModel model;
	private int[] pColumnWidth = {50, 50, 70, 70, 150, 30, 30, 195, 195, 50 };
	private boolean[] columnResizable = { false, false, false, false, true, false, false, true, true, false };
	private boolean[] columnsVisible = { true, true, true, GeneralData.OPDEXTENDED, GeneralData.OPDEXTENDED, true, true, true, true, true };
	private int[] columnsAlignment = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT };
	private boolean[] columnsBold = { true, true, false, true, false, false, false, false, false, false };
	private int selectedrow;
	private OpdBrowserManager manager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private JButton filterButton = null;
	private String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	private JLabel rowCounter = null;
	private JRadioButton radioNew;
	private JRadioButton radioAll;
	private final JFrame myFrame;
	private JRadioButton radiom;
	private JRadioButton radioa;
	private DiseaseBrowserManager diseaseManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private ArrayList<Disease> diseases = null;
	protected AbstractButton searchButton;
	
	private String disease;
	private String diseasetype;
	private char sex;
	private char newPatient;
	private GregorianCalendar dateFrom;
	private GregorianCalendar dateTo;

	private PaginatedTableDecoratorFull<Opd> paginatedDecorator;

	public JTable getJTable() {
		if (jTable == null) {
			model = new OpdBrowsingModel();
			jTable = new JTable(model);
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			TableColumnModel columnModel = jTable.getColumnModel();
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			for (int i = 0; i < model.getColumnCount(); i++) {
				columnModel.getColumn(i).setMinWidth(pColumnWidth[i]);
				columnModel.getColumn(i).setCellRenderer(new AlignmentCellRenderer());
				if (!columnResizable[i]) {
					columnModel.getColumn(i).setMaxWidth(pColumnWidth[i]);
				}
				if (!columnsVisible[i]) {
					columnModel.getColumn(i).setMaxWidth(0);
					columnModel.getColumn(i).setMinWidth(0);
					columnModel.getColumn(i).setPreferredWidth(0);
				}
			}
		}
		return jTable;
	}
	
	class AlignmentCellRenderer extends DefaultTableCellRenderer {  

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			setHorizontalAlignment(columnsAlignment[column]);
			if (columnsBold[column]) {
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}
	
	/**
	 * This method initializes
	 */
	public OpdBrowser() {
		super();
		myFrame = this;
		initialize();
        setVisible(true);
	}
	
	public OpdBrowser(Patient patient) {
		super();
		myFrame = this;
		initialize();
        setVisible(true);
        Opd newOpd = new Opd(0,' ',-1,new Disease());
        OpdEditExtended editrecord = new OpdEditExtended(myFrame, newOpd, patient, true);
        editrecord.addSurgeryListener(OpdBrowser.this);
		editrecord.showAsModal(myFrame);
	}
	
	/**
	 * This method initializes jButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnopdnew")) {
				jButtonPanel.add(getJNewButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopdedit")) {
				jButtonPanel.add(getJEditButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopddel")) {
				jButtonPanel.add(getJDeleteButton(), null);
			}
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 20;
		final int pfrmWidth = 17;
		final int pfrmHeight = 12;
		this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase) / 2,
				(screensize.height - screensize.height * pfrmHeight / pfrmBase) / 2,
				screensize.width * pfrmWidth / pfrmBase + 50,
				screensize.height * pfrmHeight / pfrmBase + 20);
		this.setTitle(MessageBundle.getMessage("angal.opd.opdoutpatientdepartment.title"));
		this.setContentPane(getJContainPanel());
		rowCounter.setText(rowCounterText + pSur.size());
		validate();
		this.setLocationRelativeTo(null);
	}
	
	/**
	 * This method initializes containPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(getJSelectionPanel(), java.awt.BorderLayout.WEST);
			paginatedDecorator = PaginatedTableDecoratorFull.decorate(getJTable(),
				              OpdBrowser.this, new int[]{5, 10, 20, 50, 75, 100}, DEFAULT_PAGE_SIZE);
			jContainPanel.add(paginatedDecorator.getContentPanel());
			validate();
		}
		return jContainPanel;
	}
	
	/**
	 * This method initializes jNewButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(event -> {
				Opd newOpd = new Opd(0,' ',-1,new Disease());
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended newrecord = new OpdEditExtended(myFrame, newOpd, true);
					newrecord.addSurgeryListener(OpdBrowser.this);
					newrecord.showAsModal(myFrame);
				} else {
					OpdEdit newrecord = new OpdEdit(myFrame, newOpd, true);
					newrecord.addSurgeryListener(OpdBrowser.this);
					newrecord.setLocationRelativeTo(myFrame);
					newrecord.setVisible(true);
				}
			});
		}
		return jNewButton;
	}

	/**
	 * This method initializes jEditButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(event -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = jTable.getSelectedRow();
				Opd opd = (Opd) (model.getValueAt(selectedrow, -1));
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended editrecord = new OpdEditExtended(myFrame, opd, false);
					editrecord.addSurgeryListener(OpdBrowser.this);
					editrecord.showAsModal(myFrame);
				} else {
					OpdEdit editrecord = new OpdEdit(myFrame, opd, false);
					editrecord.addSurgeryListener(OpdBrowser.this);
					editrecord.setLocationRelativeTo(myFrame);
					editrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}
	
	/**
	 * This method initializes jCloseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
            jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(arg0 -> dispose());
		}
		return jCloseButton;
	}

	/**
	 * This method initializes jDeleteButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(event -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Opd opd = (Opd) (model.getValueAt(jTable.getSelectedRow(), -1));
				String dt = '[' + MessageBundle.getMessage("angal.opd.notspecified.msg") + ']';
				try {
					DateFormat currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
					dt = currentDateFormat.format(opd.getVisitDate().getTime());
				} catch (Exception ex) {
				}

				String message = MessageBundle.formatMessage("angal.opd.deletefollowingopd.fmt.msg",
						DATE_FORMAT.format(opd.getDate()),
						opd.getDisease().getDescription() == null
								? '[' + MessageBundle.getMessage("angal.opd.notspecified.msg") + ']'
								: opd.getDisease().getDescription(),
						opd.getAge(),
						opd.getSex(),
						dt);

				int n = JOptionPane.showConfirmDialog(null, message,
						MessageBundle.getMessage("angal.messagedialog.question.title"), JOptionPane.YES_NO_OPTION);
				try {
					if ((n == JOptionPane.YES_OPTION) && (manager.deleteOpd(opd))) {
						pSur.remove(pSur.size() - jTable.getSelectedRow() - 1);
						model.fireTableDataChanged();
						jTable.updateUI();
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return jDeleteButton;
	}
	
	/**
	 * This method initializes jSelectionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			JLabel jLabel3 = new JLabel(MessageBundle.getMessage("angal.common.selectsex.txt"));
			JPanel sexLabelPanel = new JPanel();
			sexLabelPanel.add(jLabel3);
			JPanel newPatientLabelPanel = new JPanel();
			JLabel jLabel4 = new JLabel(MessageBundle.getMessage("angal.common.patient.txt"));
			newPatientLabelPanel.add(jLabel4);
			JPanel diseaseLabelPanel = new JPanel();
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.opd.selectadisease.txt"));
			diseaseLabelPanel.add(jLabel,null);
			JPanel filterButtonPanel = new JPanel();
			filterButtonPanel.add(getFilterButton());
			jSelectionPanel = new JPanel();
			jSelectionPanel.setLayout(new BoxLayout(getJSelectionPanel(),BoxLayout.Y_AXIS));
			jSelectionPanel.setPreferredSize(new Dimension(300, pfrmHeight));
			jSelectionPanel.add(diseaseLabelPanel,null);
			jSelectionPanel.add(getJSelectionDiseasePanel(),null);
			jSelectionPanel.add(Box.createVerticalGlue(), null);
			jSelectionPanel.add(getDateFromPanel());
			jSelectionPanel.add(getDateToPanel());
			jSelectionPanel.add(Box.createVerticalGlue(), null);
			jSelectionPanel.add(getJAgePanel(), null);
			jSelectionPanel.add(Box.createVerticalGlue(), null);
			jSelectionPanel.add(sexLabelPanel, null);
			jSelectionPanel.add(getSexPanel(), null);
			jSelectionPanel.add(newPatientLabelPanel, null);
			jSelectionPanel.add(getNewPatientPanel(), null);			
			jSelectionPanel.add(filterButtonPanel, null);
			jSelectionPanel.add(getRowCounter(), null);
		}
		return jSelectionPanel;
	}
	
	
	private JLabel getRowCounter() {
		if (rowCounter == null) {
			rowCounter = new JLabel();
			rowCounter.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return rowCounter;
	}

	private JPanel getDateFromPanel() {
		if (dateFromPanel == null) {
			dateFromPanel = new JPanel();
			dateFromPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")), null);
			dayFrom = new JTextField(2);
			dayFrom.setDocument(new DocumentLimit(2));
			dayFrom.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (dayFrom.getText().length() != 0) {
						if (dayFrom.getText().length() == 1) {
							String typed = dayFrom.getText();
							dayFrom.setText('0' + typed);
						}
						if (!isValidDay(dayFrom.getText())) {
							dayFrom.setText("1");
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			monthFrom = new JTextField(2);
			monthFrom.setDocument(new DocumentLimit(2));
			monthFrom.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (monthFrom.getText().length() != 0) {
						if (monthFrom.getText().length() == 1) {
							String typed = monthFrom.getText();
							monthFrom.setText('0' + typed);
						}
						if (!isValidMonth(monthFrom.getText())) {
							monthFrom.setText("1");
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			yearFrom = new JTextField(4);
			yearFrom.setDocument(new DocumentLimit(4));
			yearFrom.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (yearFrom.getText().length() == 4) {
						if (!isValidYear(yearFrom.getText())) {
							yearFrom.setText(CURRENT_YEAR);
						}
					} else {
						yearFrom.setText(CURRENT_YEAR);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			dateFromPanel.add(dayFrom);
			dateFromPanel.add(monthFrom);
			dateFromPanel.add(yearFrom);
			GregorianCalendar now = new GregorianCalendar();
			if (!GeneralData.ENHANCEDSEARCH) {
				now.add(Calendar.WEEK_OF_YEAR, -1);
			}
			dayFrom.setText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
			monthFrom.setText(String.valueOf(now.get(Calendar.MONTH) + 1));
			yearFrom.setText(String.valueOf(now.get(Calendar.YEAR)));
		}
		return dateFromPanel;
	}
	
	public class DocumentLimit extends DefaultStyledDocument {
		
		private static final long serialVersionUID = -5098766139884585921L;
		
		private final int maximumNumberOfCharacters;
		
		public DocumentLimit(int numeroMassimoCaratteri) {
			maximumNumberOfCharacters = numeroMassimoCaratteri;
		}
		
		@Override
		public void insertString(int off, String text, AttributeSet att) throws BadLocationException {
			int numberOfCharactersInDocument = getLength();
			int newTextLength = text.length();
			if (numberOfCharactersInDocument + newTextLength > maximumNumberOfCharacters) {
				int numeroCaratteriInseribili = maximumNumberOfCharacters - numberOfCharactersInDocument;
				if (numeroCaratteriInseribili > 0) {
					String parteNuovoTesto = text.substring(0, numeroCaratteriInseribili);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}
	
	
	private JPanel getDateToPanel() {
		if (dateToPanel == null) {
			dateToPanel = new JPanel();
			dateToPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")), null);
			dayTo = new JTextField(2);
			dayTo.setDocument(new DocumentLimit(2));
			dayTo.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (dayTo.getText().length() != 0) {
						if (dayTo.getText().length() == 1) {
							String typed = dayTo.getText();
							dayTo.setText('0' + typed);
						}
						if (!isValidDay(dayTo.getText())) {
							dayTo.setText("1");
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			monthTo = new JTextField(2);
			monthTo.setDocument(new DocumentLimit(2));
			monthTo.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (monthTo.getText().length() != 0) {
						if (monthTo.getText().length() == 1) {
							String typed = monthTo.getText();
							monthTo.setText('0' + typed);
						}
						if (!isValidMonth(monthTo.getText())) {
							monthTo.setText("1");
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			yearTo = new JTextField(4);
			yearTo.setDocument(new DocumentLimit(4));
			yearTo.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					if (yearTo.getText().length() == 4) {
						if (!isValidYear(yearTo.getText())) {
							yearTo.setText(CURRENT_YEAR);
						}
					} else {
						yearTo.setText(CURRENT_YEAR);
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			dateToPanel.add(dayTo);
			dateToPanel.add(monthTo);
			dateToPanel.add(yearTo);
			GregorianCalendar now = new GregorianCalendar();
			dayTo.setText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
			monthTo.setText(String.valueOf(now.get(Calendar.MONTH) + 1));
			yearTo.setText(String.valueOf(now.get(Calendar.YEAR)));
		}
		return dateToPanel;
	}

	private boolean isValidDay(String day) {
		if (!day.chars().allMatch(Character::isDigit)) {
			return false;
		}
		int num = Integer.parseInt(day);
		return num >= 1 && num <= 31;
	}
	
	private boolean isValidMonth(String month) {
		if (!month.chars().allMatch(Character::isDigit)) {
			return false;
		}
		int num = Integer.parseInt(month);
		return num >= 1 && num <= 12;
	}
	
	private boolean isValidYear(String year) {
		return year.chars().allMatch(Character::isDigit);
	}
	
	private GregorianCalendar getDateFrom() {
		return new GregorianCalendar(Integer.parseInt(yearFrom.getText()),
									 Integer.parseInt(monthFrom.getText()) - 1,
									 Integer.parseInt(dayFrom.getText()));
	}
	
	private GregorianCalendar getDateTo() {
		return new GregorianCalendar(Integer.parseInt(yearTo.getText()),
									 Integer.parseInt(monthTo.getText()) - 1,
									 Integer.parseInt(dayTo.getText()));
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox<DiseaseType> getDiseaseTypeBox() {
		if (jDiseaseTypeBox == null) {
			jDiseaseTypeBox = new JComboBox<>();
			jDiseaseTypeBox.setMaximumSize(new Dimension(300,50));
			
			DiseaseTypeBrowserManager diseaseTypeManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
			ArrayList<DiseaseType> types = null;
			try {
				types = diseaseTypeManager.getDiseaseType();
			} catch(OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
			
			jDiseaseTypeBox.addItem(allType);
			if (types != null){
				for (DiseaseType elem : types) {
					jDiseaseTypeBox.addItem(elem);
				}
			}
			
			jDiseaseTypeBox.addActionListener(e -> {
				jDiseaseBox.removeAllItems();
				getDiseaseBox();
			});
		}
		
		return jDiseaseTypeBox;
	}
	
	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getDiseaseBox() {
		if (jDiseaseBox == null) {
			jDiseaseBox = new JComboBox();
			jDiseaseBox.setMaximumSize(new Dimension(300, 50));
		}
		try {
			if (((DiseaseType)jDiseaseTypeBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.common.alltypes.txt"))){
				diseases = diseaseManager.getDiseaseOpd();
			} else {
				diseases = diseaseManager.getDiseaseOpd(((DiseaseType)jDiseaseTypeBox.getSelectedItem()).getCode());
			}
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		Disease allDisease = new Disease(MessageBundle.getMessage("angal.opd.alldiseases.txt"), MessageBundle.getMessage("angal.opd.alldiseases.txt"), allType);
		jDiseaseBox.addItem(allDisease);
		if (diseases != null){
			for (Disease elem : diseases) {
				jDiseaseBox.addItem(elem);
			}		
		}
		return jDiseaseBox;
	}
	
	/**
	 * This method initializes sexPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			ButtonGroup group = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			JRadioButton radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			radioa = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioa.setSelected(true);
			group.add(radiom);
			group.add(radiof);
			group.add(radioa);
			sexPanel.add(radioa);
			sexPanel.add(radiom);
			sexPanel.add(radiof);
		}
		return sexPanel;
	}
	
	public JPanel getNewPatientPanel() {
		if (newPatientPanel == null) {
			newPatientPanel = new JPanel();
			ButtonGroup groupNewPatient = new ButtonGroup();
			radioNew= new JRadioButton(MessageBundle.getMessage("angal.opd.new.btn"));
			JRadioButton radioRea= new JRadioButton(MessageBundle.getMessage("angal.opd.reattendance.btn"));
			radioAll= new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioAll.setSelected(true);
			groupNewPatient.add(radioAll);
			groupNewPatient.add(radioNew);
			groupNewPatient.add(radioRea);
			newPatientPanel.add(radioAll);
			newPatientPanel.add(radioNew);
			newPatientPanel.add(radioRea);
		}
		return newPatientPanel;
	}
	
	/**
	 * This method initializes jSelectionDiseasePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSelectionDiseasePanel() {
		if (jSelectionDiseasePanel == null) {
			JLabel jLabel2 = new JLabel();
			jLabel2.setText("    ");
			jSelectionDiseasePanel = new JPanel();
			jSelectionDiseasePanel.setLayout(new BoxLayout(jSelectionDiseasePanel,BoxLayout.Y_AXIS));
			jSelectionDiseasePanel.add(getDiseaseTypeBox(), null);
			jSelectionDiseasePanel.add(jLabel2, null);
			jSelectionDiseasePanel.add(getJSearchDiseaseTextFieldPanel(), null);
			jSelectionDiseasePanel.add(getDiseaseBox(), null);
		}
		return jSelectionDiseasePanel;
	}
	
	private JPanel getJSearchDiseaseTextFieldPanel() {
		JPanel searchFieldPanel = new JPanel();
		JTextField searchDiseasetextField = new JTextField(10);
		searchFieldPanel.add(searchDiseasetextField);
        searchDiseasetextField.addKeyListener(new KeyListener() {
            
			@Override
			public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
            @Override
            public void keyTyped(KeyEvent e) {}
        });

        searchButton = new JButton("");
        searchFieldPanel.add(searchButton);
		searchButton.addActionListener(arg0 -> {
			jDiseaseBox.removeAllItems();
			jDiseaseBox.addItem("");
			for (Disease disease : getSearchDiagnosisResults(searchDiseasetextField.getText(), diseases)) {
				jDiseaseBox.addItem(disease);
			}

			if (jDiseaseBox.getItemCount() >= 2) {
				jDiseaseBox.setSelectedIndex(1);
			}
			jDiseaseBox.requestFocus();
			if (jDiseaseBox.getItemCount() > 2) {
				jDiseaseBox.showPopup();
			}
		});
        searchButton.setPreferredSize(new Dimension(20, 20));
        searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		return searchFieldPanel;
	}
	
	private ArrayList<Disease> getSearchDiagnosisResults(String s, ArrayList<Disease> diseaseList) {
		String query = s.trim();
		ArrayList<Disease> results = new ArrayList<>();
		for (Disease disease : diseaseList) {
			if (!query.isEmpty()) {
				String[] patterns = query.split(" ");
				String name = disease.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						// It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(disease);
				}
			} else {
				results.add(disease);
			}
		}
		return results;
	}

	/**
	 * This method initializes jAgePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAgeFromPanel() {
		if (jAgeFromPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.agefrom.label"));
			jAgeFromPanel = new JPanel();
			jAgeFromPanel.add(jLabel, null);
			jAgeFromPanel.add(getJAgeFromTextField(), null);
		}
		return jAgeFromPanel;
	}
	
	/**
	 * This method initializes jAgeFromTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private VoLimitedTextField getJAgeFromTextField() {
		if (jAgeFromTextField == null) {
			jAgeFromTextField = new VoLimitedTextField(3, 2);
			jAgeFromTextField.setText("0");
			jAgeFromTextField.setMinimumSize(new Dimension(100, 50));
			ageFrom = 0;
			jAgeFromTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						if (ageFrom < 0 || ageFrom > 200) {
							jAgeFromTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("");
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeFromTextField;
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAgeToPanel() {
		if (jAgeToPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.ageto.label"));
			jAgeToPanel = new JPanel();
			jAgeToPanel.add(jLabel, null);
			jAgeToPanel.add(getJAgeToTextField(), null);
		}
		return jAgeToPanel;
	}
	
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private VoLimitedTextField getJAgeToTextField() {
		if (jAgeToTextField == null) {
			jAgeToTextField = new VoLimitedTextField(3, 2);
			jAgeToTextField.setText("0");
			jAgeToTextField.setMaximumSize(new Dimension(100, 50));
			ageTo = 0;
			jAgeToTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						if (ageTo < 0 || ageTo > 200) {
							jAgeToTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("");
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeToTextField;
	}

	/**
	 * This method initializes jAgePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setLayout(new BoxLayout(getJAgePanel(),BoxLayout.Y_AXIS));
			jAgePanel.add(getJAgeFromPanel(), null);
			jAgePanel.add(getJAgeToPanel(), null);
		}
		return jAgePanel;
	}

	class OpdBrowsingModel extends PageableTableModel<Opd>  {

		private static final long serialVersionUID = -9129145534999353730L;

		public OpdBrowsingModel(String diseaseTypeCode, String diseaseCode, GregorianCalendar dateFrom, GregorianCalendar dateTo, int ageFrom, int ageTo,
						char sex, char newPatient, int pageNumber, int pageSize) {
			pSur = manager.getOpdPaginated(diseaseTypeCode, diseaseCode, dateFrom, dateTo, ageFrom, ageTo, sex, newPatient, 
							pageNumber, 
							pageSize == 0 ? DEFAULT_PAGE_SIZE : pageSize);
			try {
				pSurSize = manager.getOpd(diseasetype, disease, dateFrom, dateTo, ageFrom, ageTo, sex, newPatient).size();
			} catch (OHServiceException  ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		public OpdBrowsingModel() {
			try {
				pSur = manager.getOpd(!GeneralData.ENHANCEDSEARCH);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			if (pSur == null) {
				return 0;
			}
			return pSur.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(Opd opd, int c) {
			Patient pat = opd.getPatient();
			if (c == -1) {
				return opd;
			} else if (c == 0) {
				return opd.getCode();
			} else if (c == 1) {
				return opd.getProgYear();
			} else if (c == 2) {
				String sVisitDate;
				if (opd.getVisitDate() == null) {
					sVisitDate = "";
				} else {
					sVisitDate = DATE_FORMAT.format(opd.getVisitDate().getTime());
				}
				return sVisitDate;
			} else if (c == 3) {
				return pat != null ? opd.getPatient().getCode() : null;
			} else if (c == 4) {
				return pat != null ? opd.getFullName() : null;
			} else if (c == 5) {
				return opd.getSex();
			} else if (c == 6) {
				return opd.getAge();
			} else if (c == 7) {
				return opd.getDisease().getDescription();
			} else if (c == 8) {
				return opd.getDisease().getType().getDescription();
			} else if (c == 9) {
				String patientStatus;
				if (opd.getNewPatient() == 'N') {
					patientStatus = MessageBundle.getMessage("angal.opd.new.btn");
				} else {
					patientStatus = MessageBundle.getMessage("angal.opd.reattendance.btn");
				}
				return patientStatus;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

		@Override
		public String getFieldName(int column) {
			return null;
		}

	}
	
	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		pSur.set(pSur.size() - selectedrow - 1, opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if ((jTable.getRowCount() > 0) && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
		rowCounter.setText(rowCounterText + pSur.size());
	}
	
	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
		pSur.add(pSur.size(), opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
		rowCounter.setText(rowCounterText + pSur.size());
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(e -> {
				Object selectedItem = jDiseaseBox.getSelectedItem();
				if (!(selectedItem instanceof Disease)) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.pleaseselectadisease.msg");
					return;
				}
				disease = ((Disease) selectedItem).getCode();
				diseasetype = ((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getCode();

				if (radioa.isSelected()) {
					sex = 'A';
				} else if (radiom.isSelected()) {
					sex = 'M';
				} else {
					sex = 'F';
				}

				if (radioAll.isSelected()) {
					newPatient = 'A';
				} else if (radioNew.isSelected()) {
					newPatient = 'N';
				} else {
					newPatient = 'R';
				}

				dateFrom = getDateFrom();
				dateTo = getDateTo();

				if (dateFrom.after(dateTo)) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.datefrommustbebefordateto.msg");
					return;
				}

				if (ageFrom > ageTo) {
					MessageDialog.error(OpdBrowser.this, "angal.opd.agefrommustbelowerthanageto.msg");
					jAgeFromTextField.setText(ageTo.toString());
					ageFrom = ageTo;
					return;
				}

				try {
					pSurSize = manager.getOpd(diseasetype, disease, dateFrom, dateTo, ageFrom, ageTo, sex, newPatient).size();
				} catch (OHServiceException  ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
				if (getTotalRowCount() > DEFAULT_PAGE_SIZE * 10) {
					int ok = JOptionPane.showConfirmDialog(OpdBrowser.this,
							MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
							MessageBundle.getMessage("angal.messagedialog.question.title"),
							JOptionPane.OK_CANCEL_OPTION);
					if (ok != JOptionPane.OK_OPTION) {
						return;
					}
				}
				rowCounter.setText(rowCounterText + getTotalRowCount());
				paginatedDecorator.paginate();
			});
		}
		return filterButton;
	}

	@Override
	public int getTotalRowCount() {
		return pSurSize;
	}

	@Override
	public List<Opd> getRows(int pageNumber, int pageSize) {
		model = new OpdBrowsingModel(diseasetype, disease, getDateFrom(), getDateTo(), ageFrom, ageTo, sex, newPatient, pageNumber, pageSize);
		return pSur;
	}

} 
