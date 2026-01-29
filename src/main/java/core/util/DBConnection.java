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
		String dbUrl = System.getenv("DATABASE_URL");
		if (dbUrl != null && !dbUrl.isEmpty()) {
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
			String dbUrl = System.getenv("DATABASE_URL");
			String dbUser = System.getenv("MYSQL_USER");
			String dbPassword = System.getenv("MYSQL_PASSWORD");
			String dbName = System.getenv("MYSQL_DATABASE");

			// Railway MySQL URL 格式通常是: mysql://user:password@host:port/database
			// 需要轉換成 JDBC URL
			if (dbUrl != null && dbUrl.startsWith("mysql://")) {
				dbUrl = dbUrl.replace("mysql://", "jdbc:mysql://");
			} else if (dbUser != null && dbPassword != null) {
				// 如果沒有 DATABASE_URL，用個別變數組合
				String host = System.getenv("MYSQL_HOST");
				String port = System.getenv("MYSQL_PORT");
				dbUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
			}

			return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
		} else {
			// 本機模式：使用 JNDI DataSource
			if (ds == null) {
				throw new SQLException("缺少DataSource");
			}
			return ds.getConnection();
		}
	}
}