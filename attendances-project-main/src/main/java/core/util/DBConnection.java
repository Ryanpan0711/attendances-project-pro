package core.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBConnection {

	private static DataSource ds = null;

	static {
		try {
			Context ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/attendances");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}

	// 呼叫此方法 取得連線
	public static Connection getConnection() throws SQLException {
		if (ds == null) {
			throw new SQLException("缺少DataSource");
		}

		return ds.getConnection();
	}
}