// package com.kimquyen.healthapp.ui;
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
    private JButton viewDetailsButton; // Tùy chọn: Nút xem chi tiết một bài đánh giá
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

        // Table Model và JTable
        String[] columnNames = {"Ngày Đánh Giá", "Tổng Điểm", "Mức Độ Rủi Ro"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp
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
        // Cho phép sắp xếp theo cột ngày tháng (cần custom Comparator nếu SimpleDateFormat không tự sắp xếp đúng)
        // sorter.setComparator(0, new Comparator<String>() { ... });


        // (Tùy chọn) Xử lý double-click để xem chi tiết
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Căn phải

        viewDetailsButton = new JButton("Xem Chi Tiết");
        viewDetailsButton.setFont(new Font("Arial", Font.PLAIN, 14));
        viewDetailsButton.addActionListener(e -> viewSelectedAssessmentDetails());
        // buttonPanel.add(viewDetailsButton); // Thêm nút này nếu bạn muốn có chức năng xem chi tiết

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
                        // Nếu AssessmentResult có attemptId, bạn có thể lưu nó ẩn để dùng khi xem chi tiết
                        // Ví dụ: tableModel.addColumn("AttemptID_Hidden"); và set giá trị, rồi ẩn cột đó.
                });
            }
        } else {
            // Có thể thêm một dòng thông báo vào bảng nếu không có lịch sử
            // Hoặc hiển thị một JLabel riêng.
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

        // Để lấy chi tiết, bạn cần một định danh cho lần làm bài đó.
        // Lý tưởng nhất là UserAssessmentAttempt.attemptId đã được lưu đâu đó khi loadHistoryData
        // Hoặc bạn có thể dựa vào ngày tháng + userId để truy vấn lại các HraResponse
        // (cách này phức tạp hơn và có thể không chính xác nếu user làm nhiều bài trong 1 giây)

        // GIẢ SỬ: bạn có cách lấy attemptId từ dòng đã chọn
        // (Ví dụ, bạn lấy ngày tháng từ bảng, rồi tìm lại attempt trong danh sách `history` ban đầu
        // hoặc tốt hơn là UserAssessmentAttemptDAO có getAttemptsByUserId trả về List<UserAssessmentAttempt>
        // và bạn lưu trữ danh sách này để lấy attemptId khi cần)

        // ---- BẮT ĐẦU PHẦN CẦN HOÀN THIỆN LOGIC LẤY ATTEMPT ID ----
        // Ví dụ đơn giản (CẦN CẢI THIỆN): Lấy lại từ danh sách AssessmentResult đã load
        // Điều này yêu cầu AssessmentResult phải có một trường để lưu attemptId từ UserAssessmentAttempt
        // Hoặc bạn lưu trực tiếp List<UserAssessmentAttempt> trong panel này.
        // Giả sử AssessmentResult có getAttemptId() (bạn cần thêm vào model và service)
        /*
        UserData currentUser = SessionManager.getInstance().getCurrentUserData();
        List<AssessmentResult> currentHistory = assessmentService.getUserAssessmentHistory(currentUser); // Tải lại hoặc dùng list đã lưu
        if (selectedRowModel < currentHistory.size()) {
            AssessmentResult selectedResult = currentHistory.get(selectedRowModel);
            int attemptId = selectedResult.getAttemptId(); // GIẢ SỬ CÓ METHOD NÀY

            if (attemptId > 0 && mainFrame != null) {
                // mainFrame.showDetailedAssessmentResultPanel(attemptId); // MainFrame sẽ gọi service để lấy chi tiết
                JOptionPane.showMessageDialog(this, "Chức năng xem chi tiết cho Attempt ID: " + attemptId + " đang phát triển.");
            } else {
                 JOptionPane.showMessageDialog(this, "Không thể xác định bài đánh giá để xem chi tiết.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        */
        // ---- KẾT THÚC PHẦN CẦN HOÀN THIỆN ----
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