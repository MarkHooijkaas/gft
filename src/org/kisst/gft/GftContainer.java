package org.kisst.gft;

import org.kisst.gft.poller.QueuePoller;
import org.kisst.gft.task.TaskQueue;
import org.kisst.gft.task.file.FileSystem;
import org.kisst.gft.tasks.EchoHandler;

public class GftContainer {
	FileSystem fs=new FileSystem("testdata");
	TaskQueue incoming=fs.getQueue("incoming");
	QueuePoller poller=new QueuePoller(incoming, new EchoHandler());
	
	public void run() {
		while (true) {
			//System.out.println("polling");
			poller.pollTillEmpty();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		new GftContainer().run();
	}
}
