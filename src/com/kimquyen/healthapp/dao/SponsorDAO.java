package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.Sponsor;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorDAO {

    public Sponsor getSponsorById(int sponsorId) {
        String sql = "SELECT id, name FROM sponsor_data WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sponsorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Sponsor(
                    rs.getInt("id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Sponsor theo id: " + sponsorId);
            e.printStackTrace();
        }
        return null;
    }

    public List<Sponsor> getAllSponsors() {
        List<Sponsor> sponsors = new ArrayList<>();
        String sql = "SELECT id, name FROM sponsor_data ORDER BY name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sponsor sponsor = new Sponsor(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                sponsors.add(sponsor);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả Sponsors");
            e.printStackTrace();
        }
        return sponsors;
    }

    public boolean addSponsor(Sponsor sponsor) {
        String sql = "INSERT INTO sponsor_data (name) VALUES (?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sponsor.getName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Sponsor: " + sponsor.getName());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSponsor(Sponsor sponsor) {
        String sql = "UPDATE sponsor_data SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sponsor.getName());
            pstmt.setInt(2, sponsor.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Sponsor: " + sponsor.getId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSponsor(int sponsorId) {
        String sql = "DELETE FROM sponsor_data WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sponsorId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Sponsor: " + sponsorId);
            e.printStackTrace();
            return false;
        }
    }
}