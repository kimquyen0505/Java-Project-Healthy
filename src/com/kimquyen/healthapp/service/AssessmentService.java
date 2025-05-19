// package com.kimquyen.healthapp.service;
package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.dao.HraQuestionDAO;
import com.kimquyen.healthapp.dao.HraResponseDAO;
import com.kimquyen.healthapp.dao.UserAssessmentAttemptDAO;
import com.kimquyen.healthapp.model.AssessmentResult;
import com.kimquyen.healthapp.model.HraQuestion; // Đảm bảo có các hằng số TYPE_ và lớp OptionChoice
import com.kimquyen.healthapp.model.HraResponse;
import com.kimquyen.healthapp.model.UserAssessmentAttempt;
import com.kimquyen.healthapp.model.UserData;

// org.json imports không còn cần thiết ở đây nếu việc parse score đã được xử lý trong DAO
// import org.json.JSONObject;
// import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays; // Cho Arrays.asList khi xử lý multiple choice response
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentService {
    private final HraQuestionDAO hraQuestionDAO;
    private final HraResponseDAO hraResponseDAO;
    private final UserAssessmentAttemptDAO userAssessmentAttemptDAO;

    public AssessmentService(HraQuestionDAO hraQuestionDAO, HraResponseDAO hraResponseDAO, UserAssessmentAttemptDAO userAssessmentAttemptDAO) {
        if (hraQuestionDAO == null || hraResponseDAO == null || userAssessmentAttemptDAO == null) {
            throw new IllegalArgumentException("DAOs không được null khi khởi tạo AssessmentService.");
        }
        this.hraQuestionDAO = hraQuestionDAO;
        this.hraResponseDAO = hraResponseDAO;
        this.userAssessmentAttemptDAO = userAssessmentAttemptDAO;
    }

    /**
     * Lấy danh sách tất cả các câu hỏi cho một bài đánh giá.
     * @return Danh sách HraQuestion.
     */
    public List<HraQuestion> getAssessmentQuestions() {
        List<HraQuestion> questions = hraQuestionDAO.getAllQuestions();
        return (questions != null) ? questions : Collections.emptyList(); // Trả về list rỗng thay vì null
    }

    /**
     * Nộp bài đánh giá, lưu các câu trả lời, tính tổng điểm và lưu kết quả tổng thể.
     *
     * @param user Người dùng thực hiện.
     * @param responsesMap Map các câu hỏi (đối tượng HraQuestion từ UI) và chuỗi câu trả lời của người dùng.
     * @return Đối tượng AssessmentResult chứa kết quả, hoặc null nếu có lỗi nghiêm trọng.
     */
    public AssessmentResult submitAssessment(UserData user, Map<HraQuestion, String> responsesMap) {
        if (user == null || user.getId() == 0 || responsesMap == null) {
            System.err.println("SERVICE (submitAssessment): Thông tin người dùng hoặc câu trả lời không hợp lệ.");
            return null;
        }

        Timestamp submissionTime = new Timestamp(System.currentTimeMillis());
        int totalScore = 0;
        Map<HraQuestion, String> detailedResponsesForReport = new HashMap<>();

        for (Map.Entry<HraQuestion, String> entry : responsesMap.entrySet()) {
            HraQuestion questionFromUI = entry.getKey(); // Đây là đối tượng HraQuestion đã được load từ DAO
            String userResponseString = entry.getValue();

            // Không cần gọi lại hraQuestionDAO.getQuestionById nếu questionFromUI đã đầy đủ thông tin
            // (bao gồm type, choices, generalScore) từ lúc getAssessmentQuestions().
            // Nếu questionFromUI chỉ là đối tượng tạm với ID, thì mới cần lấy lại fullQuestionInfo.
            // Giả sử questionFromUI đã đầy đủ.

            HraResponse hraResponse = new HraResponse();
            hraResponse.setUserId(user.getId());
            hraResponse.setQuestionId(questionFromUI.getQuestionId()); // Sử dụng ID từ questionFromUI
            hraResponse.setResponse(userResponseString);
            hraResponse.setCreatedAt(submissionTime);

            int pointsForThisQuestion = calculatePointsForResponse(questionFromUI, userResponseString);
            // Nếu bạn có trường points_awarded trong HraResponse model/table:
            // hraResponse.setPointsAwarded(pointsForThisQuestion);
            totalScore += pointsForThisQuestion;

            boolean saved = hraResponseDAO.addResponse(hraResponse);
            if (saved) {
                detailedResponsesForReport.put(questionFromUI, userResponseString);
            } else {
                System.err.println("SERVICE (submitAssessment): Không thể lưu câu trả lời cho câu hỏi ID: " + questionFromUI.getQuestionId());
                // Cân nhắc chiến lược xử lý lỗi ở đây, ví dụ ném một custom exception
                // return null; // Hoặc dừng nếu việc lưu một response thất bại là nghiêm trọng
            }
        }

        String riskLevel = determineRiskLevel(totalScore);
        AssessmentResult result = new AssessmentResult(user, submissionTime, detailedResponsesForReport, totalScore, riskLevel);

        // Lưu kết quả tổng thể vào bảng user_assessment_attempts
        UserAssessmentAttempt attemptToSave = new UserAssessmentAttempt();
        attemptToSave.setUserDataId(user.getId());
        attemptToSave.setAssessmentDate(submissionTime);
        attemptToSave.setTotalScore(totalScore);
        attemptToSave.setRiskLevel(riskLevel);

        boolean attemptSaved = userAssessmentAttemptDAO.saveAttempt(attemptToSave);
        if (!attemptSaved) {
            System.err.println("SERVICE WARNING (submitAssessment): Không thể lưu kết quả tổng thể của bài đánh giá cho user ID: " + user.getId());
            // Ghi log, nhưng vẫn trả về AssessmentResult vì các response riêng lẻ có thể đã được lưu.
        } else {
            System.out.println("Kết quả tổng thể bài đánh giá đã được lưu với attempt_id: " + attemptToSave.getAttemptId());
            // Nếu model AssessmentResult có trường attemptId, bạn có thể set nó ở đây:
            // Ví dụ: result.setAttemptId(attemptToSave.getAttemptId()); (Cần thêm trường và setter trong AssessmentResult)
        }
        return result;
    }

    /**
     * Tính điểm cho một câu trả lời dựa trên thông tin câu hỏi và câu trả lời của người dùng.
     * Logic này dựa trên việc HraQuestion đã chứa danh sách các OptionChoice (với điểm)
     * hoặc generalScore đã được điền bởi HraQuestionDAO.
     */
    private int calculatePointsForResponse(HraQuestion question, String userResponseString) {
        if (question == null || userResponseString == null) {
            return 0;
        }
        int points = 0;
        String questionType = question.getType();

        // Sử dụng hằng số từ lớp HraQuestion
        if (HraQuestion.TYPE_SINGLE_CHOICE.equalsIgnoreCase(questionType)) {
            if (question.getChoices() != null) {
                for (HraQuestion.OptionChoice choice : question.getChoices()) {
                    if (choice.getOptionValue().equals(userResponseString)) {
                        points = choice.getOptionScore();
                        break;
                    }
                }
            }
        } else if (HraQuestion.TYPE_MULTIPLE_CHOICE.equalsIgnoreCase(questionType)) {
            if (!userResponseString.trim().isEmpty() && question.getChoices() != null) {
                // Tách các giá trị người dùng đã chọn (được lưu dạng "val1,val2,val3")
                List<String> selectedValues = Arrays.asList(userResponseString.split(","));
                // Loại bỏ khoảng trắng thừa từ mỗi giá trị đã chọn
                List<String> trimmedSelectedValues = new ArrayList<>();
                for(String s : selectedValues){
                    trimmedSelectedValues.add(s.trim());
                }

                for (HraQuestion.OptionChoice choice : question.getChoices()) {
                    if (trimmedSelectedValues.contains(choice.getOptionValue().trim())) {
                        points += choice.getOptionScore(); // Cộng dồn điểm
                    }
                }
            }
        } else if (HraQuestion.TYPE_TEXT_INPUT.equalsIgnoreCase(questionType)) {
            // Đối với TEXT_INPUT, điểm có thể được lấy từ generalScore của câu hỏi
            // nếu người dùng có nhập câu trả lời (không rỗng).
            if (!userResponseString.trim().isEmpty() && question.getGeneralScore() != null) {
                points = question.getGeneralScore();
            }
        }
        return points;
    }

    /**
     * Lấy lịch sử các bài đánh giá của người dùng từ bảng user_assessment_attempts.
     * @param user Người dùng cần xem lịch sử.
     * @return Danh sách các AssessmentResult (chỉ chứa thông tin tóm tắt).
     */
    public List<AssessmentResult> getUserAssessmentHistory(UserData user) {
        if (user == null || user.getId() == 0) {
            return Collections.emptyList();
        }

        List<UserAssessmentAttempt> attempts = userAssessmentAttemptDAO.getAttemptsByUserId(user.getId());
        if (attempts == null || attempts.isEmpty()) {
            return Collections.emptyList();
        }

        List<AssessmentResult> historyResults = new ArrayList<>();
        for (UserAssessmentAttempt attempt : attempts) {
            AssessmentResult summaryResult = new AssessmentResult(
                    user,
                    attempt.getAssessmentDate(),
                    null, // Map<HraQuestion, String> responses -> null cho danh sách lịch sử tóm tắt
                    attempt.getTotalScore(),
                    attempt.getRiskLevel()
            );
            // Nếu model AssessmentResult có trường attemptId:
            // summaryResult.setAttemptId(attempt.getAttemptId());
            historyResults.add(summaryResult);
        }
        // DAO nên trả về danh sách đã được sắp xếp, nếu không thì sắp xếp ở đây
        // historyResults.sort(Comparator.comparing(AssessmentResult::getAssessmentDate).reversed());
        return historyResults;
    }

    private String determineRiskLevel(int totalScore) {
        // Điều chỉnh ngưỡng điểm cho phù hợp với bộ câu hỏi của bạn
        if (totalScore < 10) return "Rất Thấp";
        if (totalScore < 25) return "Thấp";
        if (totalScore < 40) return "Trung Bình";
        if (totalScore < 60) return "Cao";
        return "Rất Cao";
    }

    /**
     * Lấy tất cả các câu trả lời của tất cả người dùng.
     * Có thể được sử dụng bởi ReportService.
     * @return Danh sách tất cả HraResponse.
     */
    public List<HraResponse> getAllResponsesForAllUsers() {
        List<HraResponse> responses = hraResponseDAO.getAllResponses();
        return (responses != null) ? responses : Collections.emptyList();
    }

    // (Tùy chọn) Phương thức để lấy chi tiết một bài đánh giá cụ thể từ lịch sử
    // public AssessmentResult getDetailedAssessmentResultByAttemptId(int attemptId) {
    //     UserAssessmentAttempt attempt = userAssessmentAttemptDAO.getAttemptById(attemptId);
    //     if (attempt == null) return null;

    //     UserData user = userDataDAO.getUserById(attempt.getUserDataId()); // Cần UserDataDAO được inject vào service này
    //     if (user == null) return null;

    //     // Lấy các HraResponse dựa trên user_id VÀ assessment_date (hoặc attempt_id nếu bạn thêm cột đó vào HraResponse)
    //     // Đây là phần cần logic truy vấn DAO phức tạp hơn nếu không có attempt_id trong HraResponse
    //     // List<HraResponse> responsesForThisAttempt = hraResponseDAO.getResponsesByUserAndDate(attempt.getUserDataId(), attempt.getAssessmentDate());
    //     // Map<HraQuestion, String> detailedResponsesMap = new HashMap<>();
    //     // for(HraResponse r : responsesForThisAttempt) {
    //     //     HraQuestion q = hraQuestionDAO.getQuestionById(r.getQuestionId());
    //     //     if(q!=null) detailedResponsesMap.put(q, r.getResponse());
    //     // }
    //     // return new AssessmentResult(user, attempt.getAssessmentDate(), detailedResponsesMap, attempt.getTotalScore(), attempt.getRiskLevel());

    //     System.err.println("getDetailedAssessmentResultByAttemptId cần được hoàn thiện với logic lấy HraResponse chi tiết.");
    //     return new AssessmentResult(user, attempt.getAssessmentDate(), new HashMap<>(), attempt.getTotalScore(), attempt.getRiskLevel()); // Trả về tạm
    // }
}