//  Copyright (c) 2010-2011, Ioannis (Yiannis) Chatzikonstantinou, ?All rights reserved.
//  http://www.volatileprototypes.com
// 
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
//  	- Redistributions of source code must retain the above copyright 
//  notice, this list of conditions and the following disclaimer.
//  	- Redistributions in binary form must reproduce the above copyright 
//  notice, this list of conditions and the following disclaimer in the documentation 
//  and/or other materials provided with the distribution.
//  
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
//  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
//  OF SUCH DAMAGE.

// Generic Solver class. Does not implement any specific solver function, but lays the
// framework for parallel processing as well implements a few useful functions.
// All solver classes are children of this one.

package volatileprototypes.fvlib;

import processing.core.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author      Yannis Chatzikonstantinou <contact@volatileprototypes.com>
 * @version     0.5.10                                    
 * @since       0.4.0          
 */
public class Solver {

  private Collection<Caller> cr; 	// Pre-caching of solver
  protected static ExecutorService xs;		// Executor Service

  // Constructor.
  public Solver() {
    int numCPUs=getNumCPUs();
    cr = new ArrayList<Caller>(numCPUs);
    for (int i=0; i<numCPUs; i++) {
		cr.add(new Caller(numCPUs,i));
	  }
    if (xs == null) {
      xs = Executors.newFixedThreadPool(numCPUs);
    }
  }
  
  // Utility method to get number of CPUs
  // Can be overridden by inheriting class
  // to enforce a specific return value.
  protected int getNumCPUs() {
        Runtime runtime = Runtime.getRuntime();
        return(Math.max(runtime.availableProcessors(),1));      
  }
  
  // Called to step the simulation by one iteration.
  // Uses executorService.invokeAll, which returns as soon as every thread
  // has finished processing.
  // step --> Caller(s) --> stepFunction
  public final void step() {
    try {
        xs.invokeAll(cr);
    } catch (InterruptedException ignore) {}
    finalizeFunction();
  }
  
  // Same but steps all instances of this class by one iteration.
  //public final void stepAll() {
  //  try {
  //      xs.invokeAll(cr);
  //  } catch (InterruptedException ignore) {}
  //}
  
  protected void stepFunction(int step, int offset) {}

  protected void finalizeFunction() {}
  
  // Inner class that calls the implemented solver function.
  // Is used during step()  to allow parallel execution (calling of the function many times
  // with different offset, step params).
  protected final class Caller implements Callable<Object> {
  
    private final int step, offset;
  
    public Caller(int stepin, int offsetin) {
    	step=stepin;
    	offset=offsetin;
    }
  
    public Object call() {
    	stepFunction(step,offset);
    	return(null);
    }
  }
  
}