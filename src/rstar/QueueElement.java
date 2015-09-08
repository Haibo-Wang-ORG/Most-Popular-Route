package rstar;

public class QueueElement 
{
	public Node node;
	public Data point;	
	public String elementType;//RTDirNode, RTDataNode, Data
	public float mindist=0;
	
	public QueueElement(Node node)
	{
		this.node =  node;
		if(node.is_data_node()==false)
		{
			elementType = "RTDirNode";
		}
		else
		{
			elementType = "RTDataNode";
		}
	}
	
	public QueueElement(Data point)
	{
		this.point =  point;
		elementType = "Data";
	}
}
