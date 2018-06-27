package edu.illinois.cs.dt.tools.minimizer;

import com.google.gson.Gson;
import com.reedoei.eunomia.collections.ListUtil;
import com.reedoei.eunomia.io.IOUtil;
import edu.illinois.cs.dt.tools.runner.SmartTestRunner;
import edu.washington.cs.dt.RESULT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MinimizeTestsResult {
    private static final int VERIFY_REPEAT_COUNT = 10;
    private static final int MAX_SUBSEQUENCES = 1000;

    private final RESULT expected;
    private final String dependentTest;
    private final List<String> deps;

    public MinimizeTestsResult(final RESULT expected, final String dependentTest, final List<String> deps) {
        this.expected = expected;
        this.dependentTest = dependentTest;
        this.deps = deps;
    }

    private boolean isExpected(final SmartTestRunner runner, final List<String> deps)
            throws InterruptedException, ExecutionException, TimeoutException {
        return runner
                .runOrder(deps, ListUtil.fromArray(dependentTest))
                .getResult(dependentTest).result.equals(expected);
    }

    public boolean verify()
            throws InterruptedException, ExecutionException, TimeoutException, MinimizeTestListException {
        return verify(new SmartTestRunner(), VERIFY_REPEAT_COUNT);
    }

    public boolean verify(final int verifyCount)
            throws InterruptedException, ExecutionException, TimeoutException, MinimizeTestListException {
        return verify(new SmartTestRunner(), verifyCount);
    }

    public boolean verify(final SmartTestRunner runner)
            throws InterruptedException, ExecutionException, TimeoutException, MinimizeTestListException {
        return verify(runner, VERIFY_REPEAT_COUNT);
    }

    public boolean verify(final SmartTestRunner runner, final int verifyCount)
            throws InterruptedException, ExecutionException, TimeoutException, MinimizeTestListException {
        for (int i = 0; i < verifyCount; i++) {
            final List<List<String>> depLists = ListUtil.sample(ListUtil.subsequences(deps), MAX_SUBSEQUENCES);
            int check = 1;
            int totalChecks = 2 + depLists.size() - 1;

            IOUtil.printClearLine(String.format("[INFO] Verifying %d of %d. Running check %d of %d.",  i, verifyCount, check++, totalChecks));
            // Check that it's correct with the dependencies
            if (!isExpected(runner, deps)) {
                throw new MinimizeTestListException("Got unexpected result when running with all dependencies!");
            }

            // Only run the first check if there are no dependencies.
            if (deps.isEmpty()) {
                continue;
            }

            IOUtil.printClearLine(String.format("[INFO] Verifying %d of %d. Running check %d of %d.",  i, verifyCount, check++, totalChecks));
            // Check that it's wrong without dependencies.
            if (isExpected(runner, new ArrayList<>())) {
                throw new MinimizeTestListException("Got expected result even without any dependencies!");
            }

            // Check that for any subsequence that isn't the whole list, it's wrong.
            for (final List<String> depList : depLists) {
                if (depList.equals(deps)) {
                    continue;
                }

                IOUtil.printClearLine(String.format("[INFO] Verifying %d of %d. Running check %d of %d.",  i, verifyCount, check++, totalChecks));
                if (isExpected(runner, depList)) {
                    throw new MinimizeTestListException("Got expected result without some dependencies! " + depList);
                }
            }
        }

        System.out.println();

        return true;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public void print() {
        print(null);
    }

    public void print(final Path outputPath) {
        if (outputPath != null) {
            try {
                final Path outputFile = outputPath.resolve(dependentTest + "-" + expected + "-dependencies.json");
                Files.write(outputFile, toString().getBytes());
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(toString());
    }

    public List<String> getDeps() {
        return deps;
    }
}
