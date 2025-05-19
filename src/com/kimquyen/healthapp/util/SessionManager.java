package com.kimquyen.healthapp.util; // Đảm bảo package này đúng với cấu trúc project của bạn

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.UserData; // Cần thiết vì chúng ta sẽ lưu và trả về UserData

public class SessionManager {
    private static SessionManager instance;
    private Account currentAccount;
    private UserData currentUserData; // Biến để lưu thông tin UserData của người đăng nhập

    // Constructor private để đảm bảo Singleton
    private SessionManager() {
        // Không cần làm gì đặc biệt ở đây cho trường hợp này
    }

    // Phương thức static để lấy instance duy nhất, đảm bảo thread-safe khi tạo lần đầu
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Xử lý việc đăng nhập, lưu thông tin Account và UserData vào session.
     *
     * @param account Đối tượng Account của người dùng (không được null).
     * @param userData Đối tượng UserData của người dùng (có thể null nếu logic cho phép,
     *                 nhưng thường thì sẽ có khi người dùng đăng nhập thành công).
     */
    public void login(Account account, UserData userData) {
        if (account == null) {
            System.err.println("LỖI NGHIÊM TRỌNG (SessionManager): Không thể đăng nhập với Account là null.");
            // Trong ứng dụng thực tế, bạn có thể muốn ném một RuntimeException ở đây
            // để dừng luồng bất thường này, vì đây là một trạng thái không hợp lệ.
            // Ví dụ: throw new IllegalArgumentException("Account không được null khi thực hiện login vào session.");
            return; // Dừng thực thi nếu account là null để tránh lỗi ở dưới
        }
        this.currentAccount = account;
        this.currentUserData = userData; // Gán UserData được truyền vào
                                         // Nếu userData là null, thì currentUserData cũng sẽ là null.
                                         // Điều này cần được xử lý ở nơi gọi getCurrentUserData() nếu nó có thể null.
    }

    /**
     * Xử lý việc đăng xuất, xóa thông tin người dùng khỏi session.
     */
    public void logout() {
        this.currentAccount = null;
        this.currentUserData = null; // << SỬA Ở ĐÂY: Đảm bảo currentUserData cũng được reset khi logout
    }

    /**
     * Lấy thông tin Account của người dùng hiện đang đăng nhập.
     *
     * @return Đối tượng Account, hoặc null nếu chưa có ai đăng nhập.
     */
    public Account getCurrentAccount() {
        return currentAccount;
    }

    /**
     * Lấy thông tin UserData của người dùng hiện đang đăng nhập.
     *
     * @return Đối tượng UserData, hoặc null nếu chưa có ai đăng nhập hoặc UserData không được thiết lập.
     */
    public UserData getCurrentUserData() { // << SỬA Ở ĐÂY: Bỏ comment và triển khai phương thức này
        return currentUserData;
    }

    /**
     * Kiểm tra xem có người dùng nào đang đăng nhập vào session hay không.
     *
     * @return true nếu có người dùng đang đăng nhập, false nếu không.
     */
    public boolean isLoggedIn() {
        return currentAccount != null; // Chỉ cần kiểm tra currentAccount là đủ để xác định trạng thái đăng nhập
    }
}