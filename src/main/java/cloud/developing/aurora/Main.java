package cloud.developing.aurora;

import static java.lang.System.getenv;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Main {

	private static final String RDS_DBNAME = getenv("RDS_DBNAME");

	private static final String RDS_HOSTNAME = getenv("RDS_HOSTNAME");

	private static final String RDS_USERNAME = getenv("RDS_USERNAME");

	private static final String RDS_PORT = getenv("RDS_PORT");

	private static final String RDS_PASSWORD = getenv("RDS_PASSWORD");

	private static final String createTable = "CREATE TABLE test (uuid VARCHAR(255), data_time VARCHAR(255), PRIMARY KEY (uuid))";

	public void run(Context context) throws Exception {
		LambdaLogger logger = context.getLogger();
		String connectionString = "jdbc:mysql://" + RDS_HOSTNAME + ":" + RDS_PORT + "/" + RDS_DBNAME + "?" + "user="
				+ RDS_USERNAME + "&password=" + RDS_PASSWORD;
		logger.log("connectionString: " + connectionString);
		try (Connection connection = DriverManager.getConnection(connectionString)) {
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet res = meta.getTables(null, null, "test", new String[] { "TABLE" });
			if (!res.first()) {
				connection.prepareStatement(createTable).execute();
			}
			PreparedStatement ps = connection.prepareStatement("insert into test values(?,?)");
			ps.setString(1, UUID.randomUUID().toString());
			ps.setString(2, LocalDateTime.now().toString());
			ps.execute();

			ResultSet rs = connection.prepareStatement("select count(*) from test").executeQuery();
			rs.next();
			int rowCount = rs.getInt(1);
			logger.log("table test exists and holds " + rowCount + " records");
		}

	}

}