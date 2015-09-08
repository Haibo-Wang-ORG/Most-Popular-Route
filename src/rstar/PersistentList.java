package rstar;
////////////////////////////////////////////////////////////////////////
// PersistentList
////////////////////////////////////////////////////////////////////////
import java.lang.*;
import java.io.*;
/**
 * Persistent link-list
 */
public class PersistentList extends LinList
{
//--------------------------------------------------------------------------
    /**
     * For testing purpose
     */
    public static void main(String argv[])
    {
        try
        {
            Object[] x = new Object[10];
            for (int i = 0; i < 10; ++i)
            {
                Data d = new Data(1);
                d.distanz = i;
                x[i] = d;
            }
            System.out.println("New list:");
            PersistentList list = new PersistentList();
            list.setTraceable(true);
            list.insert(x[1]);
            list.insert(x[3]);
            list.insert(x[2]);
            list.insert(x[2]);
            list.insert(x[9]);
            list.insert(x[5]);
            list.insert(x[4]);
            list.insert(x[6]);
            list.insert(x[8]);
            list.insert(x[7]);
            list.insert(x[3]);
            System.out.println(list);
            list.save("list.dat");
            System.out.println("saved...");
            PersistentList list2 = new PersistentList();
            list2.load("list.dat");
            System.out.println("loaded...");
            System.out.println(list2);
        }
        catch (Exception e)
        {
            System.out.println("ERROR:"+e);
        }
    }
//--------------------------------------------------------------------------
    /**
     * return file header size
     */
    protected int get_header_size() { return 0; }
    /**
     * input header from buffer
     */
    protected void read_header(byte[] buffer) {}
    /**
     * copy header to buffer
     */
    protected void write_header(byte[] buffer) {}
//--------------------------------------------------------------------------
    /**
     * return data header size
     */
    protected int get_data_header_size()
    {
        Streamable obj = (Streamable)get_first();
        return (obj != null) ? obj.get_data_header_size() : 0;
    }
//--------------------------------------------------------------------------
    /**
     * Save to file
     */
    public void save(String filename)
    {
        try // File operation
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            int header_size   = get_header_size();
            int num_elem      = get_num();
            int d_header_size = get_data_header_size();

            out.writeInt(header_size);   // Header size
            out.writeInt(num_elem);      // Number of elements
            out.writeInt(d_header_size); // Data header size
            // Write file header
            if (header_size > 0)
   					{
          		// Allocate file header
             	byte[] header = new byte[header_size];
          		this.write_header(header);  // fill in the header
              out.write(header);          // write the header
   					}
            for (Data d = (Data)get_first(); d != null; d = (Data)get_next())
            {
             // Write data size
             int d_size = d.get_size();
             out.writeInt(d_size);
             // Write data header
             if (d_header_size > 0)
    				 {
              // Allocate data header
              byte[] d_header = new byte[d_header_size];
     					d.write_data_header(d_header);  // fill in the data header
              out.write(d_header);            // write the data header
    				 }
             // Write actual data
             if (d_size > 0)
					   {
					     // Allocate data body
					     byte[] buffer = new byte[d_size];
					     d.write_to_buffer(buffer);      // fill in the data body
					     out.write(buffer);              // write the data body
					   }
            }
            out.close();
        }
        catch (IOException error)
        {
            System.out.println(error.getMessage());
        }
    }
//--------------------------------------------------------------------------
    /**
     * Load from file
     */
    public void load(String filename)
    {
        try // File operation
        {
            DataInputStream in = new DataInputStream(new FileInputStream(filename));
            int header_size   = in.readInt();   // Header size
            int num_elem      = in.readInt(); // Number of elements
            int d_header_size = in.readInt(); // Data header size
            // Read file header
            if (header_size > 0)
					  {
					    // Allocate file header
					    byte[] header = new byte[header_size];
					    in.read(header);
					    this.read_header(header);
					  }
            for (int i = 0; i < num_elem; i++)
            {
               // Create new element
	             Data d = new Data();
	             // Read data size
	             int d_size = in.readInt();
	             // Read data header
               if (d_header_size > 0)
    					 {
                 // Allocate data header
                 byte[] d_header = new byte[d_header_size];
                 in.read(d_header);  // read the data header
                 d.read_data_header(d_header);  // set the data header
    					 }

             // Read actual data
             if (d_size > 0)
    				 {
                // Allocate data body
     						byte[] buffer = new byte[d_size];
                in.read(buffer);            // read the data body
     						d.read_from_buffer(buffer); // set  the data body
    				 }
             // Insert the element, should be append() instead of insert()
             this.append(d);
            }
            in.close();
        }
        catch (IOException error)
        {
            System.out.println(error.getMessage());
        }
    }
//--------------------------------------------------------------------------
}