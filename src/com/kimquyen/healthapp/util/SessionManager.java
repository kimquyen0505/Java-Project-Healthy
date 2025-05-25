package com.kimquyen.healthapp.util; 

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.UserData; // Cần thiết vì chúng ta sẽ lưu và trả về UserData

public class SessionManager {
    private static SessionManager instance;
    private Account currentAccount;
    private UserData currentUserData; 

    private SessionManager() {
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
           
            return;
        }
        this.currentAccount = account;
        this.currentUserData = userData; 
    }

    /**
     * Xử lý việc đăng xuất, xóa thông tin người dùng khỏi session.
     */
    public void logout() {
        this.currentAccount = null;
        this.currentUserData = null; 
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
    public UserData getCurrentUserData() { 
        return currentUserData;
    }

    /**
     * Kiểm tra xem có người dùng nào đang đăng nhập vào session hay không.
     *
     * @return true nếu có người dùng đang đăng nhập, false nếu không.
     */
    public boolean isLoggedIn() {
        return currentAccount != null; 
    }
}