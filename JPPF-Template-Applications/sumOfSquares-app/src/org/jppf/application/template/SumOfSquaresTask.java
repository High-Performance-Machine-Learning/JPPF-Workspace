package org.jppf.application.template;
import org.jppf.node.protocol.AbstractTask;

/**
 *  Task for generating the sum of squares for ints within a range
 * @param <Int>
 */
public class SumOfSquaresTask extends AbstractTask<Object> {
  private static final long serialVersionUID = 1L;
  int from;
  int range;

  public SumOfSquaresTask(int from, int range) {
    this.from = from;
    this.range = range;
  }

  @Override
  public void run() {
	int square = 0;
	
	// display info of job, node and thread
	String jobName = this.getJob().getName();
	String nodeID = this.getNode().getUuid();
	String threadName = Thread.currentThread().getName();
	System.out.println(jobName + " executing in Node[" + nodeID + "] utilising Thread[" + threadName + "]");
	
	for(int i = from; i < from + range; i++) {
		square += i * i;
	}
	
	// randomly sleep some tasks for between 0 and 3 seconds
	// done to verify parallelism
	try {
		Thread.sleep(1000L * (int) (Math.random() * 3));
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	
	setResult(square);
	System.out.println(jobName + " finished executing.");
  }
}
