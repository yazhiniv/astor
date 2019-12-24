package fr.inria.astor.core.validation.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import fr.inria.astor.core.setup.ProjectConfiguration;

/**
 * This class runs a JUnit test suite i.e., a set of test cases.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class JUnitNologExternalExecutor extends JUnitExternalExecutor {

	@Override
	public String createOutput(Result r) {
		String out = "[";
		int nr_failures = 0;
		StringBuilder strBuilder = new StringBuilder();
		
		//ArrayList<String> strBuilder = new ArrayList<String>();
		try {
			for (Failure f : r.getFailures()) {
				String s = failureMessage(f);
				if (!s.startsWith("warning")) {
					nr_failures++;
					strBuilder.append(f.getDescription().getTestClass()+OUTSEP);
					
				}
				
			}
		} catch (Exception e) {
			// We do not care about this exception,
		}
		out = out + "]";
			
		String failingTCsString = strBuilder.toString();
		String outputString = OUTSEP + r.getRunCount() + OUTSEP + nr_failures + OUTSEP + "" + OUTSEP+failingTCsString+ OUTSEP ;
		
		//return (OUTSEP + r.getRunCount() + OUTSEP + nr_failures + OUTSEP + "" + OUTSEP);
		return outputString;
	}

	public static void main(String[] arg) throws Exception, InitializationError {

		JUnitNologExternalExecutor re = new JUnitNologExternalExecutor();

		Result result = re.run(arg);
		// This sysout is necessary for the communication between process...
		System.out.println(re.createOutput(result));

		System.exit(0);
	}

}
