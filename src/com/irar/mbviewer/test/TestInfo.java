package com.irar.mbviewer.test;

public class TestInfo {
	
	public final int totalTests, testsPassed, testsFailed;
	
	public TestInfo(int total, int passed, int failed) {
		this.totalTests = total;
		this.testsPassed = passed;
		this.testsFailed = failed;
	}
	
	@Override
	public String toString() {
		return "Passed " + testsPassed + " of " + totalTests + "; " + testsFailed + " failed.";
	}

}
