package fr.inria.astor.approaches.mpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.TestCaseResult;
import fr.inria.astor.core.validation.results.TestResult;

public class MPPTable {
	
	//<suspicious statement, testcase complete name, suspicious value of statement>
	Table<SuspiciousCode, String, Double> testScoreTable = HashBasedTable.create();
	
	//<suspicious statement, testcase complete name, #patch killed by the tc and mp>
	Table<SuspiciousCode, String, Integer> mppTable = HashBasedTable.create();
	
		
	public void initialize(List<SuspiciousCode> suspicious) {
		//System.out.println("Info in the list" + Arrays.toString(susp.toArray()));
		//TODO:
		for(SuspiciousCode susp :  suspicious ){
			//System.out.println("Info in the list::\n" + susp.getClassName()+"\t"+susp.getMethodName()+"\t"+susp.getCoverage().toString()+"\t"+susp.getSuspiciousValue());
			//System.out.println("Covered by tests: ");
			List<TestCaseResult> ts = susp.getCoveredByTests();			
			for (TestCaseResult tc:ts){
				//System.out.println("Tc name::" + tc.getTestCaseCompleteName());
				testScoreTable.put(susp,tc.getTestCaseCompleteName(),susp.getSuspiciousValue());
				//System.out.println();
			}
		}
		/*System.out.println(" ========begin table=======");
		for(String col:testScoreTable.columnKeySet()){
			System.out.print(col +"\t");
		}
		System.out.println();
		
		for(SuspiciousCode row: testScoreTable.rowKeySet()){
			System.out.print(row.getClassName()+": "+row.getLineNumber()+")\t");
			for(String col:testScoreTable.columnKeySet()){
			 System.out.print(testScoreTable.get(row, col) + ";\t");
			}
			System.out.println();
		}
		
		System.out.println(" ========End table=======");*/
	}

	public List<String> getPriorityTests(ProgramVariant mutatedVariant) {		
		List<OperatorInstance> operationsList = mutatedVariant.getAllOperations();
		List<SuspiciousCode> listMPs = new ArrayList<>();
		for(OperatorInstance op:operationsList){
			ModificationPoint mp = op.getModificationPoint();
			if (mp instanceof SuspiciousModificationPoint){
				SuspiciousModificationPoint smp = (SuspiciousModificationPoint)mp;
				SuspiciousCode susCode = smp.getSuspicious();
				listMPs.add(susCode);				
			}			
		}	
		
		Map<String, Integer> finalTestScore = new HashMap<>();
		
		for(SuspiciousCode mp:listMPs){
			List<TestCaseResult> testCases = mp.getCoveredByTests();
			for(TestCaseResult tc:testCases){
				String testName = tc.getTestCaseCompleteName();
				int count;
				if (mppTable.contains(testName, mp))
					count = mppTable.get(testName, mp);
				else
					count = 0;					
				
				int totalCount;
				if (finalTestScore.containsKey(testName))
					totalCount = finalTestScore.get(testName);
				else
					totalCount = 0;
				
				totalCount = totalCount + count;
				
				finalTestScore.put(testName, totalCount);				
			}			
		}
		
		//Sort the test cases			
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		};		
		
		List<Entry<String, Integer>> sortedTCByCount = new ArrayList<>(finalTestScore.entrySet());		
		Collections.sort(sortedTCByCount, comparator);
		
		List<String> testCases = new ArrayList<>();
		for(int i=0;i<sortedTCByCount.size();i++)
			testCases.add(sortedTCByCount.get(i).getKey());
		return testCases;				
	}
	
	public List<String> getPriorityTests(ProgramVariant mutatedVariant, List<String> allRegressionTestCases) {
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		};	
				
		List<OperatorInstance> operationsList = mutatedVariant.getAllOperations();
		List<SuspiciousCode> listMPs = new ArrayList<>();
		List<String> effectedTestCases = new ArrayList<>();
		for(OperatorInstance op:operationsList){
			ModificationPoint mp = op.getModificationPoint();
			if (mp instanceof SuspiciousModificationPoint){
				SuspiciousModificationPoint smp = (SuspiciousModificationPoint)mp;
				SuspiciousCode susCode = smp.getSuspicious();
				listMPs.add(susCode);	
				List<TestCaseResult> testCases = susCode.getCoveredByTests();
				for(TestCaseResult tc:testCases)
					if (!effectedTestCases.contains(tc.getTestCaseCompleteName()))
						effectedTestCases.add(tc.getTestCaseCompleteName());						
			}
		}	
		List<String> prioritizedOutput = new ArrayList<>();
		
		//rank all the test cases
		Map<String, Integer> allMppTestScore = new HashMap<>();
		
		for(String tcName:allRegressionTestCases){			
			Map<SuspiciousCode, Integer> mapCount = mppTable.column(tcName);
			int sum= 0;
			if (mapCount!=null)						
				for(Integer count:mapCount.values()){
					sum += count;
				}
			allMppTestScore.put(tcName, sum);
		}
		
		//rank all test cases
		List<Entry<String, Integer>> sortedTCByCount = new ArrayList<>(allMppTestScore.entrySet());		
		Collections.sort(sortedTCByCount,comparator);		
		
		//generate output list
		List<String> orderedEffectedTCs= new ArrayList<>();
		List<String> orderedUneffectedTCs= new ArrayList<>();
		//  get effected test cases		
		for(int i=0;i<sortedTCByCount.size();i++){
			String tcName = sortedTCByCount.get(i).getKey();
			if (effectedTestCases.contains(tcName))
				orderedEffectedTCs.add(tcName);
			else
				orderedUneffectedTCs.add(tcName);			
		}
		List<String> output= new ArrayList<>();
		output.addAll(orderedEffectedTCs);
		output.addAll(orderedUneffectedTCs);
		return output;				
	}

	public List<Entry<String, Integer>> sortHashMapByValues(
	        Map<String,Integer> passedMap) {
		List<Entry<String, Integer>> result = new ArrayList<>(passedMap.entrySet());
		
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		};
		
		Collections.sort(result, comparator);
		
	    return result;
	}
	
	public void updateTheTable(TestResult finalResult, ProgramVariant mutatedVariant) {
		for(String orgTcName: finalResult.getFailures()){
			String normalizedTcName = StringUtils.substringBetween(orgTcName, "(", ")"); //TODO: convert the orgTcName to normalized. 
			for(OperatorInstance changeOperation: mutatedVariant.getAllOperations()){
				ModificationPoint mp = changeOperation.getModificationPoint();
				if (mp instanceof SuspiciousModificationPoint){					
					SuspiciousCode suspStmt = ((SuspiciousModificationPoint) mp).getSuspicious();
					int count = this.getKillCount(suspStmt, normalizedTcName);
					this.setKillCount(suspStmt,normalizedTcName,count+1);					
				}
			}
		}
	}

	private void setKillCount(SuspiciousCode suspStmt, String tc, int value) {		
		this.mppTable.put(suspStmt, tc, value);	
	}
	private int getKillCount(SuspiciousCode mp,String tc){		
		Integer count = this.mppTable.get(mp, tc);
		if (count==null)
			return 0;
		return count;
	}
	
}
