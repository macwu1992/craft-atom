package io.craft.atom.util.schedule;

import io.craft.atom.test.CaseCounter;
import io.craft.atom.util.schedule.ExpirationListener;
import io.craft.atom.util.schedule.TimingWheel;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindwind
 * @version 1.0, Sep 21, 2012
 */
public class TestTimingWheel {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(TestTimingWheel.class);
	
	
	private          TimingWheel<String> wheel    ;
	private volatile long                startTime;
	private volatile long                endTime  ;
	
	
	@Before
	public void before() {
		wheel = new TimingWheel<String>(1, 500, TimeUnit.MILLISECONDS);
		wheel.addExpirationListener(new TestExpirationListener());
		wheel.start();
	}
	
	@After
	public void after() {
		wheel.stop();
	}
	
	@Test
	public void testAdd() throws InterruptedException {
		startTime = System.currentTimeMillis();
		long ttl = wheel.add("test-0");
		LOG.debug("[CRAFT-ATOM-UTIL] Add object test-0 to timing wheel, will timeout after {} ms, start time={}", ttl, new Date(startTime));
		for (int i = 1; i <= 30; i++) {
			Thread.sleep(10);
			wheel.add("test-" + i);
		}
		Assert.assertEquals(31, wheel.size());
		System.out.println(String.format("[CRAFT-ATOM-UTIL] (^_^)  <%s>  Case -> test timing wheel add. ", CaseCounter.incr(1)));
	}
	
	@Test
	public void testRemove() throws InterruptedException {
		for (int i = 1; i <= 10; i++) {
			Thread.sleep(10);
			wheel.add("test-" + i);
		}
		Assert.assertEquals(10, wheel.size());
		
		wheel.remove("test-3");
		wheel.remove("test-4");
		wheel.remove("test-5");
		Assert.assertEquals(7, wheel.size());
		Set<String> set = wheel.elements();
		Assert.assertEquals(7, set.size());
		System.out.println(String.format("[CRAFT-ATOM-UTIL] (^_^)  <%s>  Case -> test timing wheel remove. ", CaseCounter.incr(3)));
	}
	
	@Test
	public void testAddTwice() throws InterruptedException {
		startTime = System.currentTimeMillis();
		long ttl = wheel.add("test-1");
		LOG.debug("[CRAFT-ATOM-UTIL] Add object test-1 to timing wheel, will timeout after {} ms, start time={}", ttl, new Date(startTime));
		
		Thread.sleep(20);
		ttl = wheel.add("test-1");
		LOG.debug("[CRAFT-ATOM-UTIL] Add object test-1 second to timing wheel, will timeout after {} ms, start time={}", ttl, new Date(startTime));
		Assert.assertEquals(1, wheel.size());
		System.out.println(String.format("[CRAFT-ATOM-UTIL] (^_^)  <%s>  Case -> test timing wheel add twice. ", CaseCounter.incr(1)));
	}
	
	@Test
	public void testExpire() throws InterruptedException {
		startTime = System.currentTimeMillis();
		long ttl = wheel.add("test-1");
		while(endTime == 0);
		long deviation = (endTime - startTime) - ttl;
		LOG.debug("[CRAFT-ATOM-UTIL] Timing wheel deviation={}", deviation);
		Assert.assertTrue(deviation <= 2);
		Assert.assertTrue(deviation >= -2);
		System.out.println(String.format("[CRAFT-ATOM-UTIL] (^_^)  <%s>  Case -> test timing wheel expire. ", CaseCounter.incr(2)));
	}
	
	private class TestExpirationListener implements ExpirationListener<String> {
		@Override
		public void expired(String expiredObject) {
			endTime = System.currentTimeMillis();
			LOG.debug("[CRAFT-ATOM-UTIL] Expired object={} end time={}", expiredObject, new Date(endTime));
		}
	}
}
