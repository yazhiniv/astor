package fr.inria.astor.core.solutionsearch.population;

import fr.inria.astor.core.entities.VariantValidationResult;
import fr.inria.astor.core.entities.TestCaseVariantValidationResult;

public class NewTestCaseFitnessFunction implements FitnessFunction{

	int totalTestCase;
	public void setTotalTestCase(int value){
		this.totalTestCase=value;
	}
	
	
	@Override
	public double calculateFitnessValue(VariantValidationResult validationResult) {
		
		if (validationResult == null)
			return this.getWorstMaxFitnessValue();
		
		TestCaseVariantValidationResult result = (TestCaseVariantValidationResult) validationResult;
		int fitnessvalue = (result.getCasesExecuted()/totalTestCase)-(result.getFailureCount()/result.getCasesExecuted());
		return fitnessvalue;
	}

	@Override
	public double getWorstMaxFitnessValue() {
		return Double.MIN_VALUE;
	}

}
