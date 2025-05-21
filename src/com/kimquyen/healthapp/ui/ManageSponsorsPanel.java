package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.Sponsor;
import com.kimquyen.healthapp.service.SponsorService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class ManageSponsorsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private SponsorService sponsorService;

    private JTable sponsorsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, viewUsersButton, refreshButton, backButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public ManageSponsorsPanel(MainFrame mainFrame, SponsorService sponsorService) {
        this.mainFrame = mainFrame;
        this.sponsorService = sponsorService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // --- Top Panel (Title and Search) ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        JLabel titleLabel = new JLabel("Quản Lý Nhà Tài Trợ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (Tên):"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- Table ---
        String[] columnNames = {"ID Nhà Tài Trợ", "Tên Nhà Tài Trợ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sponsorsTable = new JTable(tableModel);
        sponsorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sponsorsTable.setFillsViewportHeight(true);
        sponsorsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        sponsorsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        sponsorsTable.setRowHeight(25);
        sponsorsTable.getColumnModel().getColumn(0).setMaxWidth(150); // Cột ID
        sponsorsTable.getColumnModel().getColumn(0).setMinWidth(100);


        sorter = new TableRowSorter<>(tableModel);
        sponsorsTable.setRowSorter(sorter);

        sponsorsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && sponsorsTable.getSelectedRow() != -1) {
                    openViewSponsoredUsersDialog(); // Double-click để xem người dùng được tài trợ
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(sponsorsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Control Panel (Buttons) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        Dimension regularButtonDim = new Dimension(150, 35);
        Dimension viewUsersButtonDim = new Dimension(260, 35); // Tăng chiều rộng cho nút này
        Dimension backButtonDim = new Dimension(190, 35); // Kích thước cho nút Quay lại


        addButton = new JButton("Thêm Mới");
        addButton.setFont(buttonFont);
        addButton.setPreferredSize(regularButtonDim);
        addButton.setIcon(UIManager.getIcon("FileChooser.newFolderIcon")); // Ví dụ icon
        addButton.addActionListener(e -> openAddEditSponsorDialog(null));
        controlPanel.add(addButton);

        editButton = new JButton("Sửa");
        editButton.setFont(buttonFont);
        editButton.setPreferredSize(regularButtonDim);
        editButton.setIcon(UIManager.getIcon("Actions.Redo")); // Ví dụ icon (tìm icon phù hợp hơn)
        editButton.addActionListener(e -> {
            Sponsor selected = getSelectedSponsorFromTable();
            if (selected != null) {
                openAddEditSponsorDialog(selected);
            }
        });
        controlPanel.add(editButton);

        deleteButton = new JButton("Xóa");
        deleteButton.setFont(buttonFont);
        deleteButton.setPreferredSize(regularButtonDim);
        deleteButton.setIcon(UIManager.getIcon("FileChooser.deleteIcon")); // Ví dụ icon
        deleteButton.addActionListener(e -> deleteSelectedSponsor());
        controlPanel.add(deleteButton);

        viewUsersButton = new JButton("Xem Người Dùng Được Tài Trợ");
        viewUsersButton.setFont(buttonFont);
        viewUsersButton.setPreferredSize(viewUsersButtonDim);
        // viewUsersButton.setIcon(UIManager.getIcon("Tree.openIcon")); // Ví dụ icon
        viewUsersButton.addActionListener(e -> openViewSponsoredUsersDialog());
        controlPanel.add(viewUsersButton);

        refreshButton = new JButton("Làm Mới DS");
        refreshButton.setFont(buttonFont);
        refreshButton.setPreferredSize(regularButtonDim);
        // refreshButton.setIcon(UIManager.getIcon("FileChooser.refreshAction")); // Ví dụ icon (Tìm icon phù hợp)
        refreshButton.addActionListener(e -> loadSponsorsData());
        controlPanel.add(refreshButton);

        // Nút "Quay Lại Dashboard"
        backButton = new JButton("Quay Lại Dashboard");
        backButton.setFont(buttonFont);
        backButton.setPreferredSize(backButtonDim);
        // backButton.setIcon(UIManager.getIcon("FileChooser.homeFolderIcon")); // Ví dụ icon
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD);
            }
        });
        controlPanel.add(Box.createHorizontalStrut(20)); // Tạo khoảng cách với các nút trước đó
        controlPanel.add(backButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void filterTable() {
        String text = searchField.getText();
        if (sorter == null) return;
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Lọc trên cột Tên Nhà Tài Trợ (index 1)
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
            } catch (PatternSyntaxException pse) {
                System.err.println("Lỗi Regex khi tìm kiếm nhà tài trợ: " + pse.getMessage());
            }
        }
    }

    public void loadSponsorsData() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        if (sponsorService == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Service quản lý nhà tài trợ chưa sẵn sàng.", "Lỗi Service", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Sponsor> sponsors = sponsorService.getAllSponsors();
        if (sponsors != null && !sponsors.isEmpty()) { // Thêm kiểm tra isEmpty
            for (Sponsor s : sponsors) {
                tableModel.addRow(new Object[]{s.getId(), s.getName()});
            }
        } else {
            // Tùy chọn: Hiển thị thông báo nếu không có nhà tài trợ nào
            // Ví dụ: tableModel.addRow(new Object[]{"", "Không có nhà tài trợ nào."});
            // Hoặc một JLabel riêng biệt.
             System.out.println("ManageSponsorsPanel: Không có nhà tài trợ nào để hiển thị.");
        }
    }

    private Sponsor getSelectedSponsorFromTable() {
        int selectedRowView = sponsorsTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà tài trợ từ danh sách.", "Chưa Chọn", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        // Chuyển đổi chỉ số dòng từ view sang model (quan trọng khi có sắp xếp/lọc)
        int selectedRowModel = sponsorsTable.convertRowIndexToModel(selectedRowView);
        int sponsorId = (int) tableModel.getValueAt(selectedRowModel, 0); // Cột ID ở index 0

        Sponsor sponsor = sponsorService.getSponsorById(sponsorId);
        if (sponsor == null) {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin chi tiết cho nhà tài trợ ID: " + sponsorId + ".\nNhà tài trợ có thể đã bị xóa.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            loadSponsorsData(); // Làm mới bảng nếu nhà tài trợ không còn tồn tại
        }
        return sponsor;
    }

    private void openAddEditSponsorDialog(Sponsor sponsorToEdit) {
        // Đảm bảo AddEditSponsorDialog đã được tạo và hoạt động đúng
        String dialogTitle = (sponsorToEdit == null) ? "Thêm Nhà Tài Trợ Mới" : "Sửa Thông Tin Nhà Tài Trợ (ID: " + sponsorToEdit.getId() + ")";
        AddEditSponsorDialog dialog = new AddEditSponsorDialog(mainFrame, dialogTitle, sponsorToEdit);
        dialog.setVisible(true);

        if (dialog.isSucceeded()) {
            Sponsor sponsorFromDialog = dialog.getSponsor();
            boolean successOperation;
            String actionType;

            if (sponsorToEdit == null) { // Chế độ Thêm Mới
                actionType = "Thêm";
                successOperation = sponsorService.addSponsor(sponsorFromDialog);
            } else { // Chế độ Sửa
                actionType = "Cập nhật";
                // Đảm bảo sponsorFromDialog có ID đúng (từ sponsorToEdit)
                // AddEditSponsorDialog nên xử lý việc này khi populate hoặc khi lấy sponsor
                if (sponsorFromDialog.getId() == 0 && sponsorToEdit.getId() != 0) {
                    sponsorFromDialog.setId(sponsorToEdit.getId());
                }
                successOperation = sponsorService.updateSponsor(sponsorFromDialog);
            }

            if (successOperation) {
                JOptionPane.showMessageDialog(this, actionType + " nhà tài trợ thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadSponsorsData(); // Làm mới danh sách
            } else {
                JOptionPane.showMessageDialog(this, actionType + " nhà tài trợ thất bại.\nTên có thể đã tồn tại hoặc có lỗi khác.", "Lỗi Thao Tác", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openViewSponsoredUsersDialog() {
        Sponsor selectedSponsor = getSelectedSponsorFromTable();
        if (selectedSponsor != null) {
            // Đảm bảo SponsoredUsersDialog đã được tạo và hoạt động đúng
            SponsoredUsersDialog dialog = new SponsoredUsersDialog(mainFrame, selectedSponsor, sponsorService);
            dialog.setVisible(true);
        }
    }

    private void deleteSelectedSponsor() {
        Sponsor selectedSponsor = getSelectedSponsorFromTable();
        if (selectedSponsor == null) {
            return; // Thông báo đã hiển thị trong getSelectedSponsorFromTable()
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhà tài trợ:\nID: " + selectedSponsor.getId() + "\nTên: " + selectedSponsor.getName() + "?\n\n" +
                "LƯU Ý: Hành động này có thể thất bại nếu nhà tài trợ này đang tài trợ cho người dùng.",
                "Xác Nhận Xóa Nhà Tài Trợ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = sponsorService.deleteSponsor(selectedSponsor.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhà tài trợ thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadSponsorsData(); // Làm mới danh sách
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa nhà tài trợ thất bại.\nNguyên nhân có thể là do nhà tài trợ này đang liên kết với một hoặc nhiều người dùng.\n" +
                        "Vui lòng kiểm tra và bỏ liên kết của người dùng với nhà tài trợ này trước khi xóa.",
                        "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     * Dùng để tải/làm mới dữ liệu cho panel.
     */
    public void panelVisible() {
        searchField.setText(""); // Xóa nội dung tìm kiếm cũ
        if (sorter != null) {
            sorter.setRowFilter(null); // Bỏ lọc cũ
        }
        loadSponsorsData(); // Tải/làm mới dữ liệu
        System.out.println("ManageSponsorsPanel is now visible.");
    }
}