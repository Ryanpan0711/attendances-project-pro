package web.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import web.attendance.bean.Attendance;

@WebServlet("/api/export")
public class ExportController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 設定回應標頭，告訴瀏覽器這是一個要下載的檔案
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");

        // BOM (Byte Order Mark) 讓 Excel 能正確讀取 UTF-8
        resp.getOutputStream().write(0xEF);
        resp.getOutputStream().write(0xBB);
        resp.getOutputStream().write(0xBF);

        PrintWriter out = resp.getWriter();

        // 1. 寫入 CSV 標頭 (Header)
        out.println("員工ID,打卡時間,類型,狀態,備註");

        // 2. 獲取資料 (模擬從 Service/DB 撈取)
        List<Attendance> dataList = getRealData();

        // 3. 寫入資料列
        for (Attendance record : dataList) {
            // 處理 CSV 的特殊字元 (例如逗號)，簡單用引號包起來
            String safeMemo = record.getMemo() == null ? "" : record.getMemo().replace("\"", "\"\"");

            out.printf("%s,%s,%s,%s,\"%s\"%n",
                    record.getUserId(),
                    record.getCheckInTime().toString(),
                    record.getCheckInType(),
                    record.getStatus(),
                    safeMemo);
        }

        out.flush();
    }

    // 使用 DAO 獲取真實資料
    private List<Attendance> getRealData() {
        web.attendance.dao.AttendanceDAO dao = new web.attendance.dao.AttendanceDAO();
        return dao.findAll();
    }
}
