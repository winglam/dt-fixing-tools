package edu.illinois.cs.dt.tools.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {
    private static final String DT_RANDOMIZE_ROUNDS = "dt.randomize.rounds";
    private static final int DT_RANDOMIZE_ROUNDS_NUM = 10;

    private static final Configuration config = new Configuration();

    public static Configuration config() {
        return config;
    }

    private final Properties properties = new Properties();

    public Configuration() {
    }

    public Configuration loadProperties() throws IOException {
        return loadProperties(Paths.get("dtfixingtools.properties"));
    }

    private Configuration loadProperties(final Path path) throws IOException {
        try (final InputStream fileStream = new FileInputStream(path.toFile())) {
            properties.load(fileStream);
        }

        return this;
    }

    public String getProperty(final String s, final String def) {
        if (properties.getProperty(s) == null) {
            try {
                loadProperties();
            } catch (IOException e) {
                properties.setProperty(s, def);
            }
        }

        return properties.getProperty(s);
    }

    public double getDoubleProperty(final String s) {
        return Double.parseDouble(getProperty(s, "0.0"));
    }

    public double getDoubleProperty(final String s, final double def) {
        return getProperty(s, def);
    }

    public double getProperty(final String s, final double def) {
        return Double.parseDouble(getProperty(s, String.valueOf(def)));
    }

    public int getRounds() {
        return Integer.parseInt(getProperty(DT_RANDOMIZE_ROUNDS, String.valueOf(DT_RANDOMIZE_ROUNDS_NUM)));
    }
}
