package de.tudarmstadt.tk.statistics.config;
/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import de.tudarmstadt.tk.statistics.test.Statistics;


/**
 * Singleton to encapsulate the configuration for the statistical evaluation
 * Parameters can be set both via config file or programmatically 
 * @author Guckelsberger, Schulz
 *
 */
public class StatsConfig {

	// Singleton: Only allow for one instance of RStatistics
	private static volatile StatsConfig instance = null;
	
    private static final Logger logger = LogManager.getLogger("Statistics");
    private static String SCHEMA_PATH = "config.xsd";
	
	private	HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests = null;
	private	List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections = null;
	private HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels = null;
	private int selectBestN;
	private String selectByMeasure;
	private StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable;
	
	/**
	 * Singleton constructor reading the configuration data from an external xml file
	 * @param filePath the path of the configuration xml file
	 * @return an object of type {@link StatsConfig}
	 */
	public static StatsConfig getInstance(String filePath) {
		if (instance == null) {
			synchronized (Statistics.class) {
				if (instance == null) {
					instance = new StatsConfig(filePath);
				}
			}
		}
		return instance;
	}
	
	/**
	 * Singleton constructor requiring the user to supply all configuration parameters as argument.
	 * @return an object of type {@link StatsConfig}
	 */
	public static StatsConfig getInstance(HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests, List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections, HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels, int selectBestN, String selectByMeasure, StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable) {
		if (instance == null) {
			synchronized (Statistics.class) {
				if (instance == null) {
					
					//Validate arguments
					//Tests
					if(requiredTests.size()!=StatsConfigConstants.TEST_CLASSES.values().length){
						throw new IllegalArgumentException("Number of test classes specified does not match requirements!");
					}
					Iterator<StatsConfigConstants.TEST_CLASSES> itT = requiredTests.keySet().iterator();
					while(itT.hasNext()){
						StatsConfigConstants.TEST_CLASSES testClass = itT.next();
						String testName = requiredTests.get(testClass);
						
						if(!StatsConfigConstants.TESTS.get(testClass).contains(testName)){
							throw new IllegalArgumentException(testName + " is not a valid test for test class " + testClass+"!");
						}
					}
					
					//Correction methods
					if(requiredCorrections.size()==0){
						throw new IllegalArgumentException("At least one p-value correction method must be specified!");	
					}
					
					//Significance levels
					if(requiredCorrections.size()!=StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.values().length){
						throw new IllegalArgumentException("Number of significance levels specified does not match requirements!");
					}
					Iterator<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES> it = significanceLevels.keySet().iterator();
					while(it.hasNext()){
						double significanceValue = significanceLevels.get(it.next());
						if(significanceValue<0 || significanceValue>1){
							throw new IllegalArgumentException(significanceValue + " is not a valid significance value (must be between 0 and 1)!");
						}
					}
					
					instance = new StatsConfig(requiredTests, requiredCorrections, significanceLevels, selectBestN, selectByMeasure, fixIndependentVariable); 
					
				}
			}
		}
		return instance;
	}
	
	private StatsConfig(String pathToConfigFile) {

		this.parseXML(pathToConfigFile);
		
	}
	
