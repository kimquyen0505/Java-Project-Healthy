// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.HraQuestion; // Model này phải có các hằng số TYPE_...
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.model.AssessmentResult;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;


public class TakeAssessmentPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private AssessmentService assessmentService;
    private MainFrame mainFrame;

    private List<HraQuestion> questions;
    private int currentQuestionIndex = 0;

    private JPanel questionsDisplayPanel;
    private JLabel questionLabel;
    private JPanel answerOptionsPanelContainer;
    private JScrollPane answerScrollPane;
    private JButton nextButton, prevButton, submitButton;

    private Map<Integer, List<JCheckBox>> checkBoxGroups;
    private Map<Integer, ButtonGroup> radioButtonGroups;
    private Map<Integer, JTextComponent> textInputComponents;

    private Map<Integer, String> userResponses;

    public TakeAssessmentPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;
        this.userResponses = new HashMap<>();
        this.checkBoxGroups = new HashMap<>();
        this.radioButtonGroups = new HashMap<>();
        this.textInputComponents = new HashMap<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        questionsDisplayPanel = new JPanel(new BorderLayout(15, 15));
        questionLabel = new JLabel("Đang tải câu hỏi...", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        questionsDisplayPanel.add(questionLabel, BorderLayout.NORTH);

        answerOptionsPanelContainer = new JPanel(new BorderLayout());
        answerScrollPane = new JScrollPane(answerOptionsPanelContainer);
        answerScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        answerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        answerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        questionsDisplayPanel.add(answerScrollPane, BorderLayout.CENTER);

        add(questionsDisplayPanel, BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        Dimension navButtonSize = new Dimension(150, 40);
        Font navButtonFont = new Font("Arial", Font.PLAIN, 14);

        prevButton = new JButton("<< Câu Trước");
        prevButton.setPreferredSize(navButtonSize);
        prevButton.setFont(navButtonFont);
        prevButton.addActionListener(e -> showPreviousQuestion());

        nextButton = new JButton("Câu Tiếp >>");
        nextButton.setPreferredSize(navButtonSize);
        nextButton.setFont(navButtonFont);
        nextButton.addActionListener(e -> showNextQuestion());

        submitButton = new JButton("Nộp Bài");
        submitButton.setPreferredSize(new Dimension(180, 45));
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(new Color(34, 139, 34));
        submitButton.setForeground(Color.WHITE);
        submitButton.setOpaque(true);
        submitButton.setBorderPainted(false);
        submitButton.setVisible(false);
        submitButton.addActionListener(e -> submitAssessment());

        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(submitButton);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    public void loadQuestions() {
        this.questions = assessmentService.getAssessmentQuestions(); // DAO đã nhóm các options
        this.userResponses.clear();
        this.checkBoxGroups.clear();
        this.radioButtonGroups.clear();
        this.textInputComponents.clear();
        this.currentQuestionIndex = 0;

        if (questions != null && !questions.isEmpty()) {
            displayCurrentQuestion();
        } else {
            questionLabel.setText("Không có câu hỏi nào để hiển thị hoặc có lỗi khi tải câu hỏi.");
            if (answerOptionsPanelContainer.getComponentCount() > 0) {
                 answerOptionsPanelContainer.removeAll();
            }
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            submitButton.setVisible(false);
            if (answerOptionsPanelContainer != null) {
                answerOptionsPanelContainer.revalidate();
                answerOptionsPanelContainer.repaint();
            }
        }
        revalidate();
        repaint();
    }

    private void displayCurrentQuestion() {
        if (questions == null || questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        HraQuestion currentQuestion = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><body style='width: 650px; padding: 10px;'>" +
                              "<b>Câu " + (currentQuestionIndex + 1) + "/" + questions.size() + ":</b> " +
                              (currentQuestion.getTitle() != null && !currentQuestion.getTitle().isEmpty() ? "<i>(" + currentQuestion.getTitle() + ")</i><br>" : "") +
                              currentQuestion.getText() + "</body></html>");

        JPanel currentAnswerOptionsPanel = new JPanel();
        currentAnswerOptionsPanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        if (answerOptionsPanelContainer.getComponentCount() > 0) {
            answerOptionsPanelContainer.removeAll();
        }
        answerOptionsPanelContainer.add(currentAnswerOptionsPanel, BorderLayout.NORTH);

        int qId = currentQuestion.getQuestionId();
        checkBoxGroups.remove(qId);
        radioButtonGroups.remove(qId);
        textInputComponents.remove(qId);

        String questionType = currentQuestion.getType(); // Đã được chuẩn hóa từ DAO

        // Không cần try-catch JSONException ở đây nữa vì DAO đã xử lý việc đọc options
        try {
            if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(questionType)) {
                currentAnswerOptionsPanel.setLayout(new BoxLayout(currentAnswerOptionsPanel, BoxLayout.Y_AXIS));
                ButtonGroup group = new ButtonGroup();
                radioButtonGroups.put(qId, group);

                List<HraQuestion.OptionChoice> choices = currentQuestion.getChoices();
                if (choices != null && !choices.isEmpty()) {
                    for (HraQuestion.OptionChoice choice : choices) {
                        JRadioButton radioButton = new JRadioButton("<html><body style='width: 550px;'>" + choice.getOptionLabel() + "</body></html>");
                        radioButton.setActionCommand(choice.getOptionValue());
                        radioButton.setFont(new Font("Arial", Font.PLAIN, 14));
                        if (choice.getOptionValue().equals(userResponses.get(qId))) {
                            radioButton.setSelected(true);
                        }
                        group.add(radioButton);
                        currentAnswerOptionsPanel.add(radioButton);
                        currentAnswerOptionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                    }
                } else {
                     currentAnswerOptionsPanel.add(new JLabel("Không có lựa chọn nào cho câu hỏi này."));
                }
            } else if (HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(questionType)) {
                currentAnswerOptionsPanel.setLayout(new BoxLayout(currentAnswerOptionsPanel, BoxLayout.Y_AXIS));
                List<JCheckBox> checkBoxesForThisQuestion = new ArrayList<>();
                checkBoxGroups.put(qId, checkBoxesForThisQuestion);

                Map<String, Boolean> selectedValuesMap = new HashMap<>();
                String storedResponse = userResponses.get(qId);
                if (storedResponse != null && !storedResponse.isEmpty()) {
                    Arrays.asList(storedResponse.split(",")).forEach(val -> selectedValuesMap.put(val.trim(), true));
                }

                List<HraQuestion.OptionChoice> choices = currentQuestion.getChoices();
                if (choices != null && !choices.isEmpty()) {
                    for (HraQuestion.OptionChoice choice : choices) {
                        JCheckBox checkBox = new JCheckBox("<html><body style='width: 550px;'>" + choice.getOptionLabel() + "</body></html>");
                        checkBox.setActionCommand(choice.getOptionValue());
                        checkBox.setFont(new Font("Arial", Font.PLAIN, 14));
                        if (selectedValuesMap.containsKey(choice.getOptionValue())) {
                            checkBox.setSelected(true);
                        }
                        checkBoxesForThisQuestion.add(checkBox);
                        currentAnswerOptionsPanel.add(checkBox);
                        currentAnswerOptionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                    }
                } else {
                    currentAnswerOptionsPanel.add(new JLabel("Không có lựa chọn nào cho câu hỏi này."));
                }
            } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(questionType)) {
                currentAnswerOptionsPanel.setLayout(new BorderLayout(5,5));
                JLabel answerLabel = new JLabel("Trả lời của bạn:");
                answerLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                currentAnswerOptionsPanel.add(answerLabel, BorderLayout.NORTH);

                JTextArea answerArea = new JTextArea(5, 30);
                answerArea.setLineWrap(true);
                answerArea.setWrapStyleWord(true);
                answerArea.setFont(new Font("Arial", Font.PLAIN, 14));
                answerArea.setText(userResponses.getOrDefault(qId, ""));
                answerArea.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (questions != null && currentQuestionIndex >=0 && currentQuestionIndex < questions.size()) {
                            HraQuestion activeQuestion = questions.get(currentQuestionIndex);
                            if (activeQuestion != null && activeQuestion.getQuestionId() == currentQuestion.getQuestionId() && answerArea != null) {
                                 userResponses.put(currentQuestion.getQuestionId(), answerArea.getText());
                            }
                        }
                    }
                });
                textInputComponents.put(qId, answerArea);
                JScrollPane textAnswerScrollPane = new JScrollPane(answerArea);
                currentAnswerOptionsPanel.add(textAnswerScrollPane, BorderLayout.CENTER);
            } else {
                currentAnswerOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                currentAnswerOptionsPanel.add(new JLabel("Loại câu hỏi không được hỗ trợ: " + (questionType != null ? questionType : "N/A")));
            }
        } catch (Exception e) {
             handleGenericError(currentAnswerOptionsPanel, currentQuestion, e);
        }

        updateNavigationButtons();
        answerOptionsPanelContainer.revalidate();
        answerOptionsPanelContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            if (answerScrollPane != null && answerScrollPane.getViewport() != null) {
                answerScrollPane.getViewport().setViewPosition(new Point(0, 0));
            }
        });
    }

    private void handleGenericError(JPanel panel, HraQuestion question, Exception e) {
        panel.removeAll();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("<html><body style='width: 500px;'>Lỗi không xác định khi hiển thị lựa chọn cho câu hỏi này.</body></html>"));
        System.err.println("Lỗi không xác định khi hiển thị câu hỏi ID " + (question != null ? question.getQuestionId() : "UNKNOWN"));
        e.printStackTrace();
        panel.revalidate();
        panel.repaint();
    }

    private void saveCurrentAnswer() {
        if (questions == null || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) return;
        HraQuestion currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion == null) return;
        int qId = currentQuestion.getQuestionId();

        if (textInputComponents.containsKey(qId)) {
            JTextComponent textInput = textInputComponents.get(qId);
            if (textInput != null) {
                 userResponses.put(qId, textInput.getText());
            }
        } else if (radioButtonGroups.containsKey(qId)) {
            ButtonGroup group = radioButtonGroups.get(qId);
            if (group != null) {
                ButtonModel selectedModel = group.getSelection();
                if (selectedModel != null) {
                    userResponses.put(qId, selectedModel.getActionCommand());
                } else {
                    userResponses.remove(qId);
                }
            }
        } else if (checkBoxGroups.containsKey(qId)) {
            List<JCheckBox> checkBoxes = checkBoxGroups.get(qId);
            if (checkBoxes != null) {
                StringBuilder selectedValues = new StringBuilder();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        if (selectedValues.length() > 0) {
                            selectedValues.append(",");
                        }
                        selectedValues.append(cb.getActionCommand());
                    }
                }
                if (selectedValues.length() > 0) {
                    userResponses.put(qId, selectedValues.toString());
                } else {
                    userResponses.remove(qId);
                }
            }
        }
    }

    private void updateNavigationButtons() {
        boolean hasQuestions = (questions != null && !questions.isEmpty());
        prevButton.setEnabled(hasQuestions && currentQuestionIndex > 0);
        boolean isNotLastQuestion = hasQuestions && (currentQuestionIndex < questions.size() - 1);
        nextButton.setEnabled(isNotLastQuestion);
        boolean isLastQuestion = hasQuestions && (currentQuestionIndex == questions.size() - 1);
        submitButton.setVisible(isLastQuestion);
    }

    private void showNextQuestion() {
        saveCurrentAnswer();
        if (questions != null && currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        }
    }

    private void showPreviousQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        }
    }

    private void submitAssessment() {
        saveCurrentAnswer();
        UserData currentUser = SessionManager.getInstance().getCurrentUserData();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", "Lỗi Session", JOptionPane.ERROR_MESSAGE);
            if (mainFrame != null) mainFrame.performLogout();
            return;
        }

        Map<HraQuestion, String> responsesToSubmit = new HashMap<>();
        if (questions != null) {
            for (HraQuestion q : questions) {
                responsesToSubmit.put(q, userResponses.getOrDefault(q.getQuestionId(), ""));
            }
        }

        if (responsesToSubmit.isEmpty() && (questions != null && !questions.isEmpty())) {
             int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn chưa trả lời câu hỏi nào. Bạn có muốn nộp bài không?",
                    "Xác Nhận Nộp Bài", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.NO_OPTION) {
                return;
            }
        }

        AssessmentResult assessmentResult = assessmentService.submitAssessment(currentUser, responsesToSubmit);

        if (assessmentResult != null) {
            JOptionPane.showMessageDialog(this,
                    "Nộp bài đánh giá thành công!\nĐiểm của bạn: " + assessmentResult.getTotalScore() +
                    "\nMức độ rủi ro: " + assessmentResult.getRiskLevel(),
                    "Hoàn Tất Đánh Giá", JOptionPane.INFORMATION_MESSAGE);

            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.USER_DASHBOARD_CARD);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi nộp bài đánh giá. Vui lòng thử lại hoặc liên hệ quản trị viên.",
                    "Lỗi Nộp Bài", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void panelVisible() {
        loadQuestions();
        System.out.println("TakeAssessmentPanel is now visible and questions are loaded/reset.");
    }
}