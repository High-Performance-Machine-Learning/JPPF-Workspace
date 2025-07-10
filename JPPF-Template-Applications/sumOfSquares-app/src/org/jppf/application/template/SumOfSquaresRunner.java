/*
 * JPPF.
 * Copyright (C) 2005-2019 JPPF Team.
 * http://www.jppf.org
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
package org.jppf.application.template;

import java.util.ArrayList;
import java.util.List;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFConnectionPool;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.Task;
import org.jppf.utils.Operator;

/**
 * A runner to pair with org.jppf.application.template.SumOfSquaresTask
 * Demonstrating multiple jobs in parallel, not a perfect SumOfSquares implementation
 */
public class SumOfSquaresRunner {

	public static void main(final String...args) {
		int sumOfSquares = 0;
		int slowRes = 0;
		
		try (final JPPFClient jppfClient = new JPPFClient()) {
			// create a runner instance.
			final SumOfSquaresRunner runner = new SumOfSquaresRunner();
			  
			// submit our async jobs
			List<JPPFJob> jobList = runner.calculateSumOfSquaresAsync(jppfClient, 100, 10);
			
			// submit full calculation to one job
			JPPFJob slowJob = runner.createSumOfSquaresJob(0, 100);
			jppfClient.submitAsync(slowJob);
			
			// process the results
			try {
				sumOfSquares += runner.processResults(jobList);
				slowRes += (int) slowJob.awaitResults().get(0).getResult() + 10 * 10;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// finalise with the last result
			sumOfSquares += 10 * 10;
			System.out.println("Sum Of Squares [100]: " + sumOfSquares);
			System.out.println("Slow Result [100]: " + slowRes);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a new JPPFJob loaded with SumOfSquaresTask
	 * @param from, start point
	 * @param range, how many ints
	 * @return job, the new job object
	 * @throws Exception
	 */
	public JPPFJob createSumOfSquaresJob(final int from, final int range) throws Exception {
		final JPPFJob job = new JPPFJob();
		job.setName("Squares Task [" + from + "," + (from + range - 1) +"]");
		job.add(new SumOfSquaresTask(from, range));		
		return job;
	}
  
	public List<JPPFJob> calculateSumOfSquaresAsync(JPPFClient client, int to, int numJobs) {
	    final List<JPPFJob> jobList = new ArrayList<>(numJobs);
	    int range = (int) Math.ceil(to / numJobs);
	    
	    // creating jobs
	    for(int i=0; i < numJobs; i++) {
	    	JPPFJob job = null;
	    	
			try {
				job = createSumOfSquaresJob(i * (to / numJobs), range);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// jobs need to be submitted as async objects
	    	client.submitAsync(job);
	        jobList.add(job);
		}
	    
	    System.out.println("jobs are executing ...");
	    return jobList;
	}

	private int processResults(final List<JPPFJob> jobList) {
		// wait until the jobs are finished and process their results.
	    int result = 0;
	    for (final JPPFJob job: jobList) {
	      // wait if necessary for the job to complete and collect its results
	      final List<Task<?>> results = job.awaitResults();
	      
	      // jobs could contain multiple tasks
	      for (final Task<?> task: results) {
	          if (task.getThrowable() != null) {
	            System.out.println(job.getName() + ", an exception was raised: " + task.getThrowable ().getMessage());
	          } else {
	            System.out.println(job.getName() + ", execution result: " + task.getResult());
	            result += (int) task.getResult();
	          }
	        }
	    }
	    
	    return result;
	}
}
