package com.kimquyen.healthapp.service;


import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
//UserDataDAO có thể không cần trực tiếp ở đây nếu UserData được truyền vào
//import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.model.AssessmentResult;
import com.kimquyen.healthapp.model.HraQuestion;
import com.kimquyen.healthapp.model.HraResponse;
import com.kimquyen.healthapp.model.UserData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssessmentService {
 private final HraQuestionDAO hraQuestionDAO;
 private final HraResponseDAO hraResponseDAO;
 // private final UserDataDAO userDataDAO; // Nếu cần lấy thông tin UserData từ ID

 public AssessmentService(HraQuestionDAO hraQuestionDAO, HraResponseDAO hraResponseDAO /*, UserDataDAO userDataDAO*/) {
     this.hraQuestionDAO = hraQuestionDAO;
     this.hraResponseDAO = hraResponseDAO;
     // this.userDataDAO = userDataDAO;
 }

 public List<HraQuestion> getAssessmentQuestions() {
     // Có thể có logic để chọn một bộ câu hỏi cụ thể, hoặc lấy tất cả
     return hraQuestionDAO.getAllQuestions();
 }

 public boolean submitAssessment(UserData user, Map<HraQuestion, String> responses) {
     if (user == null || user.getId() == 0 || responses == null || responses.isEmpty()) {
         System.err.println("Thông tin người dùng hoặc câu trả lời không hợp lệ.");
         return false;
     }

     Timestamp submissionTime = new Timestamp(System.currentTimeMillis());
     boolean allResponsesSaved = true;

     for (Map.Entry<HraQuestion, String> entry : responses.entrySet()) {
         HraQuestion question = entry.getKey();
         String answer = entry.getValue();

         HraResponse hraResponse = new HraResponse();
         hraResponse.setUserId(user.getId());
         hraResponse.setQuestionId(question.getQuestionId());
         hraResponse.setResponse(answer);
         hraResponse.setCreatedAt(submissionTime);

         if (!hraResponseDAO.addResponse(hraResponse)) {
             System.err.println("Không thể lưu câu trả lời cho câu hỏi ID: " + question.getQuestionId() + " của người dùng ID: " + user.getId());
             allResponsesSaved = false;
             // Trong ứng dụng thực tế, nếu một response không lưu được, bạn có thể muốn rollback tất cả
             // các response đã lưu trong assessment này (cần transaction).
         }
     }
     return allResponsesSaved;
 }

 public AssessmentResult calculateScoreAndGetResult(UserData user, List<HraResponse> userResponses) {
     if (user == null || userResponses == null || userResponses.isEmpty()) {
         System.err.println("Không đủ thông tin để tính toán kết quả.");
         return null;
     }

     int totalScore = 0;
     // Map để dễ dàng truy cập câu hỏi gốc từ response
     Map<Integer, HraQuestion> questionMap = new java.util.HashMap<>();
     List<HraQuestion> allQuestions = hraQuestionDAO.getAllQuestions(); // Lấy tất cả câu hỏi để tham chiếu điểm
     for(HraQuestion q : allQuestions) {
         questionMap.put(q.getQuestionId(), q);
     }

     Map<HraQuestion, String> detailedResponses = new java.util.HashMap<>();
     Timestamp assessmentDate = null;

     for (HraResponse response : userResponses) {
         if (assessmentDate == null) { // Lấy thời gian của response đầu tiên làm thời gian đánh giá
             assessmentDate = response.getCreatedAt();
         }
         HraQuestion question = questionMap.get(response.getQuestionId());
         if (question != null) {
             detailedResponses.put(question, response.getResponse());
             // Logic tính điểm:
             // Giả sử trường 'score' trong HraQuestion là điểm cho câu hỏi đó nếu được trả lời.
             // Hoặc bạn có thể có logic phức tạp hơn dựa trên `response.getResponse()` và `question.getOptions()`.
             // Ví dụ đơn giản: nếu câu hỏi có điểm, và người dùng trả lời, thì cộng điểm đó.
             // Cần làm rõ cách tính điểm từ `question.getScore()` và `response.getResponse()`.
             // Ví dụ, nếu `question.getScore()` là điểm của câu hỏi, và chỉ cần trả lời là có điểm:
              if (response.getResponse() != null && !response.getResponse().trim().isEmpty()) {
                 totalScore += question.getScore(); // Đây là ví dụ, cần logic tính điểm cụ thể
              }
         }
     }

     String riskLevel = determineRiskLevel(totalScore); // Cần một phương thức để xác định mức độ rủi ro

     return new AssessmentResult(user, assessmentDate, detailedResponses, totalScore, riskLevel);
 }

 // Phương thức trợ giúp để xác định mức độ rủi ro (cần định nghĩa ngưỡng)
 private String determineRiskLevel(int totalScore) {
     // Ví dụ về ngưỡng, bạn cần điều chỉnh
     if (totalScore < 50) {
         return "Low";
     } else if (totalScore < 100) {
         return "Medium";
     } else {
         return "High";
     }
 }

 // Có thể thêm phương thức lấy lịch sử đánh giá của người dùng
 public List<AssessmentResult> getUserAssessmentHistory(UserData user) {
     if (user == null || user.getId() == 0) return new ArrayList<>();

     List<HraResponse> allUserResponses = hraResponseDAO.getResponsesByUserId(user.getId());
     if (allUserResponses.isEmpty()) return new ArrayList<>();

     // Phân nhóm các responses theo từng lần làm bài (dựa vào created_at gần nhau)
     // Đây là một logic phức tạp hơn, cần xác định một "phiên làm bài"
     // Ví dụ đơn giản: coi mỗi ngày là một lần làm bài khác nhau hoặc nếu thời gian giữa các response quá xa.
     // Hoặc đơn giản hơn là giả định mỗi khi gọi calculateScoreAndGetResult là cho 1 bộ responses của 1 lần làm.
     // Sườn này sẽ giả định bạn có một List<HraResponse> cho một lần làm bài cụ thể khi gọi calculateScoreAndGetResult.

     // Nếu muốn hiển thị lịch sử, bạn cần lấy các nhóm HraResponse theo từng lần làm bài
     // và gọi calculateScoreAndGetResult cho mỗi nhóm.
     // Ví dụ (rất đơn giản hóa, cần logic nhóm tốt hơn):
     List<AssessmentResult> history = new ArrayList<>();
     if (!allUserResponses.isEmpty()) {
          // Giả sử tất cả response của user là cho một lần đánh giá (cần cải thiện logic này)
         AssessmentResult singleResult = calculateScoreAndGetResult(user, allUserResponses);
         if(singleResult != null) history.add(singleResult);
     }
     return history;
 }
}
