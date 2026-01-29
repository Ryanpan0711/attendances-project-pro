package web.attendance.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import web.attendance.bean.Attendance;
import web.attendance.service.CheckInService;

@WebServlet("/api/checkin")
public class CheckInController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private CheckInService checkInService;
    private Gson gson;

    // IP 白名單 (實際可移至設定檔)
    private static final List<String> ALLOWED_IPS = Arrays.asList(
            "127.0.0.1", "0:0:0:0:0:0:0:1", "192.168.1.100" // 包含 localhost 和範例公司 IP
    );

    @Override
    public void init() throws ServletException {
        this.checkInService = new CheckInService();
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 1. IP 白名單檢查 (Security Check) - 暫時停用以便測試雲端部署
            // String clientIp = req.getRemoteAddr();
            // if (!ALLOWED_IPS.contains(clientIp)) {
            // resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // throw new SecurityException("IP Address not allowed: " + clientIp);
            // }
            String clientIp = req.getRemoteAddr(); // 保留 IP 記錄但不檢查

            // 2. 讀取 JSON Body
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            JsonObject jsonBody;
            try {
                jsonBody = gson.fromJson(jsonBuffer.toString(), JsonObject.class);
            } catch (JsonSyntaxException e) {
                throw new IllegalArgumentException("無效的 JSON 格式");
            }

            if (jsonBody == null || !jsonBody.has("userId")) {
                throw new IllegalArgumentException("請求必須包含 'userId'");
            }

            String userId = jsonBody.get("userId").getAsString();
            String memo = jsonBody.has("memo") ? jsonBody.get("memo").getAsString() : "";
            // 預設為 IN，若前端有傳 checkInType 則使用 (IN 或 OUT)
            String checkInType = jsonBody.has("checkInType") ? jsonBody.get("checkInType").getAsString() : "IN";

            // 3. 呼叫 Service (傳入 checkInType)
            Attendance result = checkInService.processCheckIn(userId, clientIp, checkInType, memo);

            // 4. 回傳成功
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "success");
            responseJson.add("data", gson.toJsonTree(result));
            out.print(gson.toJson(responseJson));

        } catch (SecurityException e) {
            // 403 已設定
            sendErrorResponse(out, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendErrorResponse(out, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(out, "系統錯誤: " + e.getMessage());
        }
    }

    private void sendErrorResponse(PrintWriter out, String message) {
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("status", "error");
        errorJson.addProperty("message", message);
        out.print(gson.toJson(errorJson));
    }
}
