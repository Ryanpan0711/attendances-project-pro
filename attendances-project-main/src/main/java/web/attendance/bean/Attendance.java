package web.attendance.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String userId; // 員工ID
    private Timestamp checkInTime; // 打卡時間
    private String status; // 狀態: 正常(NORMAL), 異常(ABNORMAL), 遲到(LATE)
    private String checkInType; // 類型: 上班(IN), 下班(OUT)
    private String memo; // 備註

    public Attendance() {
    }

    public Attendance(String userId, Timestamp checkInTime, String status) {
        this.userId = userId;
        this.checkInTime = checkInTime;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckInType() {
        return checkInType;
    }

    public void setCheckInType(String checkInType) {
        this.checkInType = checkInType;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
