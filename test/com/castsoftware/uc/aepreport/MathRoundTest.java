package com.castsoftware.uc.aepreport;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathRoundTest {

	@Test
	public void test1() {
		int rounded = (int) Math.round(new Double(2.28));
		if (rounded != 2)
			fail("failed");
		
		rounded = (int) Math.round(new Double(-2.28));
		if (rounded != -2)
			fail("failed");

		rounded = (int) Math.round(new Double(2.5));
		if (rounded != 3)
			fail("failed");
		
	}

}
