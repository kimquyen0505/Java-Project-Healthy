// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent; // Cho JTextField
import javax.swing.event.DocumentListener; // Cho JTextField
import java.awt.*;
import java.awt.event.FocusAdapter; // Cho JTextField
import java.awt.event.FocusEvent;   // Cho JTextField
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TakeAssessmentPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Thêm để tránh cảnh báo

    private AssessmentService assessmentService;
    private MainFrame mainFrame; // Tham chiếu đến MainFrame để điều hướng

    private List<HraQuestion> questions;
    private int currentQuestionIndex = 0;
    private JPanel questionsDisplayPanel;
    private JLabel questionLabel;
    private JPanel answerOptionsPanel;
    private JButton nextButton, prevButton, submitButton;

    // Lưu trữ các component input để có thể lấy giá trị từ chúng
    private Map<Integer, Component> answerInputComponents;
    private Map<Integer, ButtonGroup> radioButtonGroups; // Dùng cho câu hỏi dạng RadioButton

    private Map<Integer, String> userResponses; // Lưu trữ câu trả lời: questionId -> response

    public TakeAssessmentPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;
        this.userResponses = new HashMap<>();
        this.answerInputComponents = new HashMap<>();
        this.radioButtonGroups = new HashMap<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
        // loadQuestions(); // Gọi trong panelVisible()
    }

    private void initComponents() {
        questionsDisplayPanel = new JPanel(new BorderLayout(10, 10));
        questionLabel = new JLabel("Đang tải câu hỏi...", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionsDisplayPanel.add(questionLabel, BorderLayout.NORTH);

        answerOptionsPanel = new JPanel(); // Layout sẽ được đặt khi hiển thị câu hỏi
        // Cho phép answerOptionsPanel cuộn nếu có nhiều lựa chọn
        JScrollPane answerScrollPane = new JScrollPane(answerOptionsPanel);
        answerScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Bỏ viền của scrollpane
        questionsDisplayPanel.add(answerScrollPane, BorderLayout.CENTER);

        add(questionsDisplayPanel, BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        prevButton = new JButton("<< Câu Trước");
        nextButton = new JButton("Câu Tiếp >>");
        submitButton = new JButton("Nộp Bài Đánh Giá");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(0, 153, 51)); // Màu xanh lá
        submitButton.setForeground(Color.WHITE);
        submitButton.setVisible(false);

        prevButton.addActionListener(e -> showPreviousQuestion());
        nextButton.addActionListener(e -> showNextQuestion());
        submitButton.addActionListener(e -> submitAssessment());

        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(submitButton);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    public void loadQuestions() {
        this.questions = assessmentService.getAssessmentQuestions();
        this.userResponses.clear();
        this.answerInputComponents.clear();
        this.radioButtonGroups.clear();
        this.currentQuestionIndex = 0;

        if (questions != null && !questions.isEmpty()) {
            displayCurrentQuestion();
        } else {
            questionLabel.setText("Không có câu hỏi nào để hiển thị.");
            answerOptionsPanel.removeAll();
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            submitButton.setVisible(false);
            answerOptionsPanel.revalidate();
            answerOptionsPanel.repaint();
        }
        revalidate();
        repaint();
    }

    private void displayCurrentQuestion() {
        if (questions == null || questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        HraQuestion currentQuestion = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><body style='width: 600px; padding: 10px;'>" +
                              "<b>Câu " + (currentQuestionIndex + 1) + "/" + questions.size() + ":</b> " +
                              currentQuestion.getText() + "</body></html>");

        answerOptionsPanel.removeAll();
        answerInputComponents.remove(currentQuestion.getQuestionId()); // Xóa component cũ nếu có
        radioButtonGroups.remove(currentQuestion.getQuestionId());   // Xóa group cũ

        // Dựa vào currentQuestion.getType() và currentQuestion.getOptions() để tạo UI
        // Ví dụ: SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT_INPUT
        // Bạn cần định nghĩa các hằng số cho question.getType()

        // GIẢ SỬ BẠN CÓ CÁC HẰNG SỐ NHƯ SAU TRONG HraQuestion hoặc một nơi khác
        // public static final String TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
        // public static final String TYPE_TEXT_INPUT = "TEXT_INPUT";

        // ---- VÍ DỤ TRIỂN KHAI CHO SINGLE_CHOICE VÀ TEXT_INPUT ----
        if ("SINGLE_CHOICE".equalsIgnoreCase(currentQuestion.getType())) { // Thay "SINGLE_CHOICE" bằng hằng số
            answerOptionsPanel.setLayout(new BoxLayout(answerOptionsPanel, BoxLayout.Y_AXIS));
            ButtonGroup group = new ButtonGroup();
            radioButtonGroups.put(currentQuestion.getQuestionId(), group);

            String[] options = (currentQuestion.getOptions() != null) ? currentQuestion.getOptions().split("\\|") : new String[0]; // Giả sử options phân tách bằng |
            for (String opt : options) {
                String trimmedOpt = opt.trim();
                if (trimmedOpt.isEmpty()) continue;

                JRadioButton radioButton = new JRadioButton("<html><body style='width: 450px;'>" + trimmedOpt + "</body></html>");
                radioButton.setActionCommand(trimmedOpt);
                radioButton.setFont(new Font("Arial", Font.PLAIN, 14));

                if (trimmedOpt.equals(userResponses.get(currentQuestion.getQuestionId()))) {
                    radioButton.setSelected(true);
                }
                group.add(radioButton);
                answerOptionsPanel.add(radioButton);
                // Không cần addActionListener ở đây nữa vì saveCurrentAnswer sẽ được gọi khi chuyển câu
            }
        } else { // Mặc định hoặc nếu là TYPE_TEXT_INPUT
            answerOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            answerOptionsPanel.add(new JLabel("Trả lời: "));
            JTextField answerField = new JTextField(40);
            answerField.setFont(new Font("Arial", Font.PLAIN, 14));
            answerField.setText(userResponses.getOrDefault(currentQuestion.getQuestionId(), ""));
            answerInputComponents.put(currentQuestion.getQuestionId(), answerField);
            answerOptionsPanel.add(answerField);
        }
        // ---- KẾT THÚC VÍ DỤ ----

        updateNavigationButtons();
        answerOptionsPanel.revalidate();
        answerOptionsPanel.repaint();
    }

    private void saveCurrentAnswer() {
        if (questions == null || questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        HraQuestion currentQuestion = questions.get(currentQuestionIndex);
        int qId = currentQuestion.getQuestionId();

        Component inputComponent = answerInputComponents.get(qId);
        if (inputComponent instanceof JTextField) {
            userResponses.put(qId, ((JTextField) inputComponent).getText());
        } else if (radioButtonGroups.containsKey(qId)) {
            ButtonGroup group = radioButtonGroups.get(qId);
            ButtonModel selectedModel = group.getSelection();
            if (selectedModel != null) {
                userResponses.put(qId, selectedModel.getActionCommand());
            } else {
                userResponses.remove(qId); // Hoặc lưu giá trị rỗng nếu không chọn
            }
        }
        // Thêm logic cho các loại input khác (JCheckBox, JTextArea, ...) nếu có
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setEnabled(currentQuestionIndex < questions.size() - 1);
        submitButton.setVisible(currentQuestionIndex == questions.size() - 1 && questions.size() > 0);
        // Chỉ hiện nút Nộp bài khi ở câu cuối và có ít nhất 1 câu hỏi
    }

    private void showNextQuestion() {
        saveCurrentAnswer(); // Lưu câu trả lời của câu hiện tại
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        }
    }

    private void showPreviousQuestion() {
        saveCurrentAnswer(); // Lưu câu trả lời của câu hiện tại
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        }
    }

    private void submitAssessment() {
        saveCurrentAnswer(); // Lưu câu trả lời của câu hỏi cuối cùng

        UserData currentUser = SessionManager.getInstance().getCurrentUserData();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            if (mainFrame != null) mainFrame.performLogout(); // Gọi logout của MainFrame
            return;
        }

        // Kiểm tra xem tất cả câu hỏi đã được trả lời chưa (TÙY CHỌN)
        // for (HraQuestion q : questions) {
        //     if (!userResponses.containsKey(q.getQuestionId()) || userResponses.get(q.getQuestionId()).trim().isEmpty()){
        //         JOptionPane.showMessageDialog(this, "Vui lòng trả lời tất cả các câu hỏi.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
        //         // Có thể chuyển đến câu hỏi chưa trả lời đầu tiên
        //         // int firstUnanswered = findFirstUnansweredQuestion();
        //         // if (firstUnanswered != -1) { currentQuestionIndex = firstUnanswered; displayCurrentQuestion(); }
        //         return;
        //     }
        // }


        Map<HraQuestion, String> responsesToSubmit = new HashMap<>();
        for (HraQuestion q : questions) {
            // Chỉ submit những câu hỏi có trong danh sách câu hỏi của bài đánh giá này
            // và người dùng đã có câu trả lời cho nó (dù là rỗng nếu là JTextField)
            responsesToSubmit.put(q, userResponses.getOrDefault(q.getQuestionId(), ""));
        }

        boolean success = assessmentService.submitAssessment(currentUser, responsesToSubmit);
        if (success) {
            JOptionPane.showMessageDialog(this, "Nộp bài đánh giá thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            if (mainFrame != null) {
                // Chuyển về UserDashboard hoặc một panel kết quả
                // Giả sử UserDashboard là nơi người dùng thường quay lại
                mainFrame.showPanel(MainFrame.USER_DASHBOARD_CARD);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi nộp bài. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Phương thức này sẽ được gọi bởi MainFrame khi panel này được hiển thị.
     */
    public void panelVisible() {
        loadQuestions(); // Luôn tải lại câu hỏi khi panel được hiển thị, reset trạng thái
        System.out.println("TakeAssessmentPanel is now visible.");
    }
}