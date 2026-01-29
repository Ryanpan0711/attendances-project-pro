package web.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import web.attendance.bean.Attendance;
import web.attendance.dao.AttendanceDAO;

@WebServlet("/api/status")
public class StatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AttendanceDAO attendanceDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.attendanceDAO = new AttendanceDAO();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 檢查 user_id 參數
        String userId = req.getParameter("userId");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (userId == null || userId.trim().isEmpty()) {
            resp.setStatus(400);
            out.print(gson.toJson(error("缺少 userId")));
            return;
        }

        // 使用新方法更精確判斷
        boolean hasCheckedInToday = attendanceDAO.findTodayRecord(userId, "IN");
        boolean hasCheckedOutToday = attendanceDAO.findTodayRecord(userId, "OUT");

        // 為了相容前端顯示 lastType
        String lastType = "";
        Attendance lastRecord = attendanceDAO.findLastByUserId(userId);
        if (lastRecord != null) {
            lastType = lastRecord.getCheckInType();
        }

        JsonObject status = new JsonObject();
        status.addProperty("userId", userId);
        status.addProperty("hasCheckedIn", hasCheckedInToday); // 用來 Disable 上班按鈕
        status.addProperty("hasCheckedOut", hasCheckedOutToday); // 用來 Disable 下班按鈕 (如果需要)
        status.addProperty("lastType", lastType);

        out.print(gson.toJson(status));
    }

    private JsonObject error(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("error", msg);
        return json;
    }
}
