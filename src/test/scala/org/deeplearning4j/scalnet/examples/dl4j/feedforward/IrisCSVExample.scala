package org.deeplearning4j.scalnet.examples.dl4j.feedforward

import java.util

import com.typesafe.scalalogging.LazyLogging
import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.split.FileSplit
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.scalnet.layers.Dense
import org.deeplearning4j.scalnet.models.NeuralNet
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.{DataSet, SplitTestAndTrain}
import org.nd4j.linalg.io.ClassPathResource
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

object IrisCSVExample extends App with LazyLogging {

  val numLinesToSkip = 0
  val delimiter = ','
  val labelIndex = 4
  val nClasses = 3
  val batchSize = 150
  val hiddenSize = 128
  val inputSize = 4
  val outputSize = 3
  val epochs = 1000
  val scoreFrequency = 5
  val seed = 1234

  logger.info("Reading data set...")
  val recordReader = new CSVRecordReader(numLinesToSkip, delimiter)
  recordReader.initialize(new FileSplit(new ClassPathResource("iris.txt").getFile))
  val iterator: DataSetIterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, nClasses)

  logger.info("Prepare data set for training...")
  val next: DataSet = iterator.next()
  next.shuffle()
  val testAndTrain: SplitTestAndTrain = next.splitTestAndTrain(0.75)
  val test_data: DataSet = testAndTrain.getTest
  val training_ : util.List[DataSet] = testAndTrain.getTrain.asList()
  val training_data = new ListDataSetIterator(training_, training_.size)

  logger.info("Build model...")
  val model: NeuralNet = NeuralNet(rngSeed = seed)
  model.add(Dense(nIn = inputSize, nOut = hiddenSize, activation = Activation.RELU))
  model.add(Dense(nOut = hiddenSize, activation = Activation.RELU))
  model.add(Dense(nOut = hiddenSize, activation = Activation.RELU))
  model.add(Dense(outputSize, activation = Activation.SOFTMAX))
  model.compile(lossFunction = LossFunction.MCXENT)

  logger.info("Train model...")
  model.fit(iter = training_data, nbEpoch = epochs, listeners = List(new ScoreIterationListener(scoreFrequency)))

  logger.info("Evaluate model...")
  logger.info(s"Train accuracy = ${model.evaluate(training_data).accuracy}")
  logger.info(s"Test accuracy = ${model.evaluate(test_data).accuracy}")
}
