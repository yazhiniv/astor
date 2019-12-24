package fr.inria.astor.approaches.mpp;

import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import example.Test_GZoltarFaultLocalization;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.population.MppTestCaseBasedFitnessPopulationController;
import fr.inria.astor.core.solutionsearch.population.NewTestCaseFitnessFunction;
import fr.inria.astor.core.validation.ProgramVariantValidator;

public class MppEngine extends JGenProg{

	
	//private ProgramVariantValidator validator; //your new

	public MppEngine(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
		super(mutatorExecutor, projFacade);		
	}
	
	@Override
	public void initPopulation(List<SuspiciousCode> suspicious) throws Exception {
		if (this.getProgramValidator() instanceof MppNewValidator){
			MppNewValidator validator = (MppNewValidator) this.getProgramValidator();
			validator.initializeTheMPPTable(suspicious);
		}
		if (this.getFitnessFunction() instanceof NewTestCaseFitnessFunction){
			int totalNumberTestCase = this.projectFacade.getProperties().getRegressionTestCases().size();
			NewTestCaseFitnessFunction function = (NewTestCaseFitnessFunction) this.getFitnessFunction();
			function.setTotalTestCase(totalNumberTestCase);
		}
		
		super.initPopulation(suspicious);		
	}
	
	@Override
	protected void loadValidator() throws Exception {
		ProgramVariantValidator validator = new MppNewValidator();
		this.setProgramValidator(validator);
	}
	
	@Override
	protected void loadFitnessFunction() throws Exception {
		NewTestCaseFitnessFunction fitnessFun = new NewTestCaseFitnessFunction();
		this.setFitnessFunction(fitnessFun);
	}
	
	@Override
	protected void loadPopulation() throws Exception {
		MppTestCaseBasedFitnessPopulationController popController = new MppTestCaseBasedFitnessPopulationController();
		this.setPopulationControler(popController);
	}
}
