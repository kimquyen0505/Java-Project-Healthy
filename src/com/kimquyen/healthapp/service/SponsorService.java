// com/kimquyen/healthapp/service/SponsorService.java
package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.dao.SponsorDAO;
import com.kimquyen.healthapp.dao.UserDataDAO; // Cần để lấy danh sách người dùng được tài trợ
import com.kimquyen.healthapp.model.Sponsor;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.util.ValidationUtil; // Nếu bạn có validation chung

import java.util.List;
import java.util.Collections; // Để trả về danh sách rỗng an toàn

public class SponsorService {
    private final SponsorDAO sponsorDAO;
    private final UserDataDAO userDataDAO; // DAO để lấy thông tin người dùng

    public SponsorService(SponsorDAO sponsorDAO, UserDataDAO userDataDAO) {
        if (sponsorDAO == null) {
            throw new IllegalArgumentException("SponsorDAO không được null khi khởi tạo SponsorService.");
        }
        if (userDataDAO == null) {
            throw new IllegalArgumentException("UserDataDAO không được null khi khởi tạo SponsorService.");
        }
        this.sponsorDAO = sponsorDAO;
        this.userDataDAO = userDataDAO;
    }

    public List<Sponsor> getAllSponsors() {
        List<Sponsor> sponsors = sponsorDAO.getAllSponsors();
        return (sponsors != null) ? sponsors : Collections.emptyList();
    }

    public Sponsor getSponsorById(int sponsorId) {
        if (sponsorId <= 0) {
            System.err.println("SERVICE (getSponsorById): Sponsor ID không hợp lệ.");
            return null;
        }
        return sponsorDAO.getSponsorById(sponsorId);
    }

    public boolean addSponsor(Sponsor sponsor) {
        if (sponsor == null || ValidationUtil.isNullOrEmpty(sponsor.getName())) {
            System.err.println("SERVICE (addSponsor): Tên nhà tài trợ không được để trống.");
            return false;
        }
        // Kiểm tra xem tên nhà tài trợ đã tồn tại chưa (nếu cần)
        // List<Sponsor> existingSponsors = sponsorDAO.getAllSponsors();
        // for (Sponsor s : existingSponsors) {
        //     if (s.getName().equalsIgnoreCase(sponsor.getName().trim())) {
        //         System.err.println("SERVICE (addSponsor): Tên nhà tài trợ '" + sponsor.getName() + "' đã tồn tại.");
        //         return false;
        //     }
        // }
        return sponsorDAO.addSponsor(sponsor);
    }

    public boolean updateSponsor(Sponsor sponsor) {
        if (sponsor == null || sponsor.getId() == 0 || ValidationUtil.isNullOrEmpty(sponsor.getName())) {
            System.err.println("SERVICE (updateSponsor): Thông tin nhà tài trợ không hợp lệ để cập nhật.");
            return false;
        }
        // Kiểm tra trùng tên (ngoại trừ chính nó) nếu cần
        return sponsorDAO.updateSponsor(sponsor);
    }

    public boolean deleteSponsor(int sponsorId) {
        if (sponsorId <= 0) {
            System.err.println("SERVICE (deleteSponsor): Sponsor ID không hợp lệ để xóa.");
            return false;
        }
        // **QUAN TRỌNG**: Trước khi xóa nhà tài trợ, bạn cần quyết định điều gì sẽ xảy ra
        // với những UserData đang được tài trợ bởi nhà tài trợ này.
        // 1. Cấm xóa nếu có người dùng đang được tài trợ.
        // 2. Set sponsor_id của những người dùng đó thành NULL (hoặc một ID mặc định "Không có nhà tài trợ").
        // 3. Xóa luôn cả những người dùng đó (ít khả năng).

        // Ví dụ: Kiểm tra lựa chọn 1
        List<UserData> sponsoredUsers = userDataDAO.getUsersBySponsorId(sponsorId);
        if (sponsoredUsers != null && !sponsoredUsers.isEmpty()) {
            System.err.println("SERVICE (deleteSponsor): Không thể xóa nhà tài trợ ID " + sponsorId +
                               " vì có " + sponsoredUsers.size() + " người dùng đang được tài trợ.");
            // Có thể ném một exception hoặc trả về một mã lỗi đặc biệt để UI hiển thị thông báo
            return false; // Hoặc throw new SponsorInUseException("Nhà tài trợ đang được sử dụng bởi người dùng.");
        }

        return sponsorDAO.deleteSponsor(sponsorId);
    }

    /**
     * Lấy danh sách những người dùng được tài trợ bởi một nhà tài trợ cụ thể.
     * @param sponsorId ID của nhà tài trợ.
     * @return Danh sách UserData, hoặc danh sách rỗng nếu không có hoặc có lỗi.
     */
    public List<UserData> getUsersSponsoredBy(int sponsorId) {
        if (sponsorId <= 0) {
            System.err.println("SERVICE (getUsersSponsoredBy): Sponsor ID không hợp lệ.");
            return Collections.emptyList();
        }
        List<UserData> users = userDataDAO.getUsersBySponsorId(sponsorId);
        return (users != null) ? users : Collections.emptyList();
    }
}