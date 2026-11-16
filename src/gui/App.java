package gui;
//Sparking! AMBR (AniMationBoneReplacer) by ViveTheJoestar
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import cmd.Animation;
import cmd.Main;

public class App {
	private static final Font BOLD_L = new Font("Tahoma", 1, 34);
	private static final Font BOLD_M = new Font("Tahoma", 1, 24);
	private static final Font BOLD_S = new Font("Tahoma", 1, 14);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String HTML_START = "<html><div style='font-size: 14px; font-style: italic;'>";
	private static final String HTML_END = "</div></html>";
	private static final String WINDOW_TITLE = "Sparking! AMBR - Volume 3";

	private static void errorBeep(Toolkit tk) {
		Runnable runWinErrorSnd = (Runnable) tk.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd != null) runWinErrorSnd.run();
	}
	private static void errorDialog(Exception e, Toolkit tk) {
		errorBeep(tk);
		JOptionPane.showMessageDialog(null, Main.getExceptionText(e), "Exception", JOptionPane.ERROR_MESSAGE);
	}
	private static File getFileFromChooser(File[] allDirs, Toolkit tk, boolean[] bigEndian) throws IOException {
		File file = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Animation (*.ANM)", "anm");
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);		
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Select ANM File/Folder...");
		if (allDirs[2] != null) fc.setCurrentDirectory(allDirs[2]);
		while (file == null) {
			int result = fc.showOpenDialog(fc);
			if (result == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				if (file.isFile()) {
					Animation anm = new Animation(file, bigEndian[0]);
					if (!anm.isValidAnimation()) {
						errorBeep(tk);
						JOptionPane.showMessageDialog(null, "Invalid ANM file provided!", WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
						return null;
					}
				}
				allDirs[2] = file;
			}
			else break;
		}
		return file;
	}
	@SuppressWarnings("unchecked")
	private static void setApp(String[] boneNames, Toolkit tk) {
		final boolean[] bigEndian = new boolean[1];
		final int[] anmCnt = new int[1];
		final File[] allDirs = new File[3];
		final JProgressBar[] bar = new JProgressBar[1];
		String[] hexColors = { "#7FFFD4", "#FF00FF", "#A8AE9C", "#E30B5D", "#228B22", "#DC143C", "#FFBF01", "#FFD700", "#000000", "#221F1E" };
		String[] text = { "Source", "Destination", "Start", "End", "first", "last" };
		String[] titleText = { "Ani", "Mation", "Bone", "Replacer" };
		//Initialize components
		Box[] dropdownBoxes = new Box[2];
		Box titleBox = Box.createHorizontalBox();
		GridBagConstraints gbc = new GridBagConstraints();
		Image img = tk.getImage(ClassLoader.getSystemResource("img/ambr.png"));
		Image imgSmall = img.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
		ImageIcon icon = new ImageIcon(imgSmall);
		JButton apply = new JButton("Replace Bone Contents");
		JComboBox<String>[] dropdowns = new JComboBox[dropdownBoxes.length];
		JCheckBox wiiMode = new JCheckBox("Wii Mode");
		JFrame frame = new JFrame(WINDOW_TITLE);
		JLabel iconLabel = new JLabel("");
		JLabel[] empty = new JLabel[dropdowns.length], dropdownLabels = new JLabel[dropdowns.length], titleLabels = new JLabel[titleText.length];
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem[] folderSelects = new JMenuItem[dropdowns.length];
		JPanel panel = new JPanel(new GridBagLayout());
		//Set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		iconLabel.setIcon(icon);
		titleBox.add(iconLabel);
		for (int i = 0; i < titleLabels.length; i++) {
			titleLabels[i] = new JLabel(titleText[i]);
			titleLabels[i].setFont(BOLD_L);
			titleLabels[i].setForeground(Color.decode(hexColors[i]));
			titleBox.add(titleLabels[i]);
		}
		for (int i = 0; i < dropdowns.length; i++) {
			dropdownBoxes[i] = Box.createHorizontalBox();
			dropdownBoxes[i].setToolTipText(HTML_START + "The " + text[4 + i] + " bone included in the selection." + HTML_END);
			JLabel boneSel = new JLabel("Bone Selection ");
			boneSel.setFont(BOLD_M);
			boneSel.setForeground(Color.decode(hexColors[2]));
			empty[i] = new JLabel(" ");
			empty[i].setFont(BOLD_L);
			folderSelects[i] = new JMenuItem("Select " + text[i] + " ANM File/Folder...");
			fileMenu.add(folderSelects[i]);
			dropdownLabels[i] = new JLabel(text[2 + i]);
			dropdowns[i] = new JComboBox<String>(boneNames);
			dropdownLabels[i].setFont(BOLD_M);
			dropdownLabels[i].setForeground(Color.decode(hexColors[4 + i]));
			dropdownBoxes[i].add(boneSel);
			dropdownBoxes[i].add(dropdownLabels[i]);
			dropdowns[i].setFont(MED);
			//Thank goodness for the default renderer
			((JLabel) dropdowns[i].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		}
		apply.setBackground(Color.decode(hexColors[hexColors.length - 3]));
		apply.setContentAreaFilled(false);
		apply.setForeground(Color.decode(hexColors[hexColors.length - 2]));
		apply.setFont(BOLD_M);
		apply.setOpaque(true);
		apply.setToolTipText(HTML_START + "Empty or invalid ANMs will be skipped." + HTML_END);
		panel.setBackground(Color.decode(hexColors[hexColors.length - 1]));
		wiiMode.setBackground(Color.decode(hexColors[hexColors.length - 1]));
		wiiMode.setFont(MED);
		wiiMode.setForeground(Color.decode(hexColors[hexColors.length - 4]));
		wiiMode.setToolTipText(HTML_START + "When checked, the byte order will be set to Big Endian." + HTML_END);
		//Add action listeners
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Box horBox = Box.createHorizontalBox();
				JLabel label = new JLabel("Made by: ");
				JLabel author = new JLabel(HTML_A_START + "ViveTheJoestar" + HTML_A_END);
				label.setFont(MED);
				author.setFont(MED);
				author.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(new URI("https://github.com/ViveTheModder"));
						} catch (IOException | URISyntaxException ex) {
							errorDialog(ex, tk);
						}
					}
				});
				horBox.add(label);
				horBox.add(author);
				JOptionPane.showMessageDialog(null, horBox, WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE, icon);
			}
		});
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String errors = "";
				for (int i = 0; i < dropdowns.length; i++) {
					if (allDirs[i] == null)
						errors += "No " + text[i] + " ANM file/folder has been selected!\n";
				}
				if (!errors.equals("")) {
					errorBeep(tk);
					JOptionPane.showMessageDialog(null, errors, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
				}
				else {
					int[] boneIds = new int[2];
					for (int i = 0; i < boneIds.length; i++)
						boneIds[i] = dropdowns[i].getSelectedIndex();
					if (allDirs[1].isFile() && allDirs[2].isFile()) {
						try {
							long start = System.currentTimeMillis();
							String[] output = Main.writeSrcAnms(null, allDirs, anmCnt, boneIds, boneNames, bigEndian);
							long finish = System.currentTimeMillis();
							double time = (finish - start) / 1000.0;
							if (!output[1].equals("")) {
								errorBeep(tk);
								JOptionPane.showMessageDialog(null, output[1], App.WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
							}
							if (!output[0].equals("")) {
								errorBeep(tk);
								JOptionPane.showMessageDialog(null, output[0], App.WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
							}
							else {
								tk.beep();
								String success = " ANM file has been changed in " + time + " seconds!";
								JOptionPane.showMessageDialog(null, anmCnt[0] + success, App.WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
							}
						}
						catch (IOException ex) {
							errorDialog(ex, tk);
						}
					}
					else setProgress(allDirs, frame, bar, anmCnt, boneIds, boneNames, hexColors, tk, bigEndian);
				}
			}
		});
		wiiMode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean sel = e.getStateChange() == ItemEvent.SELECTED;
				bigEndian[0] = sel ? true : false;
			}
		});
		for (int i = 0; i < dropdowns.length; i++) {
			final int index = i;
			folderSelects[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						allDirs[index] = getFileFromChooser(allDirs, tk, bigEndian);
						((JLabel) dropdowns[0].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
						((JLabel) dropdowns[1].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
					}
					catch (IOException ex) {
						errorDialog(ex, tk);
					}
				}
			});
		}
		//Add components
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		panel.add(titleBox, gbc);
		panel.add(empty[0], gbc);
		for (int i = 0; i < dropdowns.length; i++) {
			panel.add(dropdownBoxes[i], gbc);
			panel.add(dropdowns[i], gbc);
			panel.add(new JLabel(" "), gbc);
		}
		panel.add(empty[1], gbc);
		panel.add(apply, gbc);
		panel.add(wiiMode, gbc);
		frame.add(panel);
		//Set frame properties
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setIconImage(img);
		frame.setMinimumSize(new Dimension(528, 512));
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setTitle(WINDOW_TITLE);
		frame.setVisible(true);
	}
	private static void setProgress(File[] dirs, JFrame frame, JProgressBar[] bar, int[] anmCnt, int[] ids, String[] names, String[] clrs, Toolkit tk, boolean[] be) {
		//Change progress bar settings (must be done before declaring)
		UIManager.put("ProgressBar.background", Color.WHITE);
		UIManager.put("ProgressBar.foreground", Color.decode(clrs[4]));
		UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
		UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		//Initialize components
		Dimension barSize = new Dimension(256, 32);
		GridBagConstraints gbc = new GridBagConstraints();
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel("Replacing bone contents...");
		bar[0] = new JProgressBar();
		//Set component properties
		bar[0].setValue(0);
		bar[0].setStringPainted(true);
		bar[0].setBorderPainted(true);
		bar[0].setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY));
		bar[0].setFont(BOLD_S);
		bar[0].setMinimumSize(barSize);
		bar[0].setMaximumSize(barSize);
		bar[0].setPreferredSize(barSize);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		label.setBackground(Color.decode(clrs[clrs.length - 1]));
		label.setFont(BOLD_M);
		label.setForeground(Color.decode(clrs[2]));
		panel.setBackground(Color.decode(clrs[clrs.length - 1]));
		//Add components
		panel.add(label, gbc);
		panel.add(new JLabel(" "), gbc);
		panel.add(bar[0], gbc);
		dialog.add(panel);
		dialog.setTitle(WINDOW_TITLE);
		dialog.setMinimumSize(new Dimension(528, 512));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				long start = System.currentTimeMillis();
				frame.setEnabled(false);
				String[] output = Main.writeSrcAnms(bar, dirs, anmCnt, ids, names, be);
				long finish = System.currentTimeMillis();
				double time = (finish - start) / 1000.0;
				if (!output[0].equals("")) {
					dialog.dispose();
					errorBeep(tk);
					JOptionPane.showMessageDialog(null, output[0], App.WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
				}
				else {
					tk.beep();
					String success = " ANM files have been changed in " + time + " seconds!";
					JOptionPane.showMessageDialog(null, anmCnt[0] + success, App.WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
					dialog.dispose();
				}
				frame.setEnabled(true);
				return null;
			}
		};
		worker.execute();
	}

	public static void launch() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			File csv = new File("./csv/bone-ids.csv");
			if (csv.isFile()) {
				String[] boneNames = Main.getBoneNames(csv, true);
				setApp(boneNames, tk);
			}
			else
				JOptionPane.showMessageDialog(null, "bone-ids.csv is NOT present!", WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			errorDialog(e, tk);
		}
	}
}