package web.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import core.util.DBConnection;

@WebServlet("/api/reports/missing-out")
public class MissingOutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 複雜 SQL 邏輯:
    // 找出所有 type='IN' 的紀錄，檢查該 user_id 在那之後是否沒有對應的 type='OUT'
    // 這裡我們運用一個簡單的邏輯：
    // 針對每一個員工，找出每一天有 IN 但沒有 OUT 的日期。

    // 較高效的 SQL (Self Join 或 Not Exists):
    // 找出有 IN 的紀錄 A
    // 確保不存在另一筆紀錄 B，其中 B.user_id = A.user_id 且 B.type = 'OUT' 且 B.time > A.time 且
    // B.time 在同一天
    private static final String MISSING_OUT_SQL = "SELECT a.user_id, DATE(a.check_in_time) as work_date, a.check_in_time "
            +
            "FROM attendance a " +
            "WHERE a.type = 'IN' " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM attendance b " +
            "    WHERE b.user_id = a.user_id " +
            "    AND b.type = 'OUT' " +
            "    AND DATE(b.check_in_time) = DATE(a.check_in_time) " +
            "    AND b.check_in_time > a.check_in_time " +
            ") " +
            "AND DATE(a.check_in_time) < CURRENT_DATE " + // 只看「今天以前」的，因為今天可能還沒下班
            "ORDER BY work_date DESC, a.user_id";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        List<Map<String, String>> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(MISSING_OUT_SQL);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("userId", rs.getString("user_id"));
                row.put("date", rs.getString("work_date"));
                row.put("inTime", rs.getTimestamp("check_in_time").toString());
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
        }

        Gson gson = new Gson();
        out.print(gson.toJson(results));
    }
}
