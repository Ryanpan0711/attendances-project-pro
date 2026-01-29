package web.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import core.util.DBConnection;
import web.attendance.bean.Attendance;

public class AttendanceDAO {

    private static final String INSERT_SQL = "INSERT INTO attendance (user_id, check_in_time, status, type, memo) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_LAST_SQL = "SELECT id, user_id, check_in_time, status, type, memo FROM attendance WHERE user_id = ? ORDER BY check_in_time DESC LIMIT 1";

    private static final String SELECT_ALL_SQL = "SELECT id, user_id, check_in_time, status, type, memo FROM attendance ORDER BY check_in_time DESC";

    public void insert(Attendance attendance) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, attendance.getUserId());
            ps.setTimestamp(2, attendance.getCheckInTime());
            ps.setString(3, attendance.getStatus());
            ps.setString(4, attendance.getCheckInType()); // IN or OUT
            ps.setString(5, attendance.getMemo());

            ps.executeUpdate();
        }
    }

    public Attendance findLastByUserId(String userId) {
        Attendance attendance = null;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_LAST_SQL)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    attendance = mapResultSetToBean(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 簡單印出錯誤，實際專案可考慮 log 或拋出
        }
        return attendance;
    }

    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBean(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 新增: 查詢今日是否有特定類型的打卡紀錄 (用於前端按鈕狀態防錯)
    public boolean findTodayRecord(String userId, String type) {
        String sql = "SELECT 1 FROM attendance WHERE user_id = ? AND type = ? AND DATE(check_in_time) = CURRENT_DATE";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // 如果有查到資料，回傳 true
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Attendance mapResultSetToBean(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getString("user_id"));
        a.setCheckInTime(rs.getTimestamp("check_in_time"));
        a.setStatus(rs.getString("status"));
        a.setCheckInType(rs.getString("type"));
        a.setMemo(rs.getString("memo"));
        return a;
    }
}