	/**
	 * Singleton constructor setting default values to the config file
	 * @return an object of type {@link StatsConfig}
	 */
	public static StatsConfig getInstance(){
		
		if (instance == null) {
			synchronized (Statistics.class) {
				if (instance == null) {
					HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests = new HashMap<StatsConfigConstants.TEST_CLASSES,String>();
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametricContingency, "McNemar");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesParametric, "DependentT");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametric, "WilcoxonSignedRank");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametric, "RepeatedMeasuresOneWayANOVA");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametric, "Friedman");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthoc, "Tukey");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHoc, "Nemenyi");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthocBaseline, "Dunett");
					requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHocBaseline, "PairwiseWilcoxonSignedRank");
					
					List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections = new ArrayList<StatsConfigConstants.CORRECTION_VALUES>();
					requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.bonferroni);
					requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.hochberg);
					requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.holm);
					
					HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double> significanceLevels = new HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double>();
					significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.low, 0.1);
					significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.medium, 0.05);
					significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.high, 0.01);
			
					StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable = StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier;
			
					int selectBestN = 10;
					String selectByMeasure = "Weighted F-Measure";
					
					instance = new StatsConfig(requiredTests, requiredCorrections, significanceLevels, selectBestN, selectByMeasure, fixIndependentVariable); 
					
				}
			}
		}
		return instance;
	}
	
	private StatsConfig(HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests, List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections, HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels, int selectBestN, String selectByMeasure, StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable){

		this.requiredTests = requiredTests;
		this.requiredCorrections = requiredCorrections;
		this.significanceLevels = significanceLevels;
		this.selectBestN = selectBestN;
		this.selectByMeasure = selectByMeasure;
		this.fixIndependentVariable = fixIndependentVariable;
		
	}
	
	/**
	 * Validate and parse XML config file. Also check if file contains legal values for tests, p-value corrections and signficiance levels.
	 * @param pathToConfigFile 
	 */
	private void parseXML(String pathToConfigFile){

		 //Validate
        XMLStreamReader reader;
		try {
			reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(pathToConfigFile));

	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new File(SCHEMA_PATH));

	        Validator validator = schema.newValidator();
	        validator.validate(new StAXSource(reader));
	        
		}catch(IllegalArgumentException e){
			logger.log(Level.ERROR, "Statistics config file doesn't validate!");
			System.err.println("Statistics config file doesn't validate!");
			System.exit(1);
		}catch (XMLStreamException	| FactoryConfigurationError | SAXException | IOException e1) {
			logger.log(Level.ERROR, "Error while validating statistics config file.");
			System.err.println("Error while validating statistics config file.");
			System.exit(1);
		}

		
		//Parse
		requiredTests = new HashMap<StatsConfigConstants.TEST_CLASSES,String>();
		requiredCorrections = new ArrayList<StatsConfigConstants.CORRECTION_VALUES>();
		significanceLevels = new HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double>();
		
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    InputStream in;
	    try {
	    	in = new FileInputStream("config.xml");
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			   
		      while (eventReader.hasNext()) {
		          XMLEvent event = eventReader.nextEvent();

		          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("test")) {
		        	  String c = null;
		        	  String n = null;
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("test"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("class")) {
				        	  event = eventReader.nextEvent();
				        	  c = event.asCharacters().getData();
				          } else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("name")) {
				        	  event = eventReader.nextEvent();
				        	  n = event.asCharacters().getData();
				          }
		        	  }
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.TEST_CLASSES tc : StatsConfigConstants.TEST_CLASSES.values()) {
		        	        if (tc.name().equals(c)) {
		        	        	 if(StatsConfigConstants.TESTS.get(tc).contains(n)){
		        	        		 requiredTests.put(tc, n);
				        	         illegal = false;
						        	 break;
				        		  }
		        	        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(c + ", " + n); 
		        	  }
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("significanceLevel")) {
		        	  String l = null;
		        	  double v = 1; 
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("significanceLevel"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("level")) {
				        	  event = eventReader.nextEvent();
				        	  l = event.asCharacters().getData();
				          }else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("value")) {
				        	  event = eventReader.nextEvent();
				        	  v = Double.parseDouble(event.asCharacters().getData());
				          }
		        	  }
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES s : StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.values()) {
		        	        if (s.name().equals(l)) {
		        	        	significanceLevels.put(s, v);
		        	        	illegal = false;
					        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(l); 
		        	  }
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("pCorrection")) {
		        	  event = eventReader.nextEvent();
		        	  String pC = event.asCharacters().getData();
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.CORRECTION_VALUES c : StatsConfigConstants.CORRECTION_VALUES.values()) {
		        	        if (c.name().equals(pC)) {
		        	        	requiredCorrections.add(c);
		        	        	illegal = false;
					        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(pC); 
		        	  }
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("selectBest")) {
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("selectBest"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("count")) {
				        	  event = eventReader.nextEvent();
				        	  selectBestN = Integer.parseInt(event.asCharacters().getData());
				          }else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("measure")) {
				        	  event = eventReader.nextEvent();
				        	  selectByMeasure = event.asCharacters().getData();
				          }
		        	  }
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("fixIndependentVariable")) {
		        	  event = eventReader.nextEvent();
		        	  String f = event.asCharacters().getData();
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES i : StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.values()) {
		        	        if (i.name().equals(f)) {
		        	        	fixIndependentVariable=i;
		        	        	illegal=false;
		        	        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(f); 
		        	  }
		          }
		      }	
		      
	    }catch(IllegalArgumentException e){
	    	logger.log(Level.ERROR, "Illegal argument in config XML: " + e.getMessage());
			System.err.println("Illegal argument in config XML: " + e.getMessage());
			System.exit(1);
	    } catch (FileNotFoundException e) {
		 	logger.log(Level.ERROR, "Statistics config file not found.");
			System.err.println("Statistics config file not found.");
			System.exit(1);
		} catch (XMLStreamException e) {
		 	logger.log(Level.ERROR, "Error while parsing statistics config file.");
			System.err.println("Error while parsing statistics config file.");
			System.exit(1);
		}
	}

	public HashMap<StatsConfigConstants.TEST_CLASSES, String> getRequiredTests() {
		return requiredTests;
	}

	public List<StatsConfigConstants.CORRECTION_VALUES> getRequiredCorrections() {
		return requiredCorrections;
	}

	public int getSelectBestN() {
		return selectBestN;
	}

	public String getSelectByMeasure() {
		return selectByMeasure;
	}

	public HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double> getSignificanceLevels() {
		return significanceLevels;
	}

	public StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES getFixIndependentVariable() {
		return fixIndependentVariable;
	}

	public void setRequiredTests(
			HashMap<StatsConfigConstants.TEST_CLASSES, String> requiredTests) {
		this.requiredTests = requiredTests;
	}

	public void setRequiredCorrections(
			List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections) {
		this.requiredCorrections = requiredCorrections;
	}

	public void setSignificanceLevels(
			HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double> significanceLevels) {
		this.significanceLevels = significanceLevels;
	}

	public void setSelectBestN(int selectBestN) {
		this.selectBestN = selectBestN;
	}

	public void setSelectByMeasure(String selectByMeasure) {
		this.selectByMeasure = selectByMeasure;
	}

	public void setFixIndependentVariable(
			StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable) {
		this.fixIndependentVariable = fixIndependentVariable;
	}

}
