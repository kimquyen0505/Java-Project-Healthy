// package com.kimquyen.healthapp.ui;
package com.kimquyen.healthapp.ui;

import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.AssessmentService;
import com.kimquyen.healthapp.util.SessionManager;
import com.kimquyen.healthapp.*; // Thêm import này nếu chưa có (cần để dùng MainFrame.TAKE_ASSESSMENT_CARD, ...)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// ActionEvent và ActionListener không cần thiết nếu chỉ dùng lambda

public class UserDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel welcomeLabel;
    private JButton takeAssessmentButton;
    private JButton viewHistoryButton;
    // private JPanel chartPanelContainer;

    private MainFrame mainFrame; // Tham chiếu đến MainFrame
    private AssessmentService assessmentService; // Service này có thể cần cho loadChart

    public UserDashboardPanel(MainFrame mainFrame, AssessmentService assessmentService) {
        this.mainFrame = mainFrame;
        this.assessmentService = assessmentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        // loadUserData(); // Sẽ được gọi trong panelVisible()
    }

    private void initComponents() {
        welcomeLabel = new JLabel("Chào mừng!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        takeAssessmentButton = new JButton("Làm bài đánh giá mới");
        takeAssessmentButton.setFont(new Font("Arial", Font.PLAIN, 16));
        // takeAssessmentButton.setIcon(UIManager.getIcon("FileView.fileIcon")); // Icon có thể gây lỗi nếu không tìm thấy
        takeAssessmentButton.setPreferredSize(new Dimension(250, 50));
        takeAssessmentButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.TAKE_ASSESSMENT_CARD); // Sử dụng hằng số từ MainFrame
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(takeAssessmentButton, gbc);

        viewHistoryButton = new JButton("Xem lịch sử đánh giá");
        viewHistoryButton.setFont(new Font("Arial", Font.PLAIN, 16));
        // viewHistoryButton.setIcon(UIManager.getIcon("FileView.historyIcon"));
        viewHistoryButton.setPreferredSize(new Dimension(250, 50));
        // *** BỎ ActionListener hiển thị JOptionPane ở đây ***
        // viewHistoryButton.addActionListener(e -> {
        //     JOptionPane.showMessageDialog(this, "Chức năng 'Xem lịch sử đánh giá' đang được phát triển.");
        // });
        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(viewHistoryButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        // *** GIỮ LẠI ActionListener này để chuyển panel ***
        viewHistoryButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainFrame.VIEW_HISTORY_CARD); // Sử dụng hằng số từ MainFrame
            }
        });

        // Panel cho biểu đồ (nếu có)
        // chartPanelContainer = new JPanel(new BorderLayout());
        // chartPanelContainer.setBorder(BorderFactory.createTitledBorder("Thống kê nhanh"));
        // chartPanelContainer.setPreferredSize(new Dimension(getWidth(), 200));
        // add(chartPanelContainer, BorderLayout.SOUTH);
    }

    private void loadUserData() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentUserData() != null) {
            UserData currentUserData = session.getCurrentUserData();
            welcomeLabel.setText("Chào mừng, " + currentUserData.getName() + "!");
        } else {
            welcomeLabel.setText("Chào mừng! (Không có thông tin người dùng)");
            // Cân nhắc gọi logout nếu session không hợp lệ
            if (mainFrame != null && (!session.isLoggedIn() || session.getCurrentUserData() == null) ){
                 mainFrame.performLogout();
            }
        }
    }

    private void loadChart() {
        // Logic để tạo và hiển thị JFreeChart
        System.out.println("Chức năng biểu đồ cho UserDashboardPanel đang được phát triển.");
        // if (assessmentService != null && chartPanelContainer != null && SessionManager.getInstance().isLoggedIn()) {
        //     UserData currentUser = SessionManager.getInstance().getCurrentUserData();
        //     if (currentUser != null) {
        //         // Ví dụ: Lấy dữ liệu cho biểu đồ từ service
        //         // DefaultPieDataset dataset = assessmentService.getSummaryChartDataForUser(currentUser.getId());
        //         // if (dataset != null && dataset.getItemCount() > 0) {
        //         //     JFreeChart chart = ChartFactory.createPieChart("Tóm tắt kết quả", dataset, true, true, false);
        //         //     ChartPanel chartDisplay = new ChartPanel(chart);
        //         //     chartPanelContainer.removeAll();
        //         //     chartPanelContainer.add(chartDisplay, BorderLayout.CENTER);
        //         //     chartPanelContainer.revalidate();
        //         //     chartPanelContainer.repaint();
        //         // } else {
        //         //     chartPanelContainer.removeAll(); // Xóa biểu đồ cũ nếu có
        //         //     chartPanelContainer.add(new JLabel("Chưa có dữ liệu để vẽ biểu đồ.", SwingConstants.CENTER));
        //         //     chartPanelContainer.revalidate();
        //         //     chartPanelContainer.repaint();
        //         // }
        //     }
        // }
    }

    public void panelVisible() {
        loadUserData();
        loadChart();
        System.out.println("UserDashboardPanel is now visible.");
    }
}