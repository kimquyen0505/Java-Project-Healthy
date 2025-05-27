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
        JLabel titleLabel = new JLabel("Question Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search (Title, Content):"));
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
        String[] columnNames = {"ID", "Title", "Content", "Type"};
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

        addButton = new JButton("ADD");
        addButton.addActionListener(e -> openAddEditQuestionDialog(null));
        controlPanel.add(addButton);
        
        editButton = new JButton("EDIT");
        editButton.addActionListener(e -> {
            HraQuestion selected = getSelectedQuestionFromTable();
            if (selected != null) {
                openAddEditQuestionDialog(selected);
            }
        });
        controlPanel.add(editButton);

        deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        controlPanel.add(deleteButton);
        
        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> openViewDetailsDialog());
        controlPanel.add(viewDetailsButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadQuestionsData());
        controlPanel.add(refreshButton);

        backButton = new JButton("Return Dashboard");
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
                System.err.println("Regex error when searching for questions: " + pse.getMessage());
            }
        }
    }

    public void loadQuestionsData() {
        tableModel.setRowCount(0);
        if (questionService == null) {
            JOptionPane.showMessageDialog(this, "Error: Question management service is not ready.", "Service Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Please select a question.", "Not Select", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int selectedRowModel = questionsTable.convertRowIndexToModel(selectedRowView);
        int questionId = (int) tableModel.getValueAt(selectedRowModel, 0); // Giả sử cột ID là cột 0

        // Cần lấy đầy đủ thông tin câu hỏi (bao gồm cả choices) từ service
        HraQuestion fullQuestion = questionService.getQuestionById(questionId);
        if (fullQuestion == null) {
             JOptionPane.showMessageDialog(this, "Unable to load question details for ID: " + questionId, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return fullQuestion;
    }

    private void openAddEditQuestionDialog(HraQuestion questionToEdit) {
        String title = (questionToEdit == null) ? "Add New Question" : "Edit Quetion (ID: " + questionToEdit.getQuestionId() + ")";
        AddEditQuestionDialog dialog = new AddEditQuestionDialog(mainFrame, title, questionToEdit);
        dialog.setVisible(true);

        if (dialog.isSucceeded()) {
            HraQuestion questionFromDialog = dialog.getQuestion();
            boolean success;
            if (questionToEdit == null) { // Chế độ thêm mới
                // Vì question_id không phải AI trong hra_qna_scores, cần cách tạo ID mới
                // Hoặc DAO sẽ xử lý việc này. Tạm thời để service/DAO quyết định ID.
                success = questionService.addQuestion(questionFromDialog);
                if (success) JOptionPane.showMessageDialog(this, "Question added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } else { // Chế độ sửa
                // questionFromDialog đã có questionId từ questionToEdit
                success = questionService.updateQuestion(questionFromDialog);
                 if (success) JOptionPane.showMessageDialog(this, "Question updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            if(success) {
                loadQuestionsData();
            } else {
                 JOptionPane.showMessageDialog(this, (questionToEdit == null ? "Add" : "Update") + " question error.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Please select a question to delete.", "Not select", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowModel = questionsTable.convertRowIndexToModel(selectedRowView);
        int questionId = (int) tableModel.getValueAt(selectedRowModel, 0);
        String questionText = (String) tableModel.getValueAt(selectedRowModel, 2);


        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the question:\nID: " + questionId + "\nContent: " + questionText + "\nAll related options and scores will be deleted.",
                "Confirm Delete Question", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = questionService.deleteQuestion(questionId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Question deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadQuestionsData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete the question. There may be an error or data constraints.", "Error", JOptionPane.ERROR_MESSAGE);
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