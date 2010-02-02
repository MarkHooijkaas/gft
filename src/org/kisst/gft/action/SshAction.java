package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.filetransfer.Ssh;
import org.kisst.gft.task.Task;
import org.kisst.util.FileUtil;
import org.kisst.util.StringUtil;

public class SshAction implements Action {
	
	private final Props actionProps;
	private final String commandTemplate;
	
	public SshAction(GftContainer gft, Props props) {
		this.actionProps=props;
		commandTemplate =FileUtil.loadString(props.getString("command"));
	}

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		SimpleProps props=new SimpleProps();
		props.put("action", actionProps);
		props.put("file", ft.file);
		props.put("channel", ft.channel.props);

		String command=StringUtil.substitute(commandTemplate, props);

		String result=Ssh.ssh(ft.channel.cred, ft.channel.getHost(), command);
		System.out.println(result);
		return null;
	}

}
