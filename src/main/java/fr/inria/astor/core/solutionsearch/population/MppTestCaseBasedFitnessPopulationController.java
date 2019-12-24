package fr.inria.astor.core.solutionsearch.population;

import fr.inria.astor.core.entities.ProgramVariant;

public class MppTestCaseBasedFitnessPopulationController extends TestCaseBasedFitnessPopulationController {
	
	public MppTestCaseBasedFitnessPopulationController() {
		this.comparator = new MppFitnessComparator();
	}
	
	/**
	 * Comparator to sort the variant in ascending mode according to the fitness
	 * values
	 * 
	 * @author Matias Martinez, matias.martinez@inria.fr
	 *
	 */
	public class MppFitnessComparator extends FitnessComparator{

		@Override
		public int compare(ProgramVariant o1, ProgramVariant o2) {
			int fitness = Double.compare(o2.getFitness(), o1.getFitness());
			if (fitness != 0)
				return fitness;
			// inversed, we prefer have child variant first
			return Integer.compare(o1.getId(), o2.getId());
		}

	}

}
