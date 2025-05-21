// com/kimquyen/healthapp/ui/AddEditQuestionDialog.java
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.HraQuestion.OptionChoice;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AddEditQuestionDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField titleField;
    private JTextArea questionTextField;
    private JComboBox<String> typeComboBox;
    private JTextField generalScoreField; // For TEXT_INPUT
    private JPanel optionsPanel; // For SINGLE_CHOICE, MULTIPLE_CHOICE
    private JTable optionsTable;
    private DefaultTableModel optionsTableModel;

    private JButton addOptionButton, removeOptionButton, saveButton, cancelButton;

    private HraQuestion currentQuestion;
    private boolean isEditMode;
    private boolean succeeded = false;

    private final String[] QUESTION_TYPES = {HraQuestion.TYPE_SINGLE_CHOICE, HraQuestion.TYPE_MULTIPLE_CHOICE, HraQuestion.TYPE_TEXT_INPUT};

    public AddEditQuestionDialog(Frame parent, String dialogTitle, HraQuestion questionToEdit) {
        super(parent, dialogTitle, true);
        this.currentQuestion = questionToEdit;
        this.isEditMode = (questionToEdit != null);

        initComponents();
        if (isEditMode) {
            populateFields();
        } else {
            // Nếu thêm mới, questionId = 0 hoặc không set
            // currentQuestion có thể được khởi tạo rỗng để lấy type, title,...
            this.currentQuestion = new HraQuestion(); // Để lưu dữ liệu mới
        }
        updateFieldsVisibilityBasedOnType();
        pack();
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(600, 500));
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tiêu đề (tùy chọn):"), gbc);
        titleField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleField, gbc);
        gbc.gridwidth = 1;


        // Question Text
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nội dung câu hỏi (*):"), gbc);
        questionTextField = new JTextArea(5, 30);
        questionTextField.setLineWrap(true);
        questionTextField.setWrapStyleWord(true);
        JScrollPane textScrollPane = new JScrollPane(questionTextField);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.5;
        formPanel.add(textScrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; gbc.gridwidth = 1;

        // Type
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Loại câu hỏi (*):"), gbc);
        typeComboBox = new JComboBox<>(QUESTION_TYPES);
        typeComboBox.addActionListener(e -> updateFieldsVisibilityBasedOnType());
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(typeComboBox, gbc);

        // General Score (for TEXT_INPUT)
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Điểm chung (nếu TEXT_INPUT):"), gbc);
        generalScoreField = new JTextField(5);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(generalScoreField, gbc);

        // Options Panel (for SINGLE_CHOICE, MULTIPLE_CHOICE)
        optionsPanel = new JPanel(new BorderLayout(5,5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Các lựa chọn và điểm"));

        optionsTableModel = new DefaultTableModel(new Object[]{"Lựa chọn (*)", "Điểm (*)"}, 0){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Integer.class; // Cột điểm là số
                return String.class;
            }
        };
        optionsTable = new JTable(optionsTableModel);
        // Cho phép sửa trực tiếp trên bảng
        optionsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);


        JPanel optionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addOptionButton = new JButton("Thêm lựa chọn");
        addOptionButton.addActionListener(e -> optionsTableModel.addRow(new Object[]{"", 0}));
        removeOptionButton = new JButton("Xóa lựa chọn");
        removeOptionButton.addActionListener(e -> {
            int selectedRow = optionsTable.getSelectedRow();
            if (selectedRow != -1) {
                if (optionsTable.isEditing()) {
                    optionsTable.getCellEditor().stopCellEditing();
                }
                optionsTableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một lựa chọn để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            }
        });
        optionButtonsPanel.add(addOptionButton);
        optionButtonsPanel.add(removeOptionButton);

        optionsPanel.add(new JScrollPane(optionsTable), BorderLayout.CENTER);
        optionsPanel.add(optionButtonsPanel, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        formPanel.add(optionsPanel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> performSave());
        cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateFieldsVisibilityBasedOnType() {
        String selectedType = (String) typeComboBox.getSelectedItem();
        if (HraQuestion.TYPE_TEXT_INPUT.equals(selectedType)) {
            generalScoreField.setVisible(true);
            optionsPanel.setVisible(false);
            generalScoreField.getParent().getParent().revalidate(); // Revalidate formPanel
            generalScoreField.getParent().getParent().repaint();
        } else { // SINGLE_CHOICE or MULTIPLE_CHOICE
            generalScoreField.setVisible(false);
            optionsPanel.setVisible(true);
            optionsPanel.getParent().getParent().revalidate(); // Revalidate formPanel
            optionsPanel.getParent().getParent().repaint();
        }
        pack(); // Có thể cần pack lại dialog
    }

    private void populateFields() {
        if (currentQuestion == null) return;
        titleField.setText(currentQuestion.getTitle());
        questionTextField.setText(currentQuestion.getText());
        typeComboBox.setSelectedItem(currentQuestion.getType());
        typeComboBox.setEnabled(!isEditMode); // Không cho sửa Type khi edit, vì cấu trúc lưu điểm khác nhau

        if (HraQuestion.TYPE_TEXT_INPUT.equals(currentQuestion.getType())) {
            generalScoreField.setText(currentQuestion.getGeneralScore() != null ? String.valueOf(currentQuestion.getGeneralScore()) : "");
        } else {
            optionsTableModel.setRowCount(0); // Xóa các dòng cũ
            if (currentQuestion.getChoices() != null) {
                for (OptionChoice choice : currentQuestion.getChoices()) {
                    optionsTableModel.addRow(new Object[]{choice.getOptionLabel(), choice.getOptionScore()});
                }
            }
        }
        updateFieldsVisibilityBasedOnType(); // Gọi lại để đảm bảo visibility đúng
    }

    private void performSave() {
        String title = titleField.getText().trim();
        String qText = questionTextField.getText().trim();
        String qType = (String) typeComboBox.getSelectedItem();

        if (qText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung câu hỏi không được để trống.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            questionTextField.requestFocus();
            return;
        }

        if (currentQuestion == null) currentQuestion = new HraQuestion(); // Khởi tạo nếu chưa có (chế độ thêm mới)
        
        if(isEditMode && currentQuestion.getQuestionId() == 0){
             JOptionPane.showMessageDialog(this, "Lỗi: Không có ID câu hỏi để sửa.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }


        currentQuestion.setTitle(title);
        currentQuestion.setText(qText);
        currentQuestion.setType(qType);
        currentQuestion.getChoices().clear(); // Xóa choice cũ trước khi thêm mới từ bảng

        if (HraQuestion.TYPE_TEXT_INPUT.equals(qType)) {
            String scoreStr = generalScoreField.getText().trim();
            if (scoreStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Điểm chung cho câu hỏi TEXT_INPUT không được để trống.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                generalScoreField.requestFocus();
                return;
            }
            try {
                currentQuestion.setGeneralScore(Integer.parseInt(scoreStr));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm chung phải là một số nguyên.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                generalScoreField.requestFocus();
                return;
            }
        } else { // SINGLE_CHOICE or MULTIPLE_CHOICE
            currentQuestion.setGeneralScore(null); // Đảm bảo generalScore là null cho loại này
            if (optionsTable.isEditing()) { // Hoàn tất việc sửa trên bảng nếu có
                optionsTable.getCellEditor().stopCellEditing();
            }
            if (optionsTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Phải có ít nhất một lựa chọn cho câu hỏi trắc nghiệm.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (int i = 0; i < optionsTableModel.getRowCount(); i++) {
                String optionLabel = (String) optionsTableModel.getValueAt(i, 0);
                Object scoreObj = optionsTableModel.getValueAt(i, 1); // Có thể là String hoặc Integer tùy JTable

                if (optionLabel == null || optionLabel.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nội dung lựa chọn ở dòng " + (i + 1) + " không được để trống.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    optionsTable.requestFocus();
                    optionsTable.changeSelection(i,0, false, false);
                    return;
                }
                if (scoreObj == null || scoreObj.toString().trim().isEmpty()) {
                     JOptionPane.showMessageDialog(this, "Điểm cho lựa chọn ở dòng " + (i + 1) + " không được để trống.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    optionsTable.requestFocus();
                    optionsTable.changeSelection(i,1, false, false);
                    return;
                }
                
                int optionScore;
                try {
                    optionScore = Integer.parseInt(scoreObj.toString());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Điểm cho lựa chọn '" + optionLabel + "' phải là một số nguyên.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    optionsTable.requestFocus();
                    optionsTable.changeSelection(i,1, false, false);
                    return;
                }
                // Tạo optionValue tự động (đơn giản hóa)
                String optionValue = optionLabel.trim().toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
                if(optionValue.isEmpty()) optionValue = "opt_" + System.currentTimeMillis() % 1000 + "_" + i;

                currentQuestion.addChoice(new OptionChoice(optionValue, optionLabel.trim(), optionScore));
            }
        }
        this.succeeded = true;
        dispose();
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public HraQuestion getQuestion() {
        return currentQuestion;
    }
}