package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.HraQuestion.OptionChoice;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QuestionDetailsDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public QuestionDetailsDialog(Frame parent, HraQuestion question) {
        super(parent, "Chi Tiết Câu Hỏi (ID: " + question.getQuestionId() + ")", true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        detailsPanel.add(createReadOnlyField("ID Câu hỏi:", String.valueOf(question.getQuestionId())));
        detailsPanel.add(createReadOnlyField("Tiêu đề:", question.getTitle()));
        detailsPanel.add(new JLabel("Nội dung câu hỏi:"));
        JTextArea qText = new JTextArea(question.getText());
        qText.setEditable(false);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        qText.setBackground(UIManager.getColor("Label.background"));
        detailsPanel.add(new JScrollPane(qText));
        detailsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        detailsPanel.add(createReadOnlyField("Loại:", question.getType()));

        if (HraQuestion.TYPE_TEXT_INPUT.equals(question.getType())) {
            detailsPanel.add(createReadOnlyField("Điểm chung:", question.getGeneralScore() != null ? String.valueOf(question.getGeneralScore()) : "N/A"));
        } else {
            detailsPanel.add(Box.createRigidArea(new Dimension(0,10)));
            JLabel optionsLabel = new JLabel("Các lựa chọn và điểm:");
            optionsLabel.setFont(optionsLabel.getFont().deriveFont(Font.BOLD));
            detailsPanel.add(optionsLabel);

            String[] columnNames = {"Lựa chọn", "Điểm"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                 @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable optionsTable = new JTable(model);
            if (question.getChoices() != null) {
                for (OptionChoice choice : question.getChoices()) {
                    model.addRow(new Object[]{choice.getOptionLabel(), choice.getOptionScore()});
                }
            }
            JScrollPane tableScrollPane = new JScrollPane(optionsTable);
            tableScrollPane.setPreferredSize(new Dimension(400, 150));
            detailsPanel.add(tableScrollPane);
        }

        add(detailsPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(parent);
    }

    private JPanel createReadOnlyField(String labelText, String valueText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));
        JTextField field = new JTextField(valueText != null ? valueText : "N/A", 25);
        field.setEditable(false);
        field.setBorder(null);
        field.setBackground(UIManager.getColor("Label.background"));
        panel.add(field);
        return panel;
    }
}