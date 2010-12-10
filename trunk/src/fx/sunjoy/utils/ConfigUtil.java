package fx.sunjoy.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ConfigUtil {
	
	private boolean isValid = false;
	
	private int textPort = -1  ;
	private int thriftPort = -1 ;
	private String indexFilePath = null ;
	private int indexBlockSize = 128 ;
	private int mmapSize = 64 ;
	private String ReplicationRole = null ;
	private String masterSource = null ;
	
	public ConfigUtil(String configFilePath)
	{
		File configFile = new File(configFilePath) ;
		if(configFile.exists())
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(configFile);
				
				//��ȡ�˿ں�
				NodeList port = doc.getElementsByTagName("Port") ;
				if(port != null && port.getLength() > 0)
				{
					NodeList ports = port.item(0).getChildNodes() ;
					if(ports != null && ports.getLength() > 0)
					{
						for(int i = 0 ; i < ports.getLength(); i++)
						{
							String nodeName = ports.item(i).getNodeName() ;
							String value = ports.item(i).getTextContent().trim() ;
							if(value != null)
							{
								if(nodeName.equals("Memcache"))
								{
									textPort = Integer.valueOf(value) ;
								}
								else if(nodeName.equals("Thrift"))
								{
									thriftPort = Integer.valueOf(value) ; 
								}
							}
						}
					}
				}
				
				//��ȡIndex�ļ�λ�úͿ��С
				NodeList index = doc.getElementsByTagName("Index") ;
				if(index != null && index.getLength() > 0)
				{
					NodeList subParams = index.item(0).getChildNodes() ;
					if(subParams != null && subParams.getLength() > 0)
					{
						for(int i = 0; i < subParams.getLength(); i++)
						{
							String nodeName = subParams.item(i).getNodeName() ;
							String value = subParams.item(i).getTextContent().trim() ;
							if(value != null)
							{
								if(nodeName.equals("FilePath"))
								{
									indexFilePath = value ;
								}
								else if(nodeName.equals("BlockSize")) 
								{
									indexBlockSize = Integer.valueOf(value) ;
								}
							}
						}
					}
				}
				
				//��ȡMMap�Ĵ�С
				NodeList mmap = doc.getElementsByTagName("MMapSize") ;
				if(mmap != null && mmap.getLength() > 0)
				{
					mmapSize = Integer.valueOf(mmap.item(0).getTextContent().trim()) ;
				}
				
				//��ȡReplication����Ϣ
				NodeList replication = doc.getElementsByTagName("Replication") ;
				if(replication != null && replication.getLength() > 0)
				{
					NodeList subParams = replication.item(0).getChildNodes() ;
					if(subParams != null && subParams.getLength() > 0)
					{
						for(int i = 0; i < subParams.getLength(); i++)
						{
							String nodeName = subParams.item(i).getNodeName() ;
							String value = subParams.item(i).getTextContent().trim() ;
							if(value != null)
							{
								if(nodeName.equals("Role"))
								{
									ReplicationRole = value ;
								}
								else if(nodeName.equals("Source"))
								{
									masterSource = value ;
								}
							}
						}
					}
				}
				
				if(textPort > 0  && thriftPort > 0 &&  indexFilePath != null && 
						indexBlockSize > 0 && mmapSize > 0)
				{
					isValid = true ;
				}
				
				if(ReplicationRole != null && ReplicationRole.equalsIgnoreCase("Slave"))
				{
					if(masterSource == null || masterSource.length() == 0)
					{
						isValid = false ;
					}
				}
				
			} 
			catch (Exception e)
			{
				e.printStackTrace() ;
				return ;
			}
			
		}
	}
	
	public boolean isValidConfigFile()
	{
		return isValid ;
	}

	public int getTextPort() {
		return textPort;
	}


	public int getThriftPort() {
		return thriftPort;
	}


	public String getIndexFilePath() {
		return indexFilePath;
	}


	public int getIndexBlockSize() {
		return indexBlockSize;
	}


	public int getMmapSize() {
		return mmapSize;
	}

	public String getReplicationRole() {
		return ReplicationRole;
	}

	public void setReplicationRole(String replicationRole) {
		ReplicationRole = replicationRole;
	}

	public String getMasterSource() {
		return masterSource;
	}

	public void setMasterSource(String masterSource) {
		this.masterSource = masterSource;
	}


}
