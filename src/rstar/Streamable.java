package rstar;
import java.io.*;
/**
 * Provide persistence to Object
 */
public interface Streamable
{
//--------------------------------------------------------------------------
    //Data Header
    abstract public int  get_data_header_size();
    abstract public void read_data_header (byte[] input_buffer)  throws IOException;
 abstract public void read_from_buffer (byte[] input_buffer)  throws IOException;
//--------------------------------------------------------------------------
    //Data Body
    abstract public int  get_size();
 abstract public void write_data_header(byte[] output_buffer) throws IOException;
    abstract public void write_to_buffer  (byte[] output_buffer) throws IOException;
//--------------------------------------------------------------------------
}