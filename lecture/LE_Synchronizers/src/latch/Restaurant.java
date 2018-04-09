package latch;

import java.util.concurrent.CountDownLatch;

public class Restaurant {
	
	public static void main(String[] args) {
		int nrGuests = 2;

		final CountDownLatch cookedLatch = new CountDownLatch(1);
		final CountDownLatch washLatch = new CountDownLatch(nrGuests);
				
		new Cook(cookedLatch).start();
		
		for(int i = 0; i < nrGuests; i++) {
			new Guest(cookedLatch, washLatch).start();
		}
		
		new DishWasher(washLatch).start();
	}
	
	
	static class Cook extends Thread {
		private final CountDownLatch latch;
		public Cook(CountDownLatch l) {
			latch = l;
		}
		
		@Override
		public void run() {
			System.out.println("Start Cooking..");
			try {
				sleep(5000);
			} catch (InterruptedException e) {}
			System.out.println("Meal is ready");
			latch.countDown();
		}
	}
	
	
	static class Guest extends Thread {
		final private CountDownLatch cookedLatch;
		final private CountDownLatch doneLatch;

		public Guest(CountDownLatch cl, CountDownLatch dl) {
			cookedLatch = cl;
			doneLatch = dl;
		}
		
		@Override
		public void run() {
			try {
				sleep(1000);
				System.out.println("Entering restaurant and placing order.");
				cookedLatch.await();
				System.out.println("Enjoying meal.");
				sleep(5000);
				System.out.println("Meal was excellent!");
				doneLatch.countDown();
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class DishWasher extends Thread {
		private final CountDownLatch latch;
		public DishWasher(CountDownLatch l) {
			latch = l;
		}
		
		@Override
		public void run() {
			try {
				System.out.println("Waiting for dirty dishes.");
				latch.await();
				System.out.println("Washing dishes.");
				sleep(0);
			} catch (InterruptedException e) {}
		}
	}
}
