package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pl.janzak.cmp_demo.Database
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(
            url = "jdbc:sqlite:database.db",
            properties = Properties(),
            schema = Database.Schema
        )
        return driver
    }
}
