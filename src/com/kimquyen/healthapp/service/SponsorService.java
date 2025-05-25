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
    private final UserDataDAO userDataDAO; 

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

        return sponsorDAO.addSponsor(sponsor);
    }

    public boolean updateSponsor(Sponsor sponsor) {
        if (sponsor == null || sponsor.getId() == 0 || ValidationUtil.isNullOrEmpty(sponsor.getName())) {
            System.err.println("SERVICE (updateSponsor): Thông tin nhà tài trợ không hợp lệ để cập nhật.");
            return false;
        }
        return sponsorDAO.updateSponsor(sponsor);
    }

    public boolean deleteSponsor(int sponsorId) {
        if (sponsorId <= 0) {
            System.err.println("SERVICE (deleteSponsor): Sponsor ID không hợp lệ để xóa.");
            return false;
        }


        List<UserData> sponsoredUsers = userDataDAO.getUsersBySponsorId(sponsorId);
        if (sponsoredUsers != null && !sponsoredUsers.isEmpty()) {
            System.err.println("SERVICE (deleteSponsor): Không thể xóa nhà tài trợ ID " + sponsorId +
                               " vì có " + sponsoredUsers.size() + " người dùng đang được tài trợ.");
            return false; 
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