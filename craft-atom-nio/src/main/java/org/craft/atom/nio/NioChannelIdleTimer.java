package org.craft.atom.nio;

import java.util.concurrent.TimeUnit;

import org.craft.atom.io.ChannelEventType;
import org.craft.atom.io.IoHandler;
import org.craft.atom.nio.spi.NioChannelEventDispatcher;
import org.craft.atom.util.schedule.ExpirationListener;
import org.craft.atom.util.schedule.TimingWheel;

/**
 * Nio channel idle timer
 * 
 * @author mindwind
 * @version 1.0, Feb 27, 2013
 */
public class NioChannelIdleTimer {
	
	private static final NioChannelIdleTimer INSTANCE = new NioChannelIdleTimer();
	private TimingWheel<NioByteChannel> timingWheel;
	private NioChannelEventDispatcher dispatcher;
	private IoHandler handler;
	private int timeoutInMillis;
	
	public static NioChannelIdleTimer getInstance() {
		return INSTANCE;
	}
	
	void init(NioChannelEventDispatcher dispatcher, IoHandler handler, int timeoutInMillis) {
		this.dispatcher = dispatcher;
		this.handler = handler;
		this.timeoutInMillis = timeoutInMillis;
		this.timingWheel = new TimingWheel<NioByteChannel>(1000, timeoutInMillis / 1000, TimeUnit.MILLISECONDS);
		this.timingWheel.addExpirationListener(new NioChannelIdleListener());
		this.timingWheel.start();
	}
	
	void add(NioByteChannel channel) {
		timingWheel.add(channel);
	}
	
	void remove(NioByteChannel channel) {
		timingWheel.remove(channel);
	}
	
	private void fireChannelIdle(NioByteChannel channel) {
    	dispatcher.dispatch(new NioByteChannelEvent(ChannelEventType.CHANNEL_IDLE, channel, handler));
    }
	
	// ~ --------------------------------------------------------------------------------------------------------
	
	private class NioChannelIdleListener implements ExpirationListener<NioByteChannel> {

		@Override
		public void expired(NioByteChannel channel) {
			long now = System.currentTimeMillis();
			long elapse = now - channel.getLastIoTime();
			if (elapse > timeoutInMillis) {
				fireChannelIdle(channel);
			}
			timingWheel.add(channel);
		}
		
	}
	
}