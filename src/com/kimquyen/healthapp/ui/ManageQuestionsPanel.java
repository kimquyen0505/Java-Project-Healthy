// com/kimquyen/healthapp/ui/ManageQuestionsPanel.java
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.service.QuestionService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class ManageQuestionsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private QuestionService questionService;

    private JTable questionsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, viewDetailsButton, refreshButton, backButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public ManageQuestionsPanel(MainFrame mainFrame, QuestionService questionService) {
        this.mainFrame = mainFrame;
        this.questionService = questionService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        JLabel titleLabel = new JLabel("Quản Lý Câu Hỏi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (Tiêu đề, Nội dung):"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table Model và JTable
        String[] columnNames = {"ID", "Tiêu đề", "Nội dung (tóm tắt)", "Loại"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionsTable = new JTable(tableModel);
        questionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ... (các cài đặt khác cho table như trong ManageUsersPanel)
        questionsTable.getColumnModel().getColumn(0).setMaxWidth(50); // Cột ID
        questionsTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Cột nội dung


        sorter = new TableRowSorter<>(tableModel);
        questionsTable.setRowSorter(sorter);

        questionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && questionsTable.getSelectedRow() != -1) {
                    openViewDetailsDialog();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(questionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // ... (Khởi tạo các nút addButton, editButton, deleteButton, refreshButton, backButton như ManageUsersPanel)

        addButton = new JButton("Thêm Mới");
        addButton.addActionListener(e -> openAddEditQuestionDialog(null));
        controlPanel.add(addButton);
        
        editButton = new JButton("Sửa");
        editButton.addActionListener(e -> {
            HraQuestion selected = getSelectedQuestionFromTable();
            if (selected != null) {
                openAddEditQuestionDialog(selected);
            }
        });
        controlPanel.add(editButton);

        deleteButton = new JButton("Xóa");
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        controlPanel.add(deleteButton);
        
        viewDetailsButton = new JButton("Xem Chi Tiết");
        viewDetailsButton.addActionListener(e -> openViewDetailsDialog());
        controlPanel.add(viewDetailsButton);

        refreshButton = new JButton("Làm Mới");
        refreshButton.addActionListener(e -> loadQuestionsData());
        controlPanel.add(refreshButton);

        backButton = new JButton("Quay Lại Dashboard");
        backButton.addActionListener(e -> mainFrame.showPanel(MainFrame.ADMIN_DASHBOARD_CARD));
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
                // Lọc trên cột Tiêu đề (index 1) hoặc Nội dung (index 2)
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
            } catch (PatternSyntaxException pse) {
                System.err.println("Lỗi Regex khi tìm kiếm câu hỏi: " + pse.getMessage());
            }
        }
    }

    public void loadQuestionsData() {
        tableModel.setRowCount(0);
        if (questionService == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Service quản lý câu hỏi chưa sẵn sàng.", "Lỗi Service", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<HraQuestion> questions = questionService.getAllQuestions();
        if (questions != null) {
            for (HraQuestion q : questions) {
                String summary = q.getText();
                if (summary != null && summary.length() > 100) {
                    summary = summary.substring(0, 97) + "...";
                }
                tableModel.addRow(new Object[]{
                        q.getQuestionId(),
                        q.getTitle(),
                        summary,
                        q.getType()
                });
            }
        }
    }

    private HraQuestion getSelectedQuestionFromTable() {
        int selectedRowView = questionsTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int selectedRowModel = questionsTable.convertRowIndexToModel(selectedRowView);
        int questionId = (int) tableModel.getValueAt(selectedRowModel, 0); // Giả sử cột ID là cột 0

        // Cần lấy đầy đủ thông tin câu hỏi (bao gồm cả choices) từ service
        HraQuestion fullQuestion = questionService.getQuestionById(questionId);
        if (fullQuestion == null) {
             JOptionPane.showMessageDialog(this, "Không thể tải chi tiết câu hỏi ID: " + questionId, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return fullQuestion;
    }

    private void openAddEditQuestionDialog(HraQuestion questionToEdit) {
        String title = (questionToEdit == null) ? "Thêm Câu Hỏi Mới" : "Sửa Câu Hỏi (ID: " + questionToEdit.getQuestionId() + ")";
        AddEditQuestionDialog dialog = new AddEditQuestionDialog(mainFrame, title, questionToEdit);
        dialog.setVisible(true);

        if (dialog.isSucceeded()) {
            HraQuestion questionFromDialog = dialog.getQuestion();
            boolean success;
            if (questionToEdit == null) { // Chế độ thêm mới
                // Vì question_id không phải AI trong hra_qna_scores, cần cách tạo ID mới
                // Hoặc DAO sẽ xử lý việc này. Tạm thời để service/DAO quyết định ID.
                success = questionService.addQuestion(questionFromDialog);
                if (success) JOptionPane.showMessageDialog(this, "Thêm câu hỏi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } else { // Chế độ sửa
                // questionFromDialog đã có questionId từ questionToEdit
                success = questionService.updateQuestion(questionFromDialog);
                 if (success) JOptionPane.showMessageDialog(this, "Cập nhật câu hỏi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
            if(success) {
                loadQuestionsData();
            } else {
                 JOptionPane.showMessageDialog(this, (questionToEdit == null ? "Thêm" : "Cập nhật") + " câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openViewDetailsDialog() {
        HraQuestion selectedQuestion = getSelectedQuestionFromTable();
        if (selectedQuestion != null) {
            QuestionDetailsDialog detailsDialog = new QuestionDetailsDialog(mainFrame, selectedQuestion);
            detailsDialog.setVisible(true);
        }
    }

    private void deleteSelectedQuestion() {
        int selectedRowView = questionsTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowModel = questionsTable.convertRowIndexToModel(selectedRowView);
        int questionId = (int) tableModel.getValueAt(selectedRowModel, 0);
        String questionText = (String) tableModel.getValueAt(selectedRowModel, 2);


        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa câu hỏi:\nID: " + questionId + "\nNội dung: " + questionText + "\nTất cả các lựa chọn và điểm liên quan sẽ bị xóa.",
                "Xác Nhận Xóa Câu Hỏi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = questionService.deleteQuestion(questionId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa câu hỏi thành công.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadQuestionsData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa câu hỏi thất bại. Có thể có lỗi hoặc ràng buộc dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void panelVisible() {
        searchField.setText("");
        if (sorter != null) sorter.setRowFilter(null);
        loadQuestionsData();
        System.out.println("ManageQuestionsPanel is now visible.");
    }
}