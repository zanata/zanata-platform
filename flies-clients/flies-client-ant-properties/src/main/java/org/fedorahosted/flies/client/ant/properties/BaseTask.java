package org.fedorahosted.flies.client.ant.properties;

import org.apache.tools.ant.taskdefs.MatchingTask;

public abstract class BaseTask extends MatchingTask {
	Progress progress = Progress.NONE;

	enum Progress {
		NONE {
			@Override
			public void update(int currentIndex, int maxIndex) {}
			@Override
			void finished() {}},
		PERCENTAGE {
			@Override
			public void update(int currentIndex, int maxIndex) {
				int percent = (int)(100.0 * currentIndex / maxIndex);
				if(percent % 5 == 0 && percent != lastPercent) {
					System.err.print(percent);
					System.err.println('%');
					lastPercent = percent;
				}
			}
			@Override
			void finished() {
				System.err.println("100%");
			}
			},
		NUMBER {
			@Override
			public void update(int currentIndex, int maxIndex) {
				System.err.printf("(%d/%d)\n", currentIndex, maxIndex);
			}
			@Override
			void finished() {
				System.err.println("(finished)         ");
			}
			},
		;
		
			int lastPercent = -1;
			abstract void update(int currentIndex, int maxIndex);
			abstract void finished();
	}
	
	public void setProgressType(String type) {
		progress = Progress.valueOf(type.toUpperCase());
	}
}
