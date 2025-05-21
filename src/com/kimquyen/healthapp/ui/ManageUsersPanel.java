package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role; // Cần thiết nếu AddEditUserDialog cần đến Role Enum
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.UserService;
import com.kimquyen.healthapp.util.SessionManager; // Để kiểm tra không xóa chính mình
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class ManageUsersPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private UserService userService;

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, backButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public ManageUsersPanel(MainFrame mainFrame, UserService userService) {
        this.mainFrame = mainFrame;
        this.userService = userService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // --- Panel Tiêu đề và Tìm kiếm ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        JLabel titleLabel = new JLabel("Quản Lý Người Dùng", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (Tên hoặc Username):"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- Table Model và JTable ---
        String[] columnNames = {"User ID", "Tên Đầy Đủ", "Username", "Vai trò", "Sponsor ID", "Ngày Tạo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setFillsViewportHeight(true);
        usersTable.getTableHeader().setReorderingAllowed(false);
        usersTable.setFont(new Font("Arial", Font.PLAIN, 13));
        usersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        usersTable.setRowHeight(25);

        sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);

        usersTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && usersTable.getSelectedRow() != -1) {
                    openEditUserDialog();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel chứa các nút điều khiển ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        Dimension buttonDim = new Dimension(150, 35);

        addButton = new JButton("Thêm Mới");
        addButton.setFont(buttonFont);
        addButton.setIcon(UIManager.getIcon("FileChooser.newFolderIcon"));
        addButton.setPreferredSize(buttonDim);
        addButton.addActionListener(e -> openAddEditUserDialog(null, null)); // null để thêm mới

        editButton = new JButton("Sửa");
        editButton.setFont(buttonFont);
        editButton.setIcon(UIManager.getIcon("Actions.Redo")); // Ví dụ icon
        editButton.setPreferredSize(buttonDim);
        editButton.addActionListener(e -> openEditUserDialog());

        deleteButton = new JButton("Xóa");
        deleteButton.setFont(buttonFont);
        deleteButton.setIcon(UIManager.getIcon("FileChooser.deleteIcon"));
        deleteButton.setPreferredSize(buttonDim);
        deleteButton.addActionListener(e -> deleteSelectedUser());

        refreshButton = new JButton("Làm Mới DS");
        refreshButton.setFont(buttonFont);
        refreshButton.setIcon(UIManager.getIcon("FileChooser.refreshAction"));// Sai icon, cần tìm icon đúng hoặc bỏ
        refreshButton.setPreferredSize(buttonDim);
        refreshButton.addActionListener(e -> loadUsersData());

        backButton = new JButton("Quay Lại Dashboard");
        backButton.setFont(buttonFont);
        backButton.setPreferredSize(new Dimension(180, 35));
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD);
            }
        });

        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(refreshButton);
        controlPanel.add(Box.createHorizontalStrut(30)); // Khoảng cách
        controlPanel.add(backButton);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void filterTable() {
        String text = searchField.getText();
        if (sorter == null) return; // Đảm bảo sorter đã được khởi tạo
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Lọc trên cột Tên Đầy Đủ (index 1) hoặc Username (index 2)
                // "(?i)" để không phân biệt chữ hoa/thường
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2));
            } catch (PatternSyntaxException pse) {
                System.err.println("Lỗi cú pháp Regex khi tìm kiếm: " + pse.getMessage());
            }
        }
    }

    public void loadUsersData() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        if (userService == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Service quản lý người dùng chưa sẵn sàng.", "Lỗi Service", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<UserData> usersDataList = userService.getAllUserData();
        if (usersDataList != null) {
            for (UserData userData : usersDataList) {
                Account account = userService.getAccountForUserData(userData.getId()); // Cần phương thức này

                tableModel.addRow(new Object[]{
                        userData.getId(),
                        userData.getName(),
                        (account != null ? account.getUsername() : "N/A"),
                        (account != null && account.getRole() != null ? account.getRole().name() : "N/A"),
                        (userData.getSponsorId() != 0 ? String.valueOf(userData.getSponsorId()) : "N/A"),
                        (userData.getCreatedAt() != null ? userData.getCreatedAt().toLocalDateTime().toLocalDate() : "N/A")
                });
            }
        }
    }

    private void openAddEditUserDialog(UserData userDataToEdit, Account accountToEdit) {
        // Tạo và hiển thị AddEditUserDialog
        AddEditUserDialog userDialog = new AddEditUserDialog(mainFrame, userService, userDataToEdit, accountToEdit);
        userDialog.setVisible(true);

        // Sau khi dialog đóng, kiểm tra xem có thành công không và tải lại dữ liệu
        if (userDialog.isSucceeded()) {
            loadUsersData(); // Tải lại danh sách người dùng
        }
    }

    private void openAddUserDialog() { // Gọi khi nhấn nút "Thêm Mới"
        openAddEditUserDialog(null, null);
    }



    private void openEditUserDialog() {
        int selectedRowView = usersTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowModel = usersTable.convertRowIndexToModel(selectedRowView);

        int userId = 0;
        String username = null;
        try {
            userId = (int) tableModel.getValueAt(selectedRowModel, 0);       // Cột "User ID"
            username = (String) tableModel.getValueAt(selectedRowModel, 2);  // Cột "Username"
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy userId/username từ tableModel trong openEditUserDialog: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu từ dòng đã chọn trong bảng.", "Lỗi Bảng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("ManageUsersPanel - SỬA - Lấy từ bảng -> UserID: " + userId + ", Username: [" + username + "]");

        if (userId == 0 || username == null || username.trim().isEmpty()) {
             JOptionPane.showMessageDialog(this, "Thông tin UserID hoặc Username từ bảng không hợp lệ.", "Lỗi Dữ Liệu Bảng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("ManageUsersPanel - SỬA - Gọi userService.getUserDataById(" + userId + ")");
        UserData userDataToEdit = userService.getUserDataById(userId);
        System.out.println("ManageUsersPanel - SỬA - UserData từ service: " + (userDataToEdit != null ? "TÌM THẤY (Name: " + userDataToEdit.getName() + ")" : "NULL"));

        System.out.println("ManageUsersPanel - SỬA - Gọi userService.getAccountByUsername('" + username + "')");
        Account accountToEdit = userService.getAccountByUsername(username);
        System.out.println("ManageUsersPanel - SỬA - Account từ service: " + (accountToEdit != null ? "TÌM THẤY (Username: " + accountToEdit.getUsername() + ")" : "NULL"));

        if (userDataToEdit != null && accountToEdit != null) {
            openAddEditUserDialog(userDataToEdit, accountToEdit);
        } else {
            StringBuilder errorMessage = new StringBuilder("Không thể tải thông tin người dùng để sửa:");
            if (userDataToEdit == null) {
                errorMessage.append("\n - UserData không tìm thấy cho ID: ").append(userId);
            }
            if (accountToEdit == null) {
                errorMessage.append("\n - Account không tìm thấy cho Username: ").append(username);
            }
            JOptionPane.showMessageDialog(this, errorMessage.toString(), "Lỗi Tải Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            // loadUsersData(); // Có thể không cần load lại nếu chỉ là lỗi tải cho dialog sửa
        }
    }

    private void deleteSelectedUser() {
        int selectedRowView = usersTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowModel = usersTable.convertRowIndexToModel(selectedRowView);
        int userId = (int) tableModel.getValueAt(selectedRowModel, 0);
        String username = (String) tableModel.getValueAt(selectedRowModel, 2);
        String name = (String) tableModel.getValueAt(selectedRowModel, 1);

        // Không cho xóa chính tài khoản admin đang đăng nhập
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentAccount() != null &&
            session.getCurrentAccount().getUsername().equals(username)) {
            JOptionPane.showMessageDialog(this, "Bạn không thể xóa chính tài khoản đang đăng nhập.", "Hành động bị chặn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa người dùng:\nTên: " + name + "\nUsername: " + username + "\n(ID UserData: " + userId + ") ?",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Đảm bảo deleteUser trong UserService xử lý xóa cả Account và UserData
            // và có thể cả các dữ liệu liên quan (ví dụ: hra_responses) hoặc có ràng buộc khóa ngoại phù hợp
            boolean success = userService.deleteUser(userId, username);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa người dùng thành công.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadUsersData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa người dùng thất bại.\nNgười dùng này có thể có các dữ liệu liên quan không thể xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     */
    public void panelVisible() {
        searchField.setText(""); // Xóa nội dung tìm kiếm cũ
        if (sorter != null) {
            sorter.setRowFilter(null); // Bỏ lọc cũ
        }
        loadUsersData(); // Tải/làm mới dữ liệu khi panel được hiển thị
        System.out.println("ManageUsersPanel is now visible.");
    }
}