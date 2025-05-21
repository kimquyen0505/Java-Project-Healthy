// com/kimquyen/healthapp/ui/SponsoredUsersDialog.java
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Sponsor;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.SponsorService; // Cần service để lấy danh sách người dùng

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class SponsoredUsersDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private SponsorService sponsorService;
    private Sponsor sponsor;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");


    public SponsoredUsersDialog(Frame parent, Sponsor sponsor, SponsorService sponsorService) {
        super(parent, "Người Dùng Được Tài Trợ Bởi: " + sponsor.getName(), true);
        this.sponsor = sponsor;
        this.sponsorService = sponsorService;

        initComponents();
        loadSponsoredUsers();
        pack();
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table Model và JTable
        String[] columnNames = {"ID Người Dùng", "Tên Người Dùng", "Ngày Tạo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setFillsViewportHeight(true);
        // ... (các cài đặt table khác nếu cần)

        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Close Button
        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSponsoredUsers() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        if (sponsorService == null || sponsor == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không thể tải danh sách người dùng.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<UserData> users = sponsorService.getUsersSponsoredBy(sponsor.getId());
        if (users != null && !users.isEmpty()) {
            for (UserData user : users) {
                tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        (user.getCreatedAt() != null ? dateFormat.format(user.getCreatedAt()) : "N/A")
                });
            }
        } else {
            // Có thể thêm một dòng thông báo "Không có người dùng nào" vào bảng
            System.out.println("Không có người dùng nào được tài trợ bởi: " + sponsor.getName());
        }
    }
}