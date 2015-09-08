package rstar;
////////////////////////////////////////////////////////////////////////////
//
// Data
////////////////////////////////////////////////////////////////////////////
//
import java.lang.*;
import java.io.*;
/**
* Data
*/
public class Data implements Comparable, Streamable
{
  static final int sizeof_dimension = 4;
  static final int sizeof_float     = 4;
  static final int sizeof_int       = 4;/// i add it

  public  float[] data      = null;   // Vector
  public  float   distanz   = 0;      // anything

  public int dimension = 0;      // array size of data[]
  public int id = 0;
  
  public int trajectoryID = 0;
  public int pointID = 0;
  public float direction = 0f;

//--------------------------------------------------------------------------
  /**
  * Default Constructor
  */
  public Data()
  {
    this.setDimension(Constants.RTDataNode__dimension);
  }
  /**
  * Constructor accept an int _dimension
  */
  public Data(int _dimension)
  {
    this.setDimension(_dimension);
  }
  
  public Data(int _dimension, int _id)
  {
    this.setDimension(_dimension);
    this.id = _id;
  }

  public Data(int _dimension, int _id, int trajectoryID, int pointID, float direction)
  {
    this.setDimension(_dimension);
    this.id = _id;
    this.trajectoryID = trajectoryID;
    this.pointID = pointID;
    this.direction = direction;
  }
  /**
  * Constructor accept a byte array representing dimension
  */
  public Data(byte[] bytes)
  {
    try
    {
      this.read_data_header(bytes);
    }
    catch(IOException e)
    {
      System.out.println(e.getMessage());
    }
  }

//--------------------------------------------------------------------------
  /**
  * Constructor accept a byte array representing dimension
  */
  protected void setDimension(int _dimension)
  {
    if (_dimension <= 0) _dimension = 1;
    this.dimension = _dimension;
    //this.data = new float[_dimension];
    this.data = new float[_dimension*2];
  }

//--------------------------------------------------------------------------
  /**
  * returns MBR (Minimum Bounding Rect) of the object
  */
  public float[] get_mbr()
  {
    // fuer Punktdaten trivial: untere_grenze == obere_grenze
    /*float[] f = new float[2 * this.dimension];
    for (int i = 0; i < this.dimension; i++)
    	f[2*i] = f[2*i+1] = this.data[i];
    return f;*/

    float[] f = new float[2 * this.dimension];
    System.arraycopy(data, 0, f, 0, 2*dimension);
    return f;
  }

//--------------------------------------------------------------------------
  /**
  * returns the area of the MBR, for vectors always 0.0
  */
  public float get_area()
  {
    return 0;
  }

//--------------------------------------------------------------------------
  /**
  * Implement Streamable
  * return data header size
  */
  public int get_data_header_size()
  {
    return sizeof_dimension;
  }
  /**
  * Implement Streamable
  * fill buffer with my data header
  */
  public void read_data_header(byte[] buffer) throws IOException
  {
    ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
    DataInputStream in = new DataInputStream(byte_in);
    int _dimension = in.readInt();
    this.setDimension(_dimension);
    in.close();
    byte_in.close();
  }
  /**
  * Implement Streamable
  * fill buffer with my data header
  */
  public void write_data_header(byte[] buffer) throws IOException
  {
    ByteArrayOutputStream byte_out = new ByteArrayOutputStream(buffer.length);
    DataOutputStream out = new DataOutputStream(byte_out);
    out.writeInt(this.dimension);
    byte[] bytes = byte_out.toByteArray();
    for (int i = 0; i < buffer.length; ++i)
    buffer[i] = bytes[i];
    out.close();
    byte_out.close();
  }

//----------------------------------------------------------------------------
  /**
  * Implement Streamable
  * returns amount of needed space in bytes
  */
  public int get_size()
  {
	//return this.dimension * 2 * sizeof_float + sizeof_dimension;
    return this.dimension * 2 * sizeof_float + sizeof_dimension + 2* sizeof_int
           + sizeof_float;
  }

