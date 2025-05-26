// com/kimquyen/healthapp/util/UIConstants.java
package com.kimquyen.healthapp.util;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
// import javax.swing.ImageIcon; // Bỏ comment nếu bạn dùng hằng số ImageIcon

public class UIConstants {

    // Dark Theme Colors
    public static final Color COLOR_BACKGROUND_DARK = new Color(45, 45, 45);         // Nền chính của ứng dụng
    public static final Color COLOR_SIDEBAR_DARK = new Color(35, 35, 35);          // Nền cho sidebar
    public static final Color COLOR_COMPONENT_BACKGROUND_DARK = new Color(60, 63, 65); // Nền cho text field, table, cards
    public static final Color COLOR_TEXT_LIGHT = new Color(220, 220, 220);             // Màu chữ chính (sáng)
    public static final Color COLOR_TEXT_SECONDARY_LIGHT = new Color(180, 180, 180);  // Màu chữ phụ (sáng, ít nổi bật hơn)
    public static final Color COLOR_ACCENT_BLUE = new Color(52, 152, 219);         // Màu nhấn (ví dụ: nút active, highlight)
    public static final Color COLOR_BORDER_DARK = new Color(80, 80, 80);               // Màu viền cho các component

    // Fonts (Cân nhắc dùng font hiện đại hơn nếu có thể)
    // "Segoe UI" là font đẹp trên Windows, "San Francisco" trên macOS, "Cantarell" hoặc "Roboto" trên Linux.
    // "Arial" hoặc "SansSerif" là lựa chọn an toàn đa nền tảng.
    public static final Font FONT_PRIMARY_REGULAR = new Font("Arial", Font.PLAIN, 14);
    public static final Font FONT_PRIMARY_BOLD = new Font("Arial", Font.BOLD, 14);
    public static final Font FONT_TITLE_LARGE = new Font("Arial", Font.BOLD, 24); // Ví dụ: Tiêu đề chính của app trên sidebar
    public static final Font FONT_TITLE_MEDIUM = new Font("Arial", Font.BOLD, 18); // Ví dụ: Tiêu đề các panel, card
    public static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    public static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);      // Font cho các nút chính
    public static final Font FONT_SIDEBAR_BUTTON = new Font("Arial", Font.PLAIN, 15); // Font cho nút trên sidebar
    public static final Font FONT_LABEL_FORM = new Font("Arial", Font.PLAIN, 13);


    // Dimensions
    public static final Dimension DIM_BUTTON_SIDEBAR = new Dimension(200, 45);     // Kích thước nút trên sidebar
    public static final Dimension DIM_BUTTON_STANDARD = new Dimension(130, 35);    // Kích thước nút tiêu chuẩn
    public static final Dimension DIM_TEXT_FIELD_STANDARD = new Dimension(250, 30); // Kích thước cho JTextField


    // Insets/Padding
    public static final Insets INSETS_PANEL_PADDING = new Insets(20, 20, 20, 20);  // Padding chung cho panel
    public static final Insets INSETS_FORM_FIELD = new Insets(8, 8, 8, 8);       // Khoảng cách giữa các trường trong form
    public static final Insets INSETS_SIDEBAR_BUTTON = new Insets(10, 15, 10, 15); // // Padding cho nút trên sidebar
    public static final Insets INSETS_TEXT_COMPONENT = new Insets(3, 6, 3, 6);    // Padding bên trong JTextField, JTextArea
	public static final Color COLOR_TEXT_DARK = null;

    // Icons (Ví dụ - bạn cần có file icon thực tế)
    // Đặt icon trong thư mục src/resources/icons (ví dụ)
    // public static final ImageIcon ICON_DASHBOARD = new ImageIcon(UIConstants.class.getResource("/icons/dashboard_light.png"));
    // public static final ImageIcon ICON_USERS = new ImageIcon(UIConstants.class.getResource("/icons/users_light.png"));
    // public static final ImageIcon ICON_QUESTIONS = new ImageIcon(UIConstants.class.getResource("/icons/questions_light.png"));
    // public static final ImageIcon ICON_SPONSORS = new ImageIcon(UIConstants.class.getResource("/icons/sponsors_light.png"));
    // public static final ImageIcon ICON_REPORTS = new ImageIcon(UIConstants.class.getResource("/icons/reports_light.png"));
    // public static final ImageIcon ICON_ASSESSMENT = new ImageIcon(UIConstants.class.getResource("/icons/assessment_light.png"));
    // public static final ImageIcon ICON_HISTORY = new ImageIcon(UIConstants.class.getResource("/icons/history_light.png"));
    // public static final ImageIcon ICON_LOGOUT = new ImageIcon(UIConstants.class.getResource("/icons/logout_light.png"));

    // --- CÓ THỂ GIỮ LẠI CÁC HẰNG SỐ CŨ NẾU BẠN MUỐN DỄ DÀNG CHUYỂN ĐỔI GIỮA LIGHT/DARK THEME ---
    // Hoặc tạo một lớp UIConstantsLight.java riêng
    /*
    // Light Theme Colors (Ví dụ từ code gốc của bạn)
    public static final Color COLOR_BACKGROUND_LIGHT = new Color(240, 240, 240);
    public static final Color COLOR_PRIMARY_BUTTON_LIGHT = new Color(50, 150, 250);
    public static final Color COLOR_PRIMARY_BUTTON_TEXT_LIGHT = Color.WHITE;
    public static final Color COLOR_SUCCESS_LIGHT = new Color(34, 139, 34);

    // Light Theme Fonts (Ví dụ từ code gốc của bạn)
    public static final Font FONT_TITLE_LIGHT = new Font("Arial", Font.BOLD, 20);
    // ... các font khác cho light theme
    */
}