import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayDeque;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/*
 * UI.java
 * 
 * User Interface that controls the running of all operations
 * Allows operations to be run for multiple algorithms at one time
 * Allows user to specify their own dataset, qrels and queries
 * Provides Analysis tab for quickly viewing and comparing TREC_EVAL evaluations
 * 
 */
public class UI extends parseDocuments {

	private JFrame frame;
	private JTextField txtGprojectdataset;
	private JTextField textField_1;
	private String source;
	private int loop;
	private java.util.List<String> allLines;
	ArrayDeque<String> selected = new ArrayDeque<String>();

	/**
	 * Launch the application
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI window = new UI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application
	 */
	public UI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame
	 */
	private void initialize() {
		frame = new JFrame("Medical Information Retrieval Evaluation");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Run", null, panel_1, null);
		panel_1.setLayout(null);
		
		JButton btnRunQueries = new JButton("Run Queries");
		
		btnRunQueries.setBounds(109, 199, 109, 23);
		panel_1.add(btnRunQueries);
		
		JButton btnRunTreceval = new JButton("Run TREC_EVAL");
		
		btnRunTreceval.setBounds(228, 199, 127, 23);
		panel_1.add(btnRunTreceval);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("BM25");
		chckbxNewCheckBox.setSelected(true);
		chckbxNewCheckBox.setBounds(283, 7, 97, 23);
		panel_1.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("LM Jelinek Mercer");
		chckbxNewCheckBox_1.setBounds(283, 137, 116, 23);
		panel_1.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox_2 = new JCheckBox("IB");
		chckbxNewCheckBox_2.setBounds(283, 163, 97, 23);
		panel_1.add(chckbxNewCheckBox_2);
		
		JCheckBox chckbxNewCheckBox_3 = new JCheckBox("DFI");
		chckbxNewCheckBox_3.setBounds(283, 33, 97, 23);
		panel_1.add(chckbxNewCheckBox_3);
		
		JCheckBox chckbxNewCheckBox_4 = new JCheckBox("DFR");
		chckbxNewCheckBox_4.setBounds(283, 111, 97, 23);
		panel_1.add(chckbxNewCheckBox_4);
		
		JCheckBox chckbxNewCheckBox_5 = new JCheckBox("LM Dirichlet");
		chckbxNewCheckBox_5.setBounds(283, 85, 97, 23);
		panel_1.add(chckbxNewCheckBox_5);
		
		JCheckBox chckbxNewCheckBox_6 = new JCheckBox("TF-IDF");
		chckbxNewCheckBox_6.setBounds(283, 59, 97, 23);
		panel_1.add(chckbxNewCheckBox_6);
		
		
		
		txtGprojectdataset = new JTextField(); // Path to dataset field
		txtGprojectdataset.setForeground(Color.BLUE);
		txtGprojectdataset.setText("G:\\Project\\dataset");
		txtGprojectdataset.setBounds(20, 98, 159, 20);
		panel_1.add(txtGprojectdataset); // 'Run' panel
		txtGprojectdataset.setColumns(10);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setToolTipText("");
		separator_1.setForeground(Color.LIGHT_GRAY);
		separator_1.setBackground(Color.LIGHT_GRAY);
		separator_1.setBounds(276, 68, 1, 120);
		panel_1.add(separator_1);
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.DARK_GRAY);
		separator.setForeground(Color.DARK_GRAY);
		separator.setBounds(10, 59, 256, 1);
		panel_1.add(separator);
		
		JLabel lblNewLabel = new JLabel("Document directory:");
		lblNewLabel.setBounds(10, 78, 127, 14);
		panel_1.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Path to queryfile:");
		lblNewLabel_1.setBounds(10, 137, 96, 14);
		panel_1.add(lblNewLabel_1);
		
		textField_1 = new JTextField(); // Queryfile path entry field
		textField_1.setBounds(20, 153, 159, 20);
		panel_1.add(textField_1);
		textField_1.setColumns(10);
		
		/*
		 * Handles running of queries
		 */
		btnRunQueries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				/*
				 * Determines which algorithms are selected, 
				 * transforms them into the format recognised by createIndices(),
				 * populates a data structure (ArrayDeque) to allow operations to be run iteratively
				 */
				if (chckbxNewCheckBox.isSelected()) selected.push(chckbxNewCheckBox.getText().toLowerCase());
				if (chckbxNewCheckBox_1.isSelected()) selected.push(chckbxNewCheckBox_1.getText().toLowerCase());
				if (chckbxNewCheckBox_2.isSelected()) selected.push(chckbxNewCheckBox_2.getText().toLowerCase());
				if (chckbxNewCheckBox_3.isSelected()) selected.push(chckbxNewCheckBox_3.getText().toLowerCase());
				if (chckbxNewCheckBox_4.isSelected()) selected.push(chckbxNewCheckBox_4.getText().toLowerCase());
				if (chckbxNewCheckBox_5.isSelected()) selected.push(chckbxNewCheckBox_5.getText().toLowerCase().replace(" ", ""));
				if (chckbxNewCheckBox_6.isSelected()) selected.push(chckbxNewCheckBox_6.getText().toLowerCase().replace("-", ""));
				
