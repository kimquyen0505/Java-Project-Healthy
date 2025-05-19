package com.kimquyen.healthapp.model;

//import java.util.List; // Nếu bạn quyết định parse 'options' thành List<String>

public class HraQuestion {
 private int questionId;
 private String type; // Ví dụ: "MULTIPLE_CHOICE", "SINGLE_CHOICE", "TEXT_INPUT"
 private String title;
 private String text; // Nội dung câu hỏi
 private String options; // Có thể là JSON string hoặc chuỗi phân tách bởi dấu phẩy
                         // Ví dụ: "Option A,Option B,Option C"
                         // Hoặc: "[{\"value\":\"A\", \"text\":\"Option A\"}, ...]"
 private int score; // Hoặc String score nếu phức tạp (ví dụ: điểm cho từng option)

 // Constructors
 public HraQuestion() {
 }

 public HraQuestion(int questionId, String type, String title, String text, String options, int score) {
     this.questionId = questionId;
     this.type = type;
     this.title = title;
     this.text = text;
     this.options = options;
     this.score = score;
 }

 // Getters and Setters
 public int getQuestionId() {
     return questionId;
 }

 public void setQuestionId(int questionId) {
     this.questionId = questionId;
 }

 public String getType() {
     return type;
 }

 public void setType(String type) {
     this.type = type;
 }

 public String getTitle() {
     return title;
 }

 public void setTitle(String title) {
     this.title = title;
 }

 public String getText() {
     return text;
 }

 public void setText(String text) {
     this.text = text;
 }

 public String getOptions() {
     return options;
 }

 public void setOptions(String options) {
     this.options = options;
 }

 // Nếu bạn muốn parse 'options' thành List<String> một cách tiện lợi:
 // public List<String> getOptionList() {
 //     if (options == null || options.isEmpty()) {
 //         return new ArrayList<>();
 //     }
 //     // Giả sử options là chuỗi phân tách bởi dấu phẩy
 //     return Arrays.asList(options.split("\\s*,\\s*"));
 // }

 public int getScore() {
     return score;
 }

 public void setScore(int score) {
     this.score = score;
 }

 // toString
 @Override
 public String toString() {
     return "HraQuestion{" +
            "questionId=" + questionId +
            ", type='" + type + '\'' +
            ", title='" + title + '\'' +
            ", text='" + text + '\'' +
            ", options='" + options + '\'' +
            ", score=" + score +
            '}';
 }
}
