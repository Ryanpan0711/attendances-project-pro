package web.attendance.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import web.attendance.bean.Attendance;
import web.attendance.dao.AttendanceDAO;

public class CheckInService {

    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0, 0);
    private AttendanceDAO attendanceDao; // Inject DAO

    public CheckInService() {
        this.attendanceDao = new AttendanceDAO();
    }

    public Attendance processCheckIn(String userId, String clientIp) throws Exception {
        // Default to "IN" check-in type for the simpler overload
        return processCheckIn(userId, clientIp, "IN", "");
    }

    public Attendance processCheckIn(String userId, String clientIp, String checkInType, String memo) throws Exception {
        // 1. 定義標準時間
        LocalTime WORK_START_TIME = LocalTime.of(9, 0, 0);
        LocalTime WORK_END_TIME = LocalTime.of(18, 0, 0);

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        String status = "NORMAL";
        String systemMemo = "";

        // 2. 核心異常判定邏輯 (Core Status Logic)
        if ("IN".equals(checkInType)) {
            // --- 上班打卡邏輯 ---

            // 檢查是否重複打卡 (當日已 IN 過則擋) - Optional Safety
            // if (hasCheckedInToday(userId)) throw new Exception("今日已上班打卡");

            if (now.toLocalTime().isAfter(WORK_START_TIME)) {
                status = "LATE";
                systemMemo = "遲到";
            }

        } else if ("OUT".equals(checkInType)) {
            // --- 下班打卡邏輯 ---

            if (now.toLocalTime().isBefore(WORK_END_TIME)) {
                status = "ABNORMAL"; // 或 EARLY_LEAVE
                systemMemo = "早退";
                // 強制備註檢查：早退也必須說明原因
                if (memo == null || memo.trim().isEmpty()) {
                    throw new IllegalArgumentException("早退必須填寫備註 (Reason required for early leave)");
                }
            }
        }

        // 3. 處理備註合併 (使用者輸入 + 系統判定)
        if (!systemMemo.isEmpty()) {
            if (memo != null && !memo.trim().isEmpty()) {
                memo = systemMemo + ": " + memo;
            } else {
                memo = systemMemo; // 如果使用者沒填，至少要有系統註記
            }
        }

        // 4. 寫入資料庫 (DAO)
        Attendance record = new Attendance();
        record.setUserId(userId);
        record.setCheckInTime(timestamp);
        record.setCheckInType(checkInType);
        record.setStatus(status);
        record.setMemo(memo); // 最終存入的 Memo

        attendanceDao.insert(record);

        return record;
    }
}
