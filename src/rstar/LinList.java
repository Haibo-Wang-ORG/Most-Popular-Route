package rstar;
////////////////////////////////////////////////////////////////////////
// LinList
////////////////////////////////////////////////////////////////////////
import java.lang.*;
import java.io.*;
/**
 * Double link-list
 */
public class LinList implements Traceable
{
		//------------------------------------------------------------------------
		/**
     * For testing purpose
     */
    public static void main(String argv[])
    {
        try
        {
            Object[] x = new Object[10];
            for (int i = 0; i < 10; ++i)
                x[i] = new Integer(i);
            System.out.println("New list:");
            LinList list = new LinList();
            list.setTraceable(true);
            list.printOn(System.out);
            System.out.println("Insert "+x[1]+":");
            list.insert(x[1]);
            list.printOn(System.out);
            System.out.println("Insert "+x[3]+":");
            list.insert(x[3]);
            list.printOn(System.out);
            System.out.println("Insert "+x[2]+":");
            list.insert(x[2]);
            list.printOn(System.out);
            System.out.println("Insert "+x[2]+":");
            list.insert(x[2]);
            list.printOn(System.out);
            System.out.println("Append "+x[9]+":");
            list.append(x[9]);
            list.printOn(System.out);
            System.out.println("Insert "+x[5]+":");
            list.insert(x[5]);
            list.printOn(System.out);
            System.out.println("erase myFirst:");
            list.get_first();
            list.erase();
            list.printOn(System.out);
            System.out.println("erase myLast:");
            list.get_last();
            list.erase();
            list.printOn(System.out);
            System.out.println("erase "+1+"-th:");
            list.get(1);
            list.erase();
            list.printOn(System.out);
            System.out.println("Insert "+x[4]+":");
            list.insert(x[4]);
            list.printOn(System.out);
            System.out.println("Insert "+x[6]+":");
            list.insert(x[6]);
            list.printOn(System.out);
            System.out.println("erase "+0+"-th:");
            list.get(0);
            list.erase();
            list.printOn(System.out);
        }
        catch (Exception e)
        {
            System.out.println("ERROR:"+e.getMessage());
        }
    }
//--------------------------------------------------------------------------
    protected int   myCount         = 0;    // Total number of elements
    protected SLink myFirst         = null; // pointer to the First element
    protected SLink myLast          = null; // pointer to the Last  element
    protected SLink myCurrent       = null; // pointer to current (cached) element
    protected int   myCurrent_index = -1;   // index (0..count-1) of current element
                                            //   -1 means undefined index
    protected Class myElementClass  = null; // Class of the element
//--------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public LinList()
    {
        try
        {
            this.myElementClass = Class.forName("Object");
        }
        catch (ClassNotFoundException e)
        {
            //System.out.println(e);
        }
    }
    /**
     * Constructor
     */
    public LinList(Class elementClass) { this.myElementClass = elementClass; }
//--------------------------------------------------------------------------
    /**
     * Print out the elements on stream
     */
    public void printOn(PrintStream out)
    {
        out.println(this.getClass().getName()+":");
        out.println("  check         = "+(this.check() ? "OK" : "Fail"));
        out.println("  count         = "+this.get_num());
        out.print  ("  enum get(i)   = [");
        for (int i = 0; i < this.get_num(); ++i)
            out.print(this.get(i)+" ");
        out.println("]");
        out.print  ("  enum get_next = [");
        for (Object obj = this.get_first(); obj != null; obj = this.get_next())
            out.print(obj+" ");
        out.println("]");

        out.print  ("  enum get_prev = [");
        for (Object obj = this.get_last(); obj != null; obj = this.get_prev())
            out.print(obj+" ");
        out.println("]");
        out.println("  toString()    = "+this.toString());
        out.println();
    }
//--------------------------------------------------------------------------
    /**
     * Print out the elements on stream
     */
    public String toString()
    {
        String answer = this.getClass().getName() + "(\n";
        for (Object obj = this.get_first(); obj != null; obj = this.get_next())
            answer = answer + obj + "\n";
        answer = answer + ")";
        return answer;
    }
//--------------------------------------------------------------------------
    /**
     * Return the total number of elements
     */
    public int get_num() { return this.myCount; }
//--------------------------------------------------------------------------
    /**
     * Insert an object to begining of list
     */
    public void insert(Object obj)
    {
        //            *----*-next-> *-------*---------------
        //            | sd |        |myFirst|...............
        //null <-prev-*----* <-prev-*-------*---------------
        // allocate new Storage for object
        SLink sd = new SLink(obj);
        // Establish the links
        sd.next = this.myFirst;
        sd.prev = null;
        if (this.myFirst != null)
         this.myFirst.prev = sd;
        // adjust myFirst, myLast and myCount;
        ++this.myCount;
        this.myFirst = sd;
        if (this.myLast == null) //original list is empty
        {
            //for empty list, this.myFirst == this.myLast
         this.myLast = sd;
        }
        // reset cuurent position and index
        this.myCurrent = null;
        this.myCurrent_index = -1;
        if (this.isTraceable())
        {
            System.out.println("LinList.insert("+obj+"): inserted to position first.");
            this.check();
        }
    }
//--------------------------------------------------------------------------
    /**
     * Insert an object to end of list
     */
    public void append(Object obj)
    {
        //            *------*------*-next-> *----*-next-> null
        //            |......|myLast|        | sd |
        //null <-prev-*------*------* <-prev-*----*
        // allocate new Storage for object
        SLink sd = new SLink(obj);
        // Establish the links
        sd.next = null;
        sd.prev = this.myLast;
        if (this.myLast != null)
         this.myLast.next = sd;
        // adjust myFirst, myLast and myCount;
        ++this.myCount;
        this.myLast = sd;
        if (this.myFirst == null) //original list is empty
        {
            //for empty list, this.myFirst == this.myLast
         this.myFirst = sd;
        }
        // reset cuurent position and index
        this.myCurrent = null;
        this.myCurrent_index = -1;
        if (this.isTraceable())
        {
            System.out.println("LinList.append("+obj+"): inserted to position last.");
            this.check();
        }
    }
    /**
     * Erase an Object from the myCurrent position
     * Return:
     *   true  if successful
     *   false if no myCurrent object
     */
    public boolean erase()
    {
        int   original_index = this.myCurrent_index;
        SLink next_current = null;
        // Check for myCurrent element
        if (this.myCurrent != null)
        {
         if (this.myCurrent == this.myFirst)
         {
             if (this.myFirst == this.myLast) //list has only one element
             {
              this.myFirst = this.myLast = null;
              next_current = null;
              this.myCurrent_index = -1;
              if (this.isTraceable())
              {
                  System.out.println("LinList.erase(): the only element erased. List becomes empty.");
              }
             }
             else //list has more than one element
             {
              // Remove myFirst element
              this.myCurrent.next.prev = null;
              this.myFirst = this.myCurrent.next;
              next_current = this.myFirst;
              this.myCurrent_index = 0;
              if (this.isTraceable())
              {
                  System.out.println("LinList.erase(): first element erased.");
              }
       			 }
         }
         else
         {
         		if (this.myCurrent == this.myLast) // erase last element
		        {
		          this.myCurrent.prev.next = null;
		          this.myLast = this.myCurrent.prev;
		          next_current = null;
		          this.myCurrent_index = -1; //no element after original myLast
		          if (this.isTraceable())
		          {
		              System.out.println("LinList.erase(): last element erased.");
		          }
		        }
		        else // erase current element
		        {
		          this.myCurrent.next.prev = this.myCurrent.prev;
		          this.myCurrent.prev.next = this.myCurrent.next;
		          next_current = this.myCurrent.next;
		          this.myCurrent_index++;
		                if (this.isTraceable())
		                {
		                    System.out.println("LinList.erase(): "+original_index+"-th element erased.");
		                }
		        }
        }
        //adjust myCurrent element
        this.myCurrent = next_current;
        // adjust the myCount
        --this.myCount;
        if (this.isTraceable())
        {
           this.check();
        }

        return true;
    }
    if (this.isTraceable())
    {
        System.out.println("LinList.erase(): current position not defined.");
        this.check();
    }
    return false;
}
//--------------------------------------------------------------------------
    /**
     * Retrieve an Object from the i-th element
     */
    public Object get(int i) //
    {
        boolean ahead = true; // true if search forward
        // check the range of i
        if (i < 0 || i >= this.myCount)
         return null;
        // return the cached value
        if (i == this.myCurrent_index)
         return this.myCurrent.d;
        // if no cached value
        if (this.myCurrent_index == -1)
        {
         // determine whether desired element is near myFirst or myLast
         if (i < (this.myCount / 2))
         {
                //Search forward from myFirst
             this.myCurrent = this.myFirst;
             this.myCurrent_index = 0;
             ahead = true;
         }
         else
         {
                //Search backward from myLast
             this.myCurrent = this.myLast;
             this.myCurrent_index = this.myCount - 1;
             ahead = false;
         }
        }
        else
        {
         // die gewuenschte Position liegt vor der aktuellen
         if (i < this.myCurrent_index) // i.........myCurrent_index.......
         {
             // liegt i naeher an myFirst, als an myCurrent_index?
             if ((this.myCurrent_index - i) > i)
             {
                    //i is far awary from myCurrent_index
                    //(myFirst)...(i)....|..........(myCurrent_index)
              this.myCurrent = this.myFirst;
              this.myCurrent_index = 0;
              ahead = true;
             }
             else
                {
                    //i is near myCurrent_index
                    //(myFirst)........|....(i).....(myCurrent_index)
              ahead = false;
                }
         }
         else // .........myCurrent_index.....i
         {
             // liegt i naeher an myLast, als an myCurrent_index?
             if ((i - this.myCurrent_index) > ((this.myCount-1) - i))
             {
                    //i is far awary from myCurrent_index
                    //(myCurrent_index).........|....(i)...(myLast)
              this.myCurrent = this.myLast;
              this.myCurrent_index = this.myCount - 1;
              ahead = false;
             }
             else
                {
                    //i is near myCurrent_index
                    //(myCurrent_index)....(i)....|........(myLast)
              ahead = true;
                }
         }
        }
        if (ahead) //search forward
        {
         for (int k = this.myCurrent_index; k < i; k++)
         {
             if (this.myCurrent == null)
    {
     System.out.println("LinList::get: List seems to be inconsistent");
     System.exit(1);
    }
             this.myCurrent = this.myCurrent.next;
         }
        }
        else  //search backward
        {
         for (int k = this.myCurrent_index; k > i; k--)
         {
             if (this.myCurrent == null)
    {
              System.out.println("LinList::get: List seems to be inconsistent");
     System.exit(1);
    }
             this.myCurrent = this.myCurrent.prev;
         }
        }
        this.myCurrent_index = i;
        return this.myCurrent.d;
    }
//--------------------------------------------------------------------------
    /**
     * Retrieve the myFirst element w.r.t. myCurrent element
     * It also position the myCurrent pointer to myFirst element
     * Return an Object
     */
    public Object get_first()
    {
        this.myCurrent = this.myFirst;
        if (this.myCurrent != null)
        {
         this.myCurrent_index = 0;
         return this.myCurrent.d;
        }
        else
         return null;
    }
//--------------------------------------------------------------------------
    /**
     * Retrieve the myLast element w.r.t. myCurrent element
     * It also position the myCurrent pointer to myLast element
     * Return an Object
     */
    public Object get_last()
    {
        this.myCurrent = this.myLast;
        if (this.myCurrent != null)
        {
         this.myCurrent_index = this.myCount - 1;
         return this.myCurrent.d;
        }
        else
         return null;
    }
//--------------------------------------------------------------------------
    /**
     * Retrieve the next element w.r.t. myCurrent element
     * It also position the myCurrent pointer to next element
     * Return an Object
     */
    public Object get_next()
    {
        this.myCurrent = this.myCurrent.next;
        if (this.myCurrent != null)
        {
         ++this.myCurrent_index;
         return this.myCurrent.d;
        }
        else
        {
         this.myCurrent_index = -1;
         return null;
        }
    }
//--------------------------------------------------------------------------
    /**
     * Retrieve the previous element w.r.t. myCurrent element
     * It also position the myCurrent pointer to previous element
     * Return an Object
     */
    public Object get_prev()
    {
        this.myCurrent = this.myCurrent.prev;
        if (this.myCurrent != null)
        {
         --this.myCurrent_index;
         return this.myCurrent.d;
        }
        else
        {
         this.myCurrent_index = -1;
         return null;
        }
    }
//--------------------------------------------------------------------------
    /**
     * Check integraty of list, true means list is valid
     */
    public boolean check()
    {
        SLink old_f = this.myFirst;
        // Liste muss ganz leer sein
        if (old_f == null) //if pointer to myFirst element is null
        {
         	if (this.myLast != null)//if pointer to myLast element is not null
   				{
                //inconsistent of myFirst and myLast
             	System.out.println("LinList.check(): ERROR, myFirst == null but myLast != null.");
    					return false;
   				}
         	if (this.myCount != 0)
   				{
                //inconsistent of myFirst and myCount
            	System.out.println("LinList.check(): ERROR, myFirst == null but myCount != 0.");
    					return false;
   				}
         	return true;
        }
        int i = 1;
        if (old_f.prev != null)
        {
         System.out.println("LinList.check(): ERROR, myFirst.prev != null.");
         return false;
        }
        for (SLink f = old_f.next; f != null; f = f.next)
        {
         if (f.prev != old_f)
         {
             System.out.println("LinList.check(): ERROR, Incorrect backward link.");
                return false;
         }
         if (old_f.next != f)
         {
             System.out.println("LinList.check(): ERROR, Incorrect forward link.");
                return false;
         }
         old_f = f;
         i++;
         if (i > this.myCount)
         {
             System.out.println("LinList.check(): ERROR, Incorrect count,(myCount "+this.myCount+" < actual count "+i+").");
             return false;
         }
        }
        if (old_f.next != null)
        {
         System.out.println("LinList.check(): ERROR, myLast.next != null.");
         return false;
        }
        if (this.myLast != old_f)
        {
         System.out.println("LinList.check(): ERROR, myLast does not point to last element.");
         return false;
        }
        if (i != this.myCount)
        {
            System.out.println("LinList.check(): ERROR, Incorrect count, (myCount "+this.myCount+" != actual count "+i+").");
   					return false;
        }
  			return true;
    }
//--------------------------------------------------------------------------
    /**
     * Implements the Traceable interface
     */
    private boolean IamTraceable;
    public void    setTraceable(boolean enable) { this.IamTraceable = enable; }
    public boolean isTraceable()                { return this.IamTraceable;   }
//--------------------------------------------------------------------------
}