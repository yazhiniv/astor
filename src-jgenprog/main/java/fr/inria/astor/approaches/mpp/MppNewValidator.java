package fr.inria.astor.approaches.mpp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.util.Name.Table;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.entities.TestCaseVariantValidationResult;
import fr.inria.astor.core.entities.VariantValidationResult;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.TestCaseResult;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.core.stats.Stats.GeneralStatEnum;
import fr.inria.astor.core.validation.ProgramVariantValidator;
import fr.inria.astor.core.validation.processbased.LaucherJUnitProcess;
import fr.inria.astor.core.validation.processbased.ProcessValidator;
import fr.inria.astor.core.validation.results.TestCasesProgramValidationResult;
import fr.inria.astor.core.validation.results.TestResult;

public class MppNewValidator extends ProcessValidator {
	MPPTable mppTable = new MPPTable();
	
	@Override
	public TestCaseVariantValidationResult validate(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade,
			boolean forceExecuteRegression) {		
		try {
			URL[] bc = createClassPath(mutatedVariant, projectFacade);

			LaucherJUnitProcess testProcessRunner = new LaucherJUnitProcess();

			log.debug("-Running first validation");

			long t1 = System.currentTimeMillis();
			String jvmPath = ConfigurationProperties.getProperty("jvm4testexecution");

			TestResult trfailing = testProcessRunner.execute(jvmPath, bc,
					projectFacade.getProperties().getFailingTestCases(),
					ConfigurationProperties.getPropertyInt("tmax1"));
			long t2 = System.currentTimeMillis();
			//My code
			//long time = t2 - t1;
			//log.debug("Time taken for TC execution.."+time);

			if (trfailing == null) {
				log.debug("**The validation 1 have not finished well**");
				return null;
			}

			log.debug(trfailing);
			TestResult finalResult = trfailing;	
			boolean runRegestionTest = false;
			if (trfailing.wasSuccessful() || forceExecuteRegression) {
				runRegestionTest = true;
				List<String> allRegressionTestCases = projectFacade.getProperties().getRegressionTestCases();
				//priority test cases based on mppTable and mutatedVariant
				List<String> allTests = mppTable.getPriorityTests(mutatedVariant,allRegressionTestCases);
				//List<String> allTests = mppTable.getPriorityTests(mutatedVariant);
				int attempts = 0;
				List<TestResult> sampledTestResults = new ArrayList<>();
				TestResult testRs=null;
				int executedTestClassesCount = 0;
				do{
					List<String> testsToExecute = samplingTests(allTests,attempts);
					attempts++;
					testRs = testProcessRunner.execute(jvmPath, bc, testsToExecute,
						ConfigurationProperties.getPropertyInt("tmax1"));
					if (testRs!=null){						
						sampledTestResults.add(testRs);	
						executedTestClassesCount += testsToExecute.size();
					}else{
						log.info("Warning: test result is null");
					}
				}while (testRs!=null && testRs.wasSuccessful() && executedTestClassesCount<allTests.size());
				
				//computed from the sampledTestResults				
				for(TestResult tr:sampledTestResults){
					finalResult.failTest.addAll(tr.getFailures());
					finalResult.failures += tr.getFailureCount();
					finalResult.successTest.addAll(tr.getSuccessTest());					
					finalResult.casesExecuted += tr.getCasesExecuted();
				}
			}			
			
			mppTable.updateTheTable(finalResult,mutatedVariant);
			
			TestCaseVariantValidationResult r = new TestCasesProgramValidationResult(finalResult,
														trfailing.wasSuccessful(), runRegestionTest);				
			return r;			

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	final int SAMPLE_SIZE = 32;
	private List<String> samplingTests(List<String> tests, int attempts) {
		// TODO Auto-generated method stub
		int startIndex = SAMPLE_SIZE * attempts;
		int endIndex = startIndex+ SAMPLE_SIZE;
		
		endIndex = Math.min(endIndex, tests.size()-1);
		
		List<String> sampleResults = new ArrayList<>();
		for(int i= startIndex;i<=endIndex;i++)
			sampleResults.add(tests.get(i));
		return sampleResults;
	}

	public void initializeTheMPPTable(List<SuspiciousCode> suspicious) {
		mppTable.initialize(suspicious);		
	}
}
