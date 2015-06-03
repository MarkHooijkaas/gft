/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.gft.filetransfer.action;

import java.io.InputStream;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.ssh.SshFileServerConnection;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class SftpGetPutAction extends BaseAction {
	private final static Logger logger=LoggerFactory.getLogger(SftpGetPutAction.class);

	private final boolean resumeAllowed;
	public SftpGetPutAction(Props props) { 
		super(props);
		this.resumeAllowed=props.getBoolean("resumeAllowed", false);
	}

	@Override public String toString() { return SftpGetPutAction.class.getSimpleName()+"(resumeAllowed="+resumeAllowed+")"; } 

	public Object execute(Task task) {
		FileLocation src= ((SourceFile) task).getSourceFile();
		FileLocation dest = ((DestinationFile) task).getDestinationFile();

		int mode=ChannelSftp.OVERWRITE;
		if(resumeAllowed)
			mode=ChannelSftp.RESUME;

		SshFileServerConnection destfsconn=null;
		SshFileServerConnection srcfsconn=(SshFileServerConnection) src.getFileServer().openConnection();
		try {
			destfsconn=(SshFileServerConnection) dest.getFileServer().openConnection();
			ChannelSftp srcchan = srcfsconn.getSftpChannel();
			ChannelSftp destchan = destfsconn.getSftpChannel();
			logger.info("opening sftp-get stream from {}",src.toString());
			InputStream inp = srcchan.get(src.getFullPath());
			logger.info("opening sftp-put stream to {}",dest.toString());
			destchan.put(inp, dest.getFullPath(), mode);
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
		finally {
			if (srcfsconn!=null)
				srcfsconn.close();
			if (destfsconn!=null)
				destfsconn.close();
		}

		return null;
	}

}
