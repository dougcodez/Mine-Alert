package io.github.dougcodez.minealert.mysql.api;

public enum SQLTypes {


    MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://{host}:{port}/{database}"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:{database}");

    private final String driverName;
    private final String driverURL;

    SQLTypes(String driverName, String driverURL) {
        this.driverName = driverName;
        this.driverURL = driverURL;
    }

    public static SQLTypes fromName(String name) {
        for (SQLTypes type : values()) {
            if (type.name().equalsIgnoreCase(name))
                return type;
        }
        return SQLITE;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverURL() {
        return driverURL;
    }
}