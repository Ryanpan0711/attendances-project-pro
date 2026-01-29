package web.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import core.util.DBConnection;

@WebServlet("/api/reports/weekly")
public class WeeklyReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 複雜的統計 SQL 會在這裡執行
    // 計算邏輯：
    // 總次數 (totalPresent): 計算該 user 在本週有多少個 type='IN'
    // 遲到次數 (lateCount): 計算 type='IN' 且 status='LATE' 的數量
    // 異常/早退次數 (abnormalCount): 計算 type='OUT' 且 status='ABNORMAL' 的數量
    // 漏打卡 (missingCount): 稍微複雜，這裡先簡單定義為 status='MANUAL_FIX' 的數量，或由前端
    // MissingOutServlet 處理
    private static final String WEEKLY_STATS_SQL = "SELECT " +
            "  COUNT(CASE WHEN type = 'IN' THEN 1 END) as total_present, " +
            "  COUNT(CASE WHEN type = 'IN' AND status = 'LATE' THEN 1 END) as late_count, " +
            "  COUNT(CASE WHEN type = 'OUT' AND status = 'ABNORMAL' THEN 1 END) as early_leave_count " +
            "FROM attendance " +
            "WHERE user_id = ? " +
            "AND YEARWEEK(check_in_time, 1) = YEARWEEK(CURRENT_DATE, 1)";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userId");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if (userId == null) {
            resp.setStatus(400);
            return;
        }

        JsonObject stats = new JsonObject();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(WEEKLY_STATS_SQL)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.addProperty("totalPresent", rs.getInt("total_present"));
                    stats.addProperty("lateCount", rs.getInt("late_count"));
                    stats.addProperty("earlyLeaveCount", rs.getInt("early_leave_count"));
                    // 這裡可以再 call MissingOutServlet 的邏輯算漏打卡，為簡化先回傳 0
                    stats.addProperty("missingCount", 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            stats.addProperty("error", e.getMessage());
        }

        out.print(stats.toString());
    }
}