				try {
					queries.selectedModels = new String[selected.size() + 1];
					for(int a = 0; a < queries.selectedModels.length -1; a++) {
						queries.selectedModels[a] = selected.pop();
					}
					queries.readQueries(new File(textField_1.getText())); // 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		JLabel lblSelectRankingAlgorithms = new JLabel("Select ranking algorithms to be compared:");
		lblSelectRankingAlgorithms.setBounds(31, 21, 246, 19);
		panel_1.add(lblSelectRankingAlgorithms);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Analyze", null, panel, null);
		//tabbedPane.setEnabledAt(1, false);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 409, 211);
		panel.add(scrollPane);
		
		JTextArea txtara = new JTextArea();
		scrollPane.setViewportView(txtara);
		
		JButton btnIndex = new JButton("Index");
		
		/*
		 * Handles indexing:
		 * Creates empty indices and configures similarity for each,
		 * runs initial index for one of the selected similarity models,
		 * then runs reindex for all subsequent models
		 */
		btnIndex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if (chckbxNewCheckBox.isSelected()) selected.push(chckbxNewCheckBox.getText().toLowerCase());
				if (chckbxNewCheckBox_1.isSelected()) selected.push(chckbxNewCheckBox_1.getText().toLowerCase());
				if (chckbxNewCheckBox_2.isSelected()) selected.push(chckbxNewCheckBox_2.getText().toLowerCase());
				if (chckbxNewCheckBox_3.isSelected()) selected.push(chckbxNewCheckBox_3.getText().toLowerCase());
				if (chckbxNewCheckBox_4.isSelected()) selected.push(chckbxNewCheckBox_4.getText().toLowerCase());
				if (chckbxNewCheckBox_5.isSelected()) selected.push(chckbxNewCheckBox_5.getText().toLowerCase().replace(" ", ""));
				if (chckbxNewCheckBox_6.isSelected()) selected.push(chckbxNewCheckBox_6.getText().toLowerCase().replace("-", ""));
				
				
				try {
					reindex.createIndices();
				} catch (IOException e1) {
					e1.printStackTrace();
				}


				
				try {
					source = selected.pop();
					System.out.println("Indexing: " + source);
					checkDirWithUrls(new File(txtGprojectdataset.getText() + "\\documents"), source);

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				loop = selected.size(); // Set a loop count as the size of 'selected' will change as elements are removed
				for(int p = 0; p < loop; p++) {

					try {
						System.out.println("Reindexing: " + selected.peek());
						reindex.reindexDocuments(source, selected.pop());
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				

			}
		});
		
		btnIndex.setBounds(10, 199, 89, 23);
		panel_1.add(btnIndex);
		
		/*
		 * Handles the running of TREC_EVAL,
		 * reads outputs of TREC_EVAL into 'Analyze' tab separated by algorithm headings
		 */
		btnRunTreceval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Running trec");
				
				if (chckbxNewCheckBox.isSelected()) selected.push(chckbxNewCheckBox.getText().toLowerCase());
				if (chckbxNewCheckBox_1.isSelected()) selected.push(chckbxNewCheckBox_1.getText().toLowerCase());
				if (chckbxNewCheckBox_2.isSelected()) selected.push(chckbxNewCheckBox_2.getText().toLowerCase());
				if (chckbxNewCheckBox_3.isSelected()) selected.push(chckbxNewCheckBox_3.getText().toLowerCase());
				if (chckbxNewCheckBox_4.isSelected()) selected.push(chckbxNewCheckBox_4.getText().toLowerCase());
				if (chckbxNewCheckBox_5.isSelected()) selected.push(chckbxNewCheckBox_5.getText().toLowerCase().replace(" ", ""));
				if (chckbxNewCheckBox_6.isSelected()) selected.push(chckbxNewCheckBox_6.getText().toLowerCase().replace("-", ""));
				

				queries.selectedModels = new String[selected.size() + 1];
				for(int a = 0; a < queries.selectedModels.length -1; a++) {
					queries.selectedModels[a] = selected.pop();
				}
				
				try {
					queries.trec_eval(txtGprojectdataset.getText(), txtGprojectdataset.getText() + "\\qrels", txtGprojectdataset.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				File evalDir = new File(txtGprojectdataset.getText() + "\\evaluations\\");
				
				for (File child : evalDir.listFiles()) {
					try {
						allLines = Files.readAllLines(child.toPath());
						txtara.append(child.getName() + ":\n");
						for(int c = 0; c < allLines.size(); c++) {
							txtara.append(allLines.get(c) + "\n");
						}
						txtara.append("\n");
					} catch (IOException e2) {
						e2.printStackTrace();
					}

				}
			}
		});

	}
}
