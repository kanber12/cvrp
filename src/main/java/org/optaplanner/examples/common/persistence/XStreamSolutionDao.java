package org.optaplanner.examples.common.persistence;

import java.io.File;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class XStreamSolutionDao<Solution_> extends AbstractSolutionDao<Solution_> {

    protected XStreamSolutionFileIO<Solution_> xStreamSolutionFileIO;

    public XStreamSolutionDao(String dirName, Class... xStreamAnnotations) {
        super(dirName);
        xStreamSolutionFileIO = new XStreamSolutionFileIO<>(xStreamAnnotations);
    }

    @Override
    public String getFileExtension() {
        return xStreamSolutionFileIO.getOutputFileExtension();
    }

    @Override
    public Solution_ readSolutionFromFile(File inputSolutionFile) {
        Solution_ solution = xStreamSolutionFileIO.read(inputSolutionFile);
        logger.info("Opened: {}", inputSolutionFile);
        return solution;
    }

    public Solution_ readSolutionFromString(String xml) {
        return (Solution_) xStreamSolutionFileIO.getXStream().fromXML(xml);
    }

    @Override
    public void writeSolution(Solution_ solution, File outputSolutionFile) {
        xStreamSolutionFileIO.write(solution, outputSolutionFile);
        logger.info("Saved: {}", outputSolutionFile);
    }

}
