package de.tudarmstadt.tk.statistics.test;

/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universit�t Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import de.tudarmstadt.tk.statistics.config.ReportTypes;

/**
 * Object to store the entire sample information
 * @author Guckelsberger, Schulz
 */
public class SampleData {

	// Pipeline type
	private ReportTypes pipelineType;

	// Contingency matrix: Only!=null in case of two classifiers on a single
	// domain
	private int[][] contingencyMatrix;

	// Samples: performance measure; model index; sample list
	// Example: F-Measure, ((M1, (43,543,43,21)),(M2, (343,23,23)))
	private HashMap<String, ArrayList<ArrayList<Double>>> samples;

	// Sum of samples: performance measure; model index; sum of samples
	private HashMap<String, ArrayList<Double>> samplesAverage;

	// Names of train/test datasets used in evaluation
	private List<Pair<String, String>> datasetNames;

	// Metadata of the models (classifier, feature set)
	private ArrayList<Pair<String, String>> modelMetadata;
	
	// Metadata of baseline models (can be >1 if both classifier and feature sets are independent variables)
	private ArrayList<Pair<String, String>> baselineModelMetadata;

	// Number of folds in case of a CV
	private int nFolds;

	// Number of repetitions in case of a repeated evaluation
	private int nRepetitions;

	// Indicates if this is a nxn or 1:n baseline evaluation
	private boolean isBaselineEvaluation=false;
	
	/**
	 * Creates an object he entire sample information for a particular pipeline
	 * run
	 * 
	 * @param samples
	 *            A 2-dimensional HashMap. 1st level key: performance measure
	 *            type. 2nd level key: model index, value: sample values
	 * @param samplesAverage
	 *            A HashMap comprising the average sample value per model (2nd
	 *            level) and performance measure (1st level)
	 * @param datasetNames
	 *            A List with the train/test dataset names
	 * @param modelMetadata
	 *            A HashMap comprising the metadata for each model (value),
	 *            identified by an index (key)
	 * @param isBaselineEvaluation
	 *            A boolean expressing whether this test result came from a 1:n
	 *            (baseline) or n:m evaluation
	 */
	public SampleData(
			int[][] contingencyMatrix,
			HashMap<String, ArrayList<ArrayList<Double>>> samples,
			HashMap<String, ArrayList<Double>> samplesAverage,
			List<Pair<String, String>> datasetNames,
			ArrayList<Pair<String,String>> modelMetadata,
			ArrayList<Pair<String, String>> baselineModelMetadata,
			ReportTypes pipelineType,
			int nFolds,
			int nRepetitions) {
		this.contingencyMatrix=contingencyMatrix;
		this.samples = samples;
		this.samplesAverage = samplesAverage;
		this.datasetNames = datasetNames;
		this.modelMetadata = modelMetadata;
		this.pipelineType = pipelineType;
		this.nFolds = nFolds;
		this.nRepetitions = nRepetitions;
		this.baselineModelMetadata=baselineModelMetadata;
		if(this.baselineModelMetadata.size()>0){
			this.isBaselineEvaluation = true;
		}
	}

	public int[][] getContingencyMatrix() {
		return contingencyMatrix;
	}

	public HashMap<String, ArrayList<ArrayList<Double>>> getSamples() {
		return samples;
	}

	public HashMap<String, ArrayList<Double>> getSamplesAverage() {
		return samplesAverage;
	}

	public List<Pair<String, String>> getDatasetNames() {
		return datasetNames;
	}

	public ArrayList<Pair<String, String>> getModelMetadata() {
		return modelMetadata;
	}

	public ReportTypes getPipelineType() {
		return pipelineType;
	}

	public int getnFolds() {
		return nFolds;
	}

	public int getnRepetitions() {
		return nRepetitions;
	}

	public boolean isBaselineEvaluation() {
		return isBaselineEvaluation;
	}

	public ArrayList<Pair<String, String>> getBaselineModelMetadata() {
		return baselineModelMetadata;
	}

	public void setBaselineModelMetadata(
			ArrayList<Pair<String, String>> baselineModelMetadata) {
		this.baselineModelMetadata = baselineModelMetadata;
	}
	
}