  /**
  * Implement Streamable
  * reads data from buffer
  */
  public void read_from_buffer(byte[] buffer) throws IOException
	{
        ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
        DataInputStream in = new DataInputStream(byte_in);

        read_from_buffer(in);

        in.close();
        byte_in.close();
 	}
	/**
	 * Implement Streamable
	 * fill buffer with my content
	 */
    public void write_to_buffer(byte[] buffer) throws IOException
    {
        ByteArrayOutputStream byte_out = new ByteArrayOutputStream(buffer.length);
        DataOutputStream out = new DataOutputStream(byte_out);

        write_to_buffer(out);

        byte[] bytes = byte_out.toByteArray();
        for (int i = 0; i < buffer.length; ++i)
        buffer[i] = bytes[i];

        out.close();
        byte_out.close();
    }

	/**
  * The following i/o functions are used by RTDataNode
  * to read/write data immediately to a stream
	*/
  public void read_from_buffer(DataInputStream in) throws IOException
  {
	  //for (int i = 0; i < this.dimension; ++i)
      for (int i = 0; i < this.dimension*2; ++i)
    	this.data[i] = in.readFloat();
      this.distanz = in.readFloat();
      this.trajectoryID = in.readInt();
      this.pointID = in.readInt();
      this.direction = in.readFloat();
  }

  public void write_to_buffer(DataOutputStream out) throws IOException
  {
    //for (int i = 0; i < this.dimension; ++i)
	for (int i = 0; i < this.dimension*2; ++i)
    {
    	out.writeFloat(this.data[i]);
    }
    out.writeFloat(this.distanz);
    
    ////////
    out.writeInt(this.trajectoryID);
    out.writeInt(this.pointID);
    out.writeFloat(this.direction);
  }

//--------------------------------------------------------------------------
  /**
  * print out all data
  */
  public void print()
  {
    System.out.print("[id: <"+this.id+"> ");
    if (this.dimension > 0)
        System.out.print(this.data[0]);
    for (int i = 1; i < this.dimension*2; i++)
        System.out.print(" "+this.data[i]);
    System.out.println("]");
  }
  /**
  * Implements Object.toString()
  */
  public String toString()
  {
    String answer = this.getClass().getName();
    answer = answer+"(distanz="+this.distanz+","+this.dimension+":[";
    if (this.dimension > 0)
    answer = answer+this.data[0];
    // for (int i = 1; i < this.dimension; ++i)
    for (int i = 1; i < this.dimension*2; ++i)
        answer = answer+" "+this.data[i];
    answer = answer+"])";
    return answer;
  }

//--------------------------------------------------------------------------
  /**
  * Implements the Comparable interface
  */
  public int compare(Object obj)
  {
    if (! (obj instanceof Data))
    return 1; //??? return arbitary value saying that not equal
    Data other = (Data)obj;
    if (this.distanz > other.distanz) return  1;
    if (this.distanz < other.distanz) return -1;
    return 0;
  }
  /**
  * Override Object.equals(...)
  */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof Data))
    return false;
    Data other = (Data)obj;
    if (this.dimension == other.dimension
    && this.distanz   == other.distanz)
    {
      for (int i = 0; i < this.data.length; ++i)
      if (this.data[i] != other.data[i])
      return false;
      return true;
    }
    return false;
  }

//--------------------------------------------------------------------------
  /**
  * set this to other.clone()
  */
  public Data assign(Data other)
  {
    this.setDimension(other.dimension);
    this.distanz   = other.distanz;
    //for (int i = 0; i < this.dimension; ++i)
    for (int i = 0; i < this.dimension*2; ++i)
        this.data[i] = other.data[i];
    this.id = other.id;
    
    //
    this.trajectoryID = other.trajectoryID;
    this.pointID = other.pointID;
    this.direction = other.direction;
    //
    
    return this;
  }
  /**
  * Override Object.clone()
  */
  public Object clone()
  {
    Data d = new Data(this.dimension);
    d.distanz = this.distanz;
    //for (int i = 0; i < this.dimension; ++i)
    for (int i = 0; i < this.dimension*2; ++i)
        d.data[i] = this.data[i];
    d.id = this.id;
    
    //
    d.trajectoryID = this.trajectoryID;
    d.pointID = this.pointID;
    d.direction = this.direction;
    //
    
    return (Object)d;
  }
  
  //-------------------------------------------------------------------------
  /**
  * For testing purpose
  */
  public static void main(String argv[])
  {
    try
    {
      Data e = new Data(0);
      Object[] x = new Object[10];
      for (int i = 0; i < 10; ++i)
      {
        Data d = new Data(1);
        d.distanz = (float)i;
        x[i] = d;
      }
    }
    catch (Exception e)
    {
      System.out.println("ERROR:"+e.getMessage());
    }
  }

}