package com.kagkarlsson.scheduler;

import java.time.LocalDateTime;

public class SettableClock implements Clock {

	private LocalDateTime now = LocalDateTime.now();

	@Override
	public LocalDateTime now() {
		return now;
	}

	public void set(LocalDateTime newNow) {
		this.now = newNow;
	}
}