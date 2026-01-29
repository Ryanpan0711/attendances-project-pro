-- 1. 建立考勤表
CREATE TABLE `attendance` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '流水號',
    `user_id` VARCHAR(50) NOT NULL COMMENT '員工ID (員編)',
    `check_in_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡時間',
    `type` VARCHAR(10) NOT NULL COMMENT '打卡類型 (IN:上班, OUT:下班)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '狀態 (NORMAL:正常, LATE:遲到, ABNORMAL:異常)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '備註 (遲到原因或系統註記)',
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `check_in_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '員工考勤紀錄表';
-- 2. 預設測試資料 (可選)
-- 模擬 A001 昨天正常上下班
INSERT INTO `attendance` (
        `user_id`,
        `check_in_time`,
        `type`,
        `status`,
        `memo`
    )
VALUES (
        'A001',
        DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,
        'IN',
        'NORMAL',
        ''
    ),
    (
        'A001',
        DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR,
        'OUT',
        'NORMAL',
        ''
    );
-- 模擬 A002 昨天忘記打下班卡 (用來測試漏打卡偵測)
INSERT INTO `attendance` (
        `user_id`,
        `check_in_time`,
        `type`,
        `status`,
        `memo`
    )
VALUES (
        'A002',
        DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 8 HOUR,
        'IN',
        'NORMAL',
        ''
    );