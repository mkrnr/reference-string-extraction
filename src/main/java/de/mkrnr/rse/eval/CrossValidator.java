package de.mkrnr.rse.eval;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import cc.mallet.pipe.SerialPipes;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.train.CRFTrainer;
import de.mkrnr.rse.util.ToFileStreamer;

public class CrossValidator {

    public static void main(String[] args) {
        CrossValidator crossValidator = new CrossValidator();
        File inputDirectory = new File("/home/martin/tmp/papers/80-test-extr");
        int numberOfFolds = 10;
        List<Fold> folds = crossValidator.splitIntoFolds(inputDirectory, numberOfFolds);
        // crossValidator.generateFoldFiles(folds, inputDirectory,
        // foldsDirectory);
        for (Fold fold : folds) {
            crossValidator.validate(fold);
        }

    }

    /**
     * Splits the files in fileDirectory into folds based on the number of
     * papers. Thereby, papers are not split into different folds.
     *
     * @param inputDirectory
     *
     * @param numberOfFolds
     *            Number of folds
     */
    public List<Fold> splitIntoFolds(File inputDirectory, int numberOfFolds) {

        File[] allFiles = inputDirectory.listFiles();
        ArrayList<File> remainingFiles = new ArrayList<File>(Arrays.asList(inputDirectory.listFiles()));

        if (numberOfFolds > allFiles.length) {
            throw new IllegalStateException("More folds than files in directory");
        }

        // shuffle files
        Collections.shuffle(remainingFiles);

        ArrayList<Fold> folds = new ArrayList<Fold>();

        for (int foldIndex = 0; foldIndex < numberOfFolds; foldIndex++) {

            Fold fold = new Fold();

            int remainingFolds = numberOfFolds - foldIndex;

            int filesinFold = (remainingFiles.size()) / remainingFolds;

            HashSet<File> testingFiles = new HashSet<File>();

            // get testing files
            for (int i = 0; i < filesinFold; i++) {
                testingFiles.add(remainingFiles.remove(0));
            }

            // add files to fold
            for (File file : allFiles) {
                if (testingFiles.contains(file)) {
                    fold.addTestingFile(file);
                } else {
                    fold.addTrainingFile(file);
                }
            }

            folds.add(fold);
        }

        return folds;

    }

    public void validate(Fold fold) {

        File testingFile = this.mergeFiles(fold.getTrainingFiles());
        File trainingFile = this.mergeFiles(fold.getTrainingFiles());

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

        SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

        // TODO handle CRF configuration in additional class
        List<String> featuresNames = new ArrayList<String>();
        featuresNames.add("CAPITALIZED");
        featuresNames.add("ONELETTER");
        featuresNames.add("ENDSWITHPERIOD");
        featuresNames.add("ENDSWITHCOMMA");

        SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(featuresNames);

        CRFTrainer crfTrainer = new CRFTrainer(serialPipes);

        crfTrainer.trainByLabelLikelihood(trainingFile, testingFile, true);

        System.out.println("Evaluation:");
        TransducerTrainerEvaluator crfEvaluator = new TransducerTrainerEvaluator(serialPipes, crfTrainer.getTrainer());
        crfEvaluator.evaluate(trainingFile, testingFile);
    }

    private File mergeFiles(ArrayList<File> files) {
        // InputStream stream = new B;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (File file : files) {
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
                byteArrayOutputStream.write(inputStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        File outputFile = null;

        try {
            byteArrayOutputStream.close();
            outputFile = ToFileStreamer.streamToFile(byteArrayInputStream, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    // TODO aggregate results

}
