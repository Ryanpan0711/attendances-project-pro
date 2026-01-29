package core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBConnection {

	private static DataSource ds = null;
	private static boolean useEnvVars = false;

	static {
		// 優先嘗試使用環境變數（Railway 雲端環境）
		String dbUrl = System.getenv("MYSQL_PUBLIC_URL");
		String dbHost = System.getenv("MYSQLHOST");

		if ((dbUrl != null && !dbUrl.isEmpty()) || (dbHost != null && !dbHost.isEmpty())) {
			useEnvVars = true;
			System.out.println("使用環境變數連接資料庫 (Railway mode)");
		} else {
			// 本機環境使用 JNDI
			try {
				Context ctx = new InitialContext();
				ds = (DataSource) ctx.lookup("java:comp/env/jdbc/attendances");
				System.out.println("使用 JNDI 連接資料庫 (Local mode)");
			} catch (NamingException e) {
				System.err.println("JNDI 查找失敗，將嘗試使用環境變數");
				useEnvVars = true;
			}
		}
	}

	// 呼叫此方法 取得連線
	public static Connection getConnection() throws SQLException {
		if (useEnvVars) {
			// Railway 模式：從環境變數讀取
			// Railway 提供的變數名稱（全大寫無底線）
			String dbUrl = System.getenv("MYSQL_PUBLIC_URL");
			String dbUser = System.getenv("MYSQLUSER");
			String dbPassword = System.getenv("MYSQLPASSWORD");
			String dbHost = System.getenv("MYSQLHOST");
			String dbPort = System.getenv("MYSQLPORT");
			String dbName = System.getenv("MYSQLDATABASE");

			// 如果有完整的 URL，直接使用
			if (dbUrl != null && !dbUrl.isEmpty()) {
				// Railway 的 URL 格式: mysql://user:pass@host:port/db
				// 轉換成 JDBC URL
				dbUrl = dbUrl.replace("mysql://", "jdbc:mysql://");
				// 從 URL 中提取 user 和 password（如果需要）
				return DriverManager.getConnection(dbUrl);
			} else if (dbHost != null && dbUser != null && dbPassword != null) {
				// 用個別變數組合
				String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbName);
				return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
			} else {
				throw new SQLException("找不到資料庫環境變數 (MYSQL_PUBLIC_URL 或 MYSQLHOST/MYSQLUSER/MYSQLPASSWORD)");
			}
		} else {
			// 本機模式：使用 JNDI DataSource
			if (ds == null) {
				throw new SQLException("缺少DataSource");
			}
			return ds.getConnection();
		}
	}
}