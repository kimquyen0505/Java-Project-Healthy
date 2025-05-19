package com.kimquyen.healthapp.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class HraQuestion {
    // Hằng số cho các loại câu hỏi
    public static final String TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
    public static final String TYPE_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String TYPE_TEXT_INPUT = "TEXT_INPUT";

    private int questionId;
    private String type;    // Sẽ sử dụng các hằng số ở trên
    private String title;
    private String text;

    // Danh sách các lựa chọn cho câu hỏi trắc nghiệm
    private List<OptionChoice> choices;

    // Điểm chung cho câu hỏi TEXT_INPUT (hoặc điểm mặc định nếu không có lựa chọn nào được chọn)
    private Integer generalScore;

    // Lớp nội bộ để đại diện cho một lựa chọn và điểm của nó
    public static class OptionChoice {
        private String optionValue; // Giá trị để lưu vào DB (có thể là label đã chuẩn hóa)
        private String optionLabel; // Nhãn hiển thị cho người dùng
        private int optionScore;    // Điểm cho lựa chọn này

        public OptionChoice(String optionValue, String optionLabel, int optionScore) {
            this.optionValue = optionValue;
            this.optionLabel = optionLabel;
            this.optionScore = optionScore;
        }

        public String getOptionValue() { return optionValue; }
        public String getOptionLabel() { return optionLabel; }
        public int getOptionScore() { return optionScore; }

        @Override
        public String toString() { // Quan trọng cho JComboBox nếu bạn dùng nó với OptionChoice
            return optionLabel;
        }
    }

    public HraQuestion() {
        this.choices = new ArrayList<>(); // Luôn khởi tạo danh sách choices
    }

    // Constructor chính được sử dụng bởi DAO khi tạo đối tượng câu hỏi ban đầu
    public HraQuestion(int questionId, String type, String title, String text) {
        this(); // Gọi constructor mặc định để khởi tạo choices
        this.questionId = questionId;
        this.type = type;
        this.title = title;
        this.text = text;
    }

    // Getters and Setters
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<OptionChoice> getChoices() { return choices; }
    public void setChoices(List<OptionChoice> choices) { this.choices = choices; }
    public void addChoice(OptionChoice choice) {
        if (this.choices == null) { // Đảm bảo an toàn dù đã khởi tạo trong constructor
            this.choices = new ArrayList<>();
        }
        this.choices.add(choice);
    }

    public Integer getGeneralScore() { return generalScore; }
    public void setGeneralScore(Integer generalScore) { this.generalScore = generalScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HraQuestion that = (HraQuestion) o;
        return questionId == that.questionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
    }

    @Override
    public String toString() {
        return "HraQuestion{" +
                "questionId=" + questionId +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", text_preview='" + (text != null && text.length() > 20 ? text.substring(0, 20) + "..." : text) + '\'' +
                ", choices_count=" + (choices != null ? choices.size() : 0) +
                ", generalScore=" + generalScore +
                '}';
    }
}