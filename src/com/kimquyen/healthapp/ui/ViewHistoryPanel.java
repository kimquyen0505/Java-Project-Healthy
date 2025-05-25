package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.AssessmentResult;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat; // Để định dạng ngày tháng
import java.util.List;

public class ViewHistoryPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private AssessmentService assessmentService;

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton backButton;
    private JButton viewDetailsButton; 
    private TableRowSorter<DefaultTableModel> sorter;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public ViewHistoryPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Lịch Sử Các Bài Đánh Giá", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Ngày Đánh Giá", "Tổng Điểm", "Mức Độ Rủi Ro"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.setRowHeight(25);

        sorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(sorter);

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedAssessmentDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        viewDetailsButton = new JButton("Xem Chi Tiết");
        viewDetailsButton.setFont(new Font("Arial", Font.PLAIN, 14));
        viewDetailsButton.addActionListener(e -> viewSelectedAssessmentDetails());

        backButton = new JButton("Quay Lại Dashboard");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                // Quay lại dashboard phù hợp (User hoặc Admin nếu admin cũng có thể xem lịch sử user khác)
                 if (SessionManager.getInstance().isLoggedIn() && SessionManager.getInstance().getCurrentAccount().getRole() == com.kimquyen.healthapp.model.Role.ADMIN) {
                    mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD);
                 } else {
                    mainFrame.showPanel(MainFrame.USER_DASHBOARD_CARD);
                 }
            }
        });
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadHistoryData() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || session.getCurrentUserData() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem lịch sử.", "Chưa Đăng Nhập", JOptionPane.WARNING_MESSAGE);
            if (mainFrame != null) mainFrame.performLogout();
            return;
        }

        UserData currentUser = session.getCurrentUserData();
        List<AssessmentResult> history = assessmentService.getUserAssessmentHistory(currentUser);

        if (history != null && !history.isEmpty()) {
            for (AssessmentResult result : history) {
                tableModel.addRow(new Object[]{
                        (result.getAssessmentDate() != null ? dateFormat.format(result.getAssessmentDate()) : "N/A"),
                        result.getTotalScore(),
                        (result.getRiskLevel() != null ? result.getRiskLevel() : "N/A")

                });
            }
        } else {

            System.out.println("Không có lịch sử đánh giá nào cho người dùng: " + currentUser.getName());
        }
    }

    private void viewSelectedAssessmentDetails() {
        int selectedRowView = historyTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài đánh giá từ danh sách để xem chi tiết.", "Chưa Chọn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int selectedRowModel = historyTable.convertRowIndexToModel(selectedRowView);

        JOptionPane.showMessageDialog(this, "Chức năng 'Xem Chi Tiết' đang được phát triển.\nBạn cần một cách để lấy ID của lần làm bài đã chọn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     */
    public void panelVisible() {
        loadHistoryData();
        System.out.println("ViewHistoryPanel is now visible.");
    }
}