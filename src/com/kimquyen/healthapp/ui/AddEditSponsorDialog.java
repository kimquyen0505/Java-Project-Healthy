package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Sponsor;
import com.kimquyen.healthapp.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class AddEditSponsorDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField nameField;
    private JButton saveButton, cancelButton;

    private Sponsor currentSponsor;
    private boolean isEditMode;
    private boolean succeeded = false;

    public AddEditSponsorDialog(Frame parent, String dialogTitle, Sponsor sponsorToEdit) {
        super(parent, dialogTitle, true);
        this.currentSponsor = sponsorToEdit;
        this.isEditMode = (sponsorToEdit != null);

        initComponents();
        if (isEditMode && currentSponsor != null) {
            nameField.setText(currentSponsor.getName());
        }
        pack();
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(400, 150));
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sponsor Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên Nhà Tài Trợ (*):"), gbc);
        nameField = new JTextField(25);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu");
        saveButton.addActionListener(e -> performSave());
        cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performSave() {
        String name = nameField.getText().trim();
        if (ValidationUtil.isNullOrEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Tên nhà tài trợ không được để trống.", "Lỗi Nhập Liệu", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (!isEditMode) {
            currentSponsor = new Sponsor(); 
        }
        currentSponsor.setName(name);

        this.succeeded = true;
        dispose();
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Sponsor getSponsor() {
        return currentSponsor;
    }
}