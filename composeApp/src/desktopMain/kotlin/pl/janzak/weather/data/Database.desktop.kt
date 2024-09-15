package pl.janzak.weather.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.harawata.appdirs.AppDirsFactory
import pl.janzak.weather.database.Database
import java.nio.file.Files
import java.util.Properties
import kotlin.io.path.Path

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dataDirectory = AppDirsFactory.getInstance()
            .getUserDataDir("pl.janzak.weather", null, null)
        requireNotNull(dataDirectory)
        val directoryPath = Path(dataDirectory)
        Files.createDirectories(directoryPath)
        val dbPath = directoryPath.resolve("database.db")

        val driver: SqlDriver = JdbcSqliteDriver(
            url = "jdbc:sqlite:$dbPath",
            properties = Properties(),
            schema = Database.Schema
        )
        return driver
    }
}
