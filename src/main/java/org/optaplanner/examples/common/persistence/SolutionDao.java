package org.optaplanner.examples.common.persistence;

import java.io.File;

import org.optaplanner.core.api.domain.solution.PlanningSolution;

/**
 * Data Access Object for the examples.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public interface SolutionDao<Solution_> {

    String getDirName();

    File getDataDir();

    String getFileExtension();

    Solution_ readSolutionFromFile(File inputSolutionFile);

    void writeSolution(Solution_ solution, File outputSolutionFile);

}
