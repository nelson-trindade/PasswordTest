package nelsonapps.demos.db1.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nelsonapps.demos.db1.service.PasswordMeter;

public class PasswordMeterTest {

	
	@Test
	public void testMiddleNumber(){
		PasswordMeter passwordMeter = new PasswordMeter("AF7LXS");
		int score = passwordMeter.scorePassword();
		assertEquals(26,score);
	}
	
	@Test
	public void onlyLetters(){
		PasswordMeter passwordMeter = new PasswordMeter("aKo");
		int score = passwordMeter.scorePassword();
		assertEquals(15,score);
	}
	
	@Test
	public void onlyNumbers(){
		PasswordMeter passwordMeter = new PasswordMeter("952");
		int score = passwordMeter.scorePassword();
		assertEquals(7, score);
	}
}
