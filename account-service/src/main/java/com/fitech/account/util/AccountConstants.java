package com.fitech.account.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yan on 2017/6/21.
 */
public class AccountConstants {
    public static Set<Long> runningSet = Collections.synchronizedSet(new HashSet<Long>());
    
    public static Boolean isRunning(Long reportId){	
		return runningSet.contains(reportId);	
    }
    
    public static void add(Long reportId){	
		 runningSet.add(reportId);
    }
    
    public static void remove(Long reportId){	
		 runningSet.remove(reportId);
    }
}
