package com.stltrimis.pdfmerger;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.lang.reflect.Field;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class PdfMergerUI extends JFrame{

    private DefaultListModel<File> listModel = new DefaultListModel<>();
    private JList<File> fileList = new JList<>(listModel);
    private JLabel statusLabel = new JLabel("Ready");

    public PdfMergerUI() {
        super("PDF Merger");

        setLayout(new BorderLayout());
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //Sidebar panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(150, getHeight()));

        //Buttons
        JButton addButton = new JButton("Add PDFs");
        JButton removeButton = new JButton("Remove Selected");
        JButton upButton = new JButton("Move Up");
        JButton downButton = new JButton("Move Down");
        JButton mergeButton = new JButton("Merge");

        //Add buttons to sidebar
        sidebar.add(addButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(removeButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(upButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(downButton);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(mergeButton);

        //Push status bar to bottom
        sidebar.add(Box.createVerticalGlue());

        //Status bar inside sidebar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        sidebar.add(statusPanel);

        //File list
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileList);
        fileList.setCellRenderer(new PdfListCellRenderer());

        //Add components to frame
        add(sidebar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        //Button actions
        addButton.addActionListener(e -> addPdfFiles());
        removeButton.addActionListener(e -> removeSelected());
        upButton.addActionListener(e -> moveUp());
        downButton.addActionListener(e -> moveDown());
        mergeButton.addActionListener(e -> mergePdfs());

        //Drag & Drop support
        fileList.setDragEnabled(true);
        fileList.setDropMode(DropMode.INSERT);
        fileList.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> droppedFiles =
                            (java.util.List<File>) support.getTransferable()
                                    .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File f : droppedFiles) {
                        if (f.getName().toLowerCase().endsWith(".pdf")) {
                            listModel.addElement(f);
                        }
                    }

                    statusLabel.setText("Added " + droppedFiles.size() + " files via drag & drop");
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    private void addPdfFiles(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            for(File f: chooser.getSelectedFiles()){
                listModel.addElement(f);
            }
            statusLabel.setText("Added " + chooser.getSelectedFiles().length + " files");
        }
    }

    private void mergePdfs(){
        if(listModel.isEmpty()){
            statusLabel.setText("No files to merge");
            return;
        }

        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setSelectedFile(new File("merged.pdf"));

        if(saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File output = saveChooser.getSelectedFile();

            try {
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDestinationFileName(output.getAbsolutePath());

                for(int i=0; i<listModel.size(); i++){
                    merger.addSource(listModel.get(i));
                }
                merger.mergeDocuments(null);
                statusLabel.setText("Merged successfully");
            } catch (Exception ex){
                ex.printStackTrace();
                statusLabel.setText("Error during merge");

            }
        }


    }

    private void moveUp(){
        int index = fileList.getSelectedIndex();
        if(index >0){
            File file = listModel.get(index);
            listModel.remove(index);
            listModel.add(index - 1, file);
            fileList.setSelectedIndex(index - 1);
        }
    }

    private void moveDown(){
        int index = fileList.getSelectedIndex();
        if(index >= 0 && index < listModel.size() -1){
            File file = listModel.get(index);
            listModel.remove(index);
            listModel.add(index + 1, file);
            fileList.setSelectedIndex(index + 1);
        }
    }

    private void removeSelected(){
        int index = fileList.getSelectedIndex();
        if(index != -1){
            listModel.remove(index);
            statusLabel.setText("Removed file");
        }else {
            statusLabel.setText("No file selected");
        }
    }

    private class PdfListCellRenderer extends DefaultListCellRenderer{
        private final Icon pdfIcon = UIManager.getIcon("FileView.fileIcon");

        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus){

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if(value instanceof File file){
                label.setText(file.getName()); //only the name
                label.setIcon(pdfIcon); //PDF icon
            }
            return label;
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> new PdfMergerUI().setVisible(true));

    }
}