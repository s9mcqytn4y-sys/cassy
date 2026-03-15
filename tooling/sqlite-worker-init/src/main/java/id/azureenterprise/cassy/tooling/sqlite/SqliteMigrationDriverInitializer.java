package id.azureenterprise.cassy.tooling.sqlite;

import app.cash.sqldelight.core.SqlDelightDatabaseProperties;
import app.cash.sqldelight.gradle.DriverInitializer;
import java.io.File;
import java.util.Properties;

public final class SqliteMigrationDriverInitializer implements DriverInitializer {
    @Override
    public void execute(SqlDelightDatabaseProperties properties, Properties driverProperties) {
        File nativeDir = new File(properties.getRootDirectory(), ".gradle/sqlite-native");
        if (!nativeDir.exists() && !nativeDir.mkdirs()) {
            throw new IllegalStateException("Unable to create sqlite native directory at " + nativeDir);
        }

        String nativePath = nativeDir.getAbsolutePath();
        System.setProperty("org.sqlite.tmpdir", nativePath);
        System.setProperty("java.io.tmpdir", nativePath);
        driverProperties.setProperty("org.sqlite.tmpdir", nativePath);
        driverProperties.setProperty("java.io.tmpdir", nativePath);
    }
}
