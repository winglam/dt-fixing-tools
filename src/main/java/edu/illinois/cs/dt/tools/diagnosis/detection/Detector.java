package edu.illinois.cs.dt.tools.diagnosis.detection;

import edu.illinois.cs.dt.tools.runner.data.DependentTest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface Detector {
    Stream<DependentTest> detect();

    void writeTo(Path dtFolder) throws IOException;
}
