# 核心邏輯說明 (Logic Overview)

## 1. 架構設計
採用標準 MVC 分層架構（Pure Java + Servlet）：
- **Controller**: `CheckInController` (處理 HTTP 請求、IP 驗證、回應 JSON)
- **Service**: `CheckInService` (異常判斷、漏打卡偵測、強制備註檢查)
- **DAO**: `AttendanceDAO` (JDBC 存取 Database)
- **Bean**: `Attendance` (資料傳輸物件)

## 2. 關鍵流程 (processCheckIn)
位置：`web.attendance.service.CheckInService.java`

```java
public Attendance processCheckIn(String userId, String clientIp, String memo) {
    // A. 漏打卡偵測 (Missing Punch Detection)
    // 查詢該員工「最新一筆」紀錄，如果是 "IN" 且日期早於今天 -> 視為昨天忘記打卡
    Attendance lastRecord = attendanceDao.findLastByUserId(userId);
    if (lastRecord != null && "IN".equals(lastRecord.getCheckInType()) && isYesterdayOrBefore(lastRecord)) {
        memo += " [系統備註: 上次疑似漏打卡]";
    }

    // B. 時間異常判定 (Lateness Check)
    // 設定標準時間為 09:00:00
    // 如果 CurrentTime > 09:00:00 -> Status = "LATE"
    if (now.isAfter(WORK_START_TIME)) {
        status = "LATE";
        // C. 強制備註 (Mandatory Memo)
        // 遲到時，如果沒有填寫 memo，拋出例外拒絕打卡
        if (isEmpty(memo)) throw new IllegalArgumentException("遲到必須填寫備註");
    }

    // D. 存檔 (Persist)
    // 呼叫 DAO 將資料寫入 MySQL
    attendanceDao.insert(newRecord);
}
```

## 3. 安全機制 (Security)
位置：`web.attendance.controller.CheckInController.java`

- **IP 白名單**: 在 `doPost` 一開始檢查 `request.getRemoteAddr()`。
- 如果 IP 不在 `ALLOWED_IPS` 清單中，直接回傳 `403 Forbidden`。
