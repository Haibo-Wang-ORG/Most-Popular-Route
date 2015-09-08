package rstar;
////////////////////////////////////////////////////////////////////////
// RDirNode
////////////////////////////////////////////////////////////////////////

/**
* RDirNode implements directory (intermediate) nodes in the R*-tree

* the block of the RTDirNode is organised as follows:
* +--------+-------------+-------------+-----+----------------------+
* | header | DirEntry[0] | DirEntry[1] | ... | DirEntry[capacity-1] |
* +--------+-------------+-------------+-----+----------------------+

* the header of the RTDirNode is organised as follows:
* +-------------+-------+-------------+
* | son_is_data | level | num_entries |
* +-------------+-------+-------------+
*/

import java.io.*;
import java.util.HashSet;

public final class RTDirNode extends RTNode implements Node
{
	public DirEntry entries[];            // array of entries in the directory
    public boolean son_is_data;           // true, if son is a data page

    public boolean is_data_node()
    {return false;}                       // this is a directory node
    

    public RTDirNode(RTree rt)
    // create a brand new directory node
    // zugehoeriger Plattenblock muss erst erzeugt werden
    {
        super(rt);
        byte b[];
        int header_size;
        DirEntry d;

        // mal kurz einen Dateneintrag erzeugen und schauen, wie gross der wird..
        d = new DirEntry (dimension, son_is_data, rt);
    
        // von der Blocklaenge geht die Headergroesse ab
        header_size = Constants.SIZEOF_BOOLEAN   //son_is_data
                        + Constants.SIZEOF_SHORT //level
                        + Constants.SIZEOF_INT;  //num_entries
        capacity = (rt.file.get_blocklength() - header_size) / d.get_size();
        //System.out.println("RTDirNode created. Id: " + rt.num_of_inodes);
        //System.out.println("RTDirNode capacity " + capacity);

        // Eintraege erzeugen, das geht mit einem Trick, da C++ beim
        // Initialisieren von Objektarrays nur Defaultkonstruktoren kapiert.
        // Daher wird ueber globale Variablen die Information uebergeben.
        //RTDirNode__dimension = dimension;
        //RTDirNode__my_tree = my_tree;
        entries = new DirEntry[capacity];

        // initialize block for this node
        // neuen Plattenblock an File anhaengen
        b = new byte[rt.file.get_blocklength()];
        // append block to the rtree's blockfile
        try
        {
            block = rt.file.append_block(b);        }
        catch (IOException e)
        {
            Constants.error("RTDirnode creation: error in block appending", true);
        }

        rt.num_of_inodes ++;

        // If removed from memory, this node has to be written back to disk
        // Plattenblock muss auf jeden Fall neu geschrieben werden
        dirty = true;
    }


    // this constructor reads an existing RTDirNode from the disk
    public RTDirNode(RTree rt, int _block)
    {
        super(rt);

        byte b[];
        int header_size;
        DirEntry d;

        // mal kurz einen Dateneintrag erzeugen und schauen, wie gross der wird..
        d = new DirEntry(dimension, son_is_data, rt);

        // von der Blocklaenge geht die Headergroesse ab
        header_size = Constants.SIZEOF_BOOLEAN   //son_is_data
                        + Constants.SIZEOF_SHORT //level
                        + Constants.SIZEOF_INT;  //num_entries
        capacity = (rt.file.get_blocklength() - header_size) / d.get_size();
        
        //System.out.println("RTDirNode created. Id: " + rt.num_of_inodes);
        //System.out.println("RTDirNode capacity " + capacity);
        
        // Eintraege erzeugen, das geht mit einem Trick, da C++ beim
        // Initialisieren von Objektarrays nur Defaultkonstruktoren kapiert.
        // Daher wird ueber globale Variablen die Information uebergeben.
        //RTDirNode__dimension = dimension;
        //RTDirNode__my_tree = my_tree;
        entries = new DirEntry[capacity];

        // zugehoerigen Plattenblock holen und Daten einlesen
        // dies kann nicht in RTNode::RTNode(..) passieren, weil
        // beim Aufruf des Basisklassenkonstruktors RTDirNode noch
        // gar nicht konstruiert ist, also auch keine Daten aufnehmen kann
        block = _block;
        b = new byte[rt.file.get_blocklength()];
        // this time we read the directory node block from the disk
        try
        {
            rt.file.read_block(b, block);
            read_from_buffer(b);
        }
        catch (IOException e)
        {
            Constants.error("RTDirnode initialization: error in block reading", true);
        }

        // Plattenblock muss vorerst nicht geschrieben werden
        dirty = false;
    }

    // transform the sequential block information in buffer to object information
    public void read_from_buffer(byte buffer[]) throws IOException
    {
        ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
        DataInputStream in = new DataInputStream(byte_in);

        // read header info
        son_is_data = in.readBoolean();
        level = in.readShort();
        num_entries = in.readInt();
        
        //System.out.println("RTDirNode.read_from_buffer(): num_entries =" +num_entries);

        // read directory entries
        for (int i = 0; i < num_entries; i++)
        {
            entries[i] = new DirEntry(dimension, son_is_data, my_tree);
            entries[i].read_from_buffer(in);
            entries[i].son_is_data = son_is_data;
        }

        in.close();
        byte_in.close();
    }

    // serialize the object's information to the buffer[]
    public void write_to_buffer(byte buffer[]) throws IOException
    {
        ByteArrayOutputStream byte_out = new ByteArrayOutputStream(buffer.length);
        DataOutputStream out = new DataOutputStream(byte_out);

        // write header info
        out.writeBoolean(son_is_data);
        out.writeShort(level);
        out.writeInt(num_entries);

        // write directory entries
        for (int i = 0; i < num_entries; i++)
            entries[i].write_to_buffer(out);

        //byte[] bytes = new byte[my_tree.file.get_blocklength()];
        //bytes = byte_out.toByteArray();
        byte[] bytes = byte_out.toByteArray();
        

        int bl = bytes.length;        
        for (int i = 0; i < bytes.length; ++i)
            buffer[i] = bytes[i];

        out.close();
        byte_out.close();
    }

    // prints the mbrs of all directory entries in this node
    public void print()
    {
        int i, n;

        n = get_num();
        for (i = 0; i < n ; i++)
        {
            System.out.println(entries[i].bounces[0]
                                + " " + entries[i].bounces[1]
                                + " " + entries[i].bounces[2]
                                + " " + entries[i].bounces[3]);
        }
        System.out.println("level: " + level);
    }

    /*
    * recursive call to compute the # of data in the tree
    */
    public int get_num_of_data()
    {
        int i, n, sum;

        n = get_num();
        sum = 0;
        for (i = 0; i < n ; i++)
            sum += entries[i].num_of_data;
        return sum;
    }

    /*
    * returns the mbr of all entries in the directory
    */
    public float[] get_mbr()
    {
        int i, j, n;
        float mbr[];

        mbr = new float[2*dimension];
        for (i = 0; i < 2*dimension; i ++ )
            mbr[i] = entries[0].bounces[i];

        n = get_num();
        for (j = 1; j < n; j++)
        {
            for (i = 0; i < 2*dimension; i += 2)
            {
                mbr[i]   = Constants.min(mbr[i],   entries[j].bounces[i]);
                mbr[i+1] = Constants.max(mbr[i+1], entries[j].bounces[i+1]);
            }
        }
        return mbr;
    }

    /*
    * insert an entry into the node
    * it should be called only for not full nodes
    */
    public void enter(DirEntry de)
    {
        // ist ein Einfuegen ueberhaupt moeglich?
        if (get_num() > (capacity-1))
            Constants.error("RTDirNode.enter: called, but node is full", true);

        // Eintrag an erste freie Stelle kopieren
        entries[num_entries] = de;

        // jetzt gibts einen mehr
        num_entries++; 
    }

    /*
    * split this to this and the new node brother
    * called when the node overflows and a split has to take place.
    * invokes RTNode.split() to calculate the split distribution
    */
    public void split(RTDirNode brother)
    // splittet den aktuellen Knoten so auf, dass m mbr's nach sn verschoben
    // werden
    {
        int i, dist, n;
        int distribution[][];                    // distribution[0] will hold the best split distribution order
        float mbr_array[][];                     // array of the mbrs of all entries
        DirEntry new_entries1[], new_entries2[]; // the new directory entries that will hold the split parts

    //#ifdef SHOWMBR
    //    split_000++;
    //#endif

        // wieviele sind denn nun belegt?
        n = get_num(); // n = number of directory entries
        distribution = new int[1][];

        // mbr_array holds the mbrs of all entries
        mbr_array = new float[n][dimension*2];
        for (i = 0; i < n; i++)
               mbr_array[i] = entries[i].bounces;

        // call super.split() to initialize distribution[0], dist
        dist = super.split(mbr_array, distribution);
        
        // neues Datenarray erzeugen
        // -. siehe Konstruktor
        //RTDirNode__dimension = dimension;
        //RTDirNode__my_tree = my_tree;
        
        //initialize the new entries that will hold the split parts
        new_entries1 = new DirEntry[capacity];
        new_entries2 = new DirEntry[capacity];

        // fill the new entries with the split parts
        for (i = 0; i < dist; i++)
        {
            new_entries1[i] = entries[distribution[0][i]];
        }

        for (i = dist; i < n; i++)
        {
            new_entries2[i-dist] = entries[distribution[0][i]];
        }

        // Datenarrays freigeben
        // da die Nachfolgeknoten aber nicht geloescht werden sollen
        // werden vor dem Aufruf von delete noch alle Pointer auf null gesetzt
        //for (i = 0; i < n; i++)
        //{
        //       entries[i].son_ptr = null;
        //       brother.entries[i].son_ptr = null;
        //}

        // update this' and the brother's entries after the split
        entries = new_entries1;
        brother.entries = new_entries2;

        // Anzahl der Eintraege berichtigen
        num_entries = dist;
        brother.num_entries = n - dist;  // muss wegen Rundung so bleiben !!
    }

    /**
    * chooses the best subtree under this node to insert a new mbr
    * There are three cases:
    * Case 1: the new mbr is contained (inside) in only one directory entry mbr.
    * In this case follow this subtree.
    * Case 2: the new mbr is contained (inside) in more than one directory entry mbr.
    * In this case follow the entry whose mbr has the minimum area
    * Case 3: the new mbr is not contained (inside) in any directory entry mbr
    * In this case the criteria are the following:
    * - If the son nodes are data nodes consider as criterion first the minimum overlap
    *   increase if we follow one node with its neighbors, then the minimum area enlargement
    *   and finally the minimum area
    * - In the son nodes are dir nodes consider as criterion first the minimum area enlargement
    *   and finally the minimum area
    * After we choose the subtree, we enlarge the directory entry (if has to be enlarged)
    * and return its index
    */
    public int choose_subtree(float mbr[])
    {
        int i, j, n, follow, minindex=0, inside[], inside_count, over[];
        float bmbr[] = new float[2*dimension];
        float old_o, o, omin, a, amin, f, fmin;

        n = get_num();

        // faellt d in einen bestehenden Eintrag ?
        inside_count = 0;   // this variable holds the number of entries whose mbr contains the new mbr to be inserted
        inside = new int[n]; // this array holds the indices of entries whose mbr contains the new mbr to be inserted

        // calculate inside[]
        for (i = 0; i < n; i++)
        {
            switch (entries[i].section(mbr))
            {
                case Constants.INSIDE:
                    // mbr is inside entries[i] mbr
                    inside[inside_count++] = i;
                    break;
            }
        }

        if (inside_count == 1)
        // Case 1: There is exactly one dir_mbr that contains mbr
            follow = inside[0];
        else if (inside_count > 1)
        // Case 2: There are many dir_mbrs that contain mbr
        // choose the one for which insertion causes the minimun area enlargement
        {
            fmin = Constants.MAXREAL;
            //printf("Punkt in %d von %d MBRs \n",inside_count,n);

            for (i = 0; i < inside_count; i++)
            {
                f = Constants.area(dimension, entries[inside[i]].bounces);
                if (f < fmin)
                {
                    minindex = i;
                    fmin = f;
                }
            }

            follow = inside[minindex];
        }
        else
        // Case 3: There are no dir_mbrs that contain mbr
        // choose the one for which insertion causes the minimun overlap if son_is_data
        // else choose the one for which insertion causes the minimun area enlargement
        
        // Case 3: Rechteck faellt in keinen Eintrag -.
        // fuer Knoten, die auf interne Knoten zeigen:
        // nimm den Eintrag, der am geringsten vergroessert wird;
        // bei gleicher Vergroesserung:
        // nimm den Eintrag, der die geringste Flaeche hat
        //
        // fuer Knoten, die auf Datenknoten zeigen:
        // nimm den, der die geringste Ueberlappung verursacht
        // bei gleicher Ueberlappung:
        // nimm den Eintrag, der am geringsten vergroessert wird;
        // bei gleicher Vergroesserung:
        // nimm den Eintrag, der die geringste Flaeche hat
        {
            if (son_is_data)
            {
                omin = Constants.MAXREAL;
                fmin = Constants.MAXREAL;
                amin = Constants.MAXREAL;
                for (i = 0; i < n; i++)
                {
                    // compute the MBR of mbr and entries[i]

                    Constants.enlarge(dimension, bmbr, mbr, entries[i].bounces);

                    // calculate area and area enlargement
                    a = Constants.area(dimension, entries[i].bounces);
                    f = Constants.area(dimension, bmbr) - a;

                    // calculate overlap before enlarging entry_i
                    old_o = o = (float)0.0;

                    for (j = 0; j < n; j++)
                    {
                        if (j != i)
                        {
                            old_o += Constants.overlap(dimension,
                                 entries[i].bounces,
                                 entries[j].bounces);
                            o += Constants.overlap(dimension,
                                 bmbr,
                                 entries[j].bounces);
                        }
                    }
                    o -= old_o;

                    // is this entry better than the former optimum ?
                    if ((o < omin) ||
                        (o == omin && f < fmin) ||
                        (o == omin && f == fmin && a < amin))
                    {
                        minindex = i;
                        omin = o;
                        fmin = f;
                        amin = a;
                    }
                    //delete [] bmbr;
                }
            }
            else //son is not data
            {
                fmin = Constants.MAXREAL;
                amin = Constants.MAXREAL;
                for (i = 0; i < n; i++)
                {
                    // compute the MBR of mbr and entries[i]
                    Constants.enlarge(dimension, bmbr, mbr, entries[i].bounces);

                    // calculate area and area enlargement
                    a = Constants.area(dimension, entries[i].bounces);
                    f = Constants.area(dimension, bmbr) - a;

                    // is this entry better than the former optimum ?
                    if ((f < fmin) || (f == fmin && a < amin))
                    {
                        minindex = i;
                        fmin = f;
                        amin = a;
                    }
                    //delete [] bmbr;
                }
            }
            // enlarge the boundaries of the directoty entry we will follow
            Constants.enlarge(dimension, bmbr, mbr, entries[minindex].bounces);
            System.arraycopy(bmbr, 0, entries[minindex].bounces, 0, 2*dimension);

            follow = minindex;

            // nod has changed; set the dirty bit
            dirty = true;
        }

        return follow;
    }

    /*
    * insert a new data under this node
    * this function may cause the directory node to split
    * NOTE: the parameter sn is a reference call to sn[0] which will be
    * a new node after a potential split and NOT an array of nodes
    */
    public int insert(Data d, RTNode sn[])
    {
        int follow;
        RTNode succ = null;
        RTNode new_succ[] = new RTNode[1];
        DirEntry de;
        int ret;
        float mbr[],nmbr[];

        // choose subtree to follow
        mbr = d.get_mbr();
        follow = choose_subtree(mbr);
        
        // get corresponding son
        succ = entries[follow].get_son();

        // insert d into son
        ret = ((Node)succ).insert(d, new_succ);
        if (ret != Constants.NONE)
        // if anything (SPLIT or REINSERT) happend -. update bounces of entry "follow"
        // because these actions change the entries in succ
        {
            mbr = ((Node)succ).get_mbr();
            System.arraycopy(mbr, 0, entries[follow].bounces, 0, 2*dimension);
        }

        // recalculate # of succeeders in the tree
        entries[follow].num_of_data = ((Node)succ).get_num_of_data();

        if (ret == Constants.SPLIT)
        // succ was split into succ and new_succ[0]
        {
            // some error checking
            if (get_num() == capacity)
                Constants.error("RTDirNode.insert: maximum capacity violation", true);

            // create a new entry to hold the new_succ[0] node 
            de = new DirEntry(dimension, son_is_data, my_tree);
            nmbr = ((Node)new_succ[0]).get_mbr();

            System.arraycopy(nmbr, 0, de.bounces, 0, 2*dimension);
            de.son = new_succ[0].block;
            de.son_ptr = new_succ[0];
            de.son_is_data = son_is_data;
            de.num_of_data = ((Node)new_succ[0]).get_num_of_data();
            
            // insert de to this
            enter(de);

            if (get_num() == (capacity - 1))
            // directory node overflows -. Split
            // this happens already if the node is nearly filled (capacity - 1)
            // for the algorithms are more easy then
            {
                // initialize brother(split) node
                sn[0] = new RTDirNode(my_tree);
                ((RTDirNode)sn[0]).son_is_data = ((RTDirNode)this).son_is_data;
                sn[0].level = level;
                // split this --> this and sn[0]
                split((RTDirNode)sn[0]);
            
                ret = Constants.SPLIT;
            }
            else
                ret = Constants.NONE;
        }
        // must write page. set dirty bit
        dirty = true;

        return ret;
    }

    /*
    * search for data with linear index i
    * follow the appropriate subtree
    */
    public Data get(int i)
    {
        int j, n, sum;
        RTNode son;

        n = get_num();
        sum = 0;
        for (j = 0; j < n; j++)
        {
            sum += entries[j].num_of_data;

            if (sum > i)
            // i-th object is behind this node -. follow son
            {
                son = entries[j].get_son();
                return ((Node)son).get(i - (sum - entries[j].num_of_data));
            }
        }

        return null;
    }

    /*
    * print the mbrs under this node that intersect the query mbr
    */
    public void region(float mbr[])
    {
        int i, n;
        int s;
        RTNode succ;

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            s = entries[i].section(mbr);
            if (s == Constants.INSIDE || s == Constants.OVERLAP)
            {
                // Rechteck ist interessant -. rekursiv weiter
                succ = entries[i].get_son();
                ((Node)succ).region(mbr);
            }
        }
    }

    /*
    * print the mbrs under this node that intersect the query point
    */
    public void point_query(float p[])
    {
        int i, n;
        RTNode succ;

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            if (entries[i].is_inside(p))
            {
                // Rechteck ist interessant -. rekursiv weiter
                succ = entries[i].get_son();
                ((Node)succ).point_query(p);
            }
        }
    }

    /*
    * store in res the mbrs under this node that intersect the query point
    */
    public void point_query(PPoint p, SortedLinList res)
    {
        int i, n;
        RTNode succ;

        //page_access += my_tree.node_weight[level];
        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            if (entries[i].is_inside(p.data))
            {
                // Rechteck ist interessant -. rekursiv weiter
                succ = entries[i].get_son();
                ((Node)succ).point_query(p, res);
            }
        }
    }
    
    /*
    * store in res the mbrs under this node that intersect the query mbr
    */
    public void rangeQuery(float mbr[], SortedLinList res)
    {
        int i, n;
        int s;
        RTNode succ;

        //page_access += my_tree.node_weight[level];
        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            s = entries[i].section(mbr);
            if (s == Constants.INSIDE || s == Constants.OVERLAP)
            {
                // Rechteck ist interessant -. rekursiv weiter
                succ = entries[i].get_son();
                ((Node)succ).rangeQuery(mbr,res);
            }
        }
    }
    
    /*
    * store in res the mbrs under this node that intersect the query circle
    */
    public void rangeQuery(PPoint center, float radius,
            SortedLinList res)
    {
        int i, n;
        boolean s;
        RTNode succ;

      //  #ifdef ZAEHLER
        //page_access += my_tree.node_weight[level];
      //  #endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // test if the circle intersects the MBR of the entries one by one
        {
            s = entries[i].section_circle(center,radius);
            if (s)
            {
                // if c intersects mbr of entry i, follow that node
                succ = entries[i].get_son();
                ((Node)succ).rangeQuery(center,radius,res);
            }
        }
    }
    
    public void range(PPoint center, float  radius, SortedLinList res)
    {
    	 int i, n;
         boolean s;
         RTNode succ;
         my_tree.page_access++;
         
         n = get_num();
         for (i = 0; i < n; i++)
         // test if the circle intersects the MBR of the entries one by one
         {
             s = entries[i].section_circle_2(center,radius);
             if (s)
             {
                 // if c intersects mbr of entry i, follow that node
                 succ = entries[i].get_son();
                 ((Node)succ).range(center,radius,res);
             }
         }
    }
    
    /*
    * store in res the mbrs under this node that intersect the query ring
    */
    public void ringQuery(PPoint center, float radius1, float radius2, SortedLinList res)
    {
        int i, n;
        boolean s;
        RTNode succ;

      //  #ifdef ZAEHLER
        //page_access += my_tree.node_weight[level];
      //  #endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // test if the circle intersects the MBR of the entries one by one
        {
            s = entries[i].section_ring(center,radius1,radius2);
            if (s)
            {
                // if c intersects mbr of entry i, follow that node
                succ = entries[i].get_son();
                ((Node)succ).ringQuery(center,radius1,radius2,res);
            }
        }
    }

    //#ifdef S3
    /*
    void neighbours(LinList sl,
                                     float eps,
                                     Result rs,
                                     norm_ptr norm)
    {
        int i, j;
        Data s;
        float mbr[];
        RTNode succ;

        mbr = new float[2*dimension];

        for (i = 0; i < get_num(); i++)
        {
            for (s = sl.get_first(); s != null; s = sl.get_next())
            {
               for (j = 0; j < dimension; j++)
               {
                   mbr[2*j] = s.data[j] - eps;
                   mbr[2*j+1] = s.data[j] + eps;
               }

               if (entries[i].section(mbr) != S_NONE)
               {
                   // Rechteck ist interessant -. rekursiv weiter
                   succ = entries[i].get_son();
                   succ.neighbours(sl, eps, rs, norm);
               }
            }
        }

        //delete [] mbr;
    }
    */
    //#endif // S3


    /*
    * store in Nearest the mbr under this node nearest to the query point
    */
    public void NearestNeighborSearch(PPoint QueryPoint, PPoint Nearest, float/*[]*/ nearest_distanz)
    {
        float minmax_distanz;        // Array fuer MINMAXDIST aller Eintr"age
        int indexliste;        // Liste (for Sorting and Prunching)
        int i,j,k,last,n;
        float akt_min_dist;        // minimal distanz computed upto now
        float minmaxdist,mindist;

        BranchList activebranchList[];

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[level];
    //#endif

        n = get_num();

        activebranchList = new BranchList [n]; // Array erzeugen mit n Elementen

        for( i = 0; i < n; i++)
        {
            activebranchList[i].entry_number = i;
            activebranchList[i].minmaxdist = Constants.MINMAXDIST(QueryPoint,entries[i].bounces);
            activebranchList[i].mindist = Constants.MINDIST(QueryPoint,entries[i].bounces);
        }

        // sort branchList
        Constants.quickSort(activebranchList,0 ,activebranchList.length - 1, Constants.SORT_MINDIST);

        // prune BranchList
        last = Constants.pruneBranchList(nearest_distanz,activebranchList,n);

        for( i = 0; i < last; i++)
        {
            ((Node)entries[activebranchList[i].entry_number].get_son()).NearestNeighborSearch(QueryPoint, Nearest, nearest_distanz);

            last = Constants.pruneBranchList(nearest_distanz,activebranchList,last);
        }

        //delete [] activebranchList;
    }

  /*
    * store in res the mbrs under this node nearest to the query point
    */
    public void NearestNeighborSearch(PPoint QueryPoint,
                    SortedLinList res,
                    float nearest_distanz/*[]*/)
    {
        float minmax_distanz;        // Array fuer MINMAXDIST aller Eintr"age
        int indexliste;        // Liste (for Sorting and Prunching)
        int i,j,k,last,n;
        float akt_min_dist;        // minimal distanz computed upto now
        float minmaxdist,mindist;

        BranchList activebranchList[];

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[level];
    //#endif

        n = get_num();

        k = res.get_num();     // wird haben eine k-nearest-Narbor-Query

        nearest_distanz/*[0]*/ = ((Data)res.get(k-1)).distanz;  // der aktuell letzte
                                                    // n"achste Nachbar wird
                                                    // versucht zu ersetzen.

        activebranchList = new BranchList [n]; // Array erzeugen mit n Elementen

        for( i = 0; i < n; i++)
        {
                    activebranchList[i].entry_number = i;
                    activebranchList[i].minmaxdist = Constants.MINMAXDIST(QueryPoint,entries[i].bounces);
                    activebranchList[i].mindist = Constants.MINDIST(QueryPoint,entries[i].bounces);
        }

        // sortbranchList
        Constants.quickSort(activebranchList,0 ,activebranchList.length - 1, Constants.SORT_MINDIST);

        // pruneBranchList
        last = Constants.pruneBranchList(nearest_distanz,activebranchList,n);

        for( i = 0; i < last; i++)
        {
                ((Node)entries[activebranchList[i].entry_number].get_son()).NearestNeighborSearch(QueryPoint, res, nearest_distanz);

                last = Constants.pruneBranchList(nearest_distanz,activebranchList,last);
        }

        //delete [] activebranchList;
    }
    


    /*
    * store in res the mbrs under this node inside the scope of the
    * parameters
    */
    public void range_nnQuery(float mbr[], SortedLinList res,
                    PPoint center, float nearest_distanz,
                    PPoint Nearest, boolean success)
    {
        float minmax_distanz;        // Array fuer MINMAXDIST aller Eintr"age
        int indexliste;        // Liste (for Sorting and Prunching)
        int i,j,k,last,n;
        float akt_min_dist;        // minimal distanz computed upto now
        float minmaxdist,mindist;
        int s;

        BranchList activebranchList[], sectionList[];

        if (success)
        {
                rangeQuery(mbr,res);
                 return;
        }

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[level];
    //#endif



        n = get_num();

        sectionList = new BranchList [n];

        // Erst einmal feststellen, welche der Eintr"age einen Schnitt mit dem
        // aktuellen mbr haben und Ergebnis in sectionList speichern

        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            s = entries[i].section(mbr);
                    sectionList[i].entry_number = i;
            if (s == Constants.INSIDE || s == Constants.OVERLAP)
                        sectionList[i].section  = true;
                    else
                        sectionList[i].section  = false;
        }


        activebranchList = new BranchList [n]; // Array erzeugen mit n Elementen

        for( i = 0; i < n; i++)
        {
                activebranchList[i].entry_number = i;
                activebranchList[i].minmaxdist = Constants.MINMAXDIST(center,entries[i].bounces);
                activebranchList[i].mindist = Constants.MINDIST(center,entries[i].bounces);
        }

        // sortbranchList
        //qsort(activebranchList,n,sizeof(BranchList),sortmindist);

        // pruneBranchList
        last = Constants.pruneBranchList(nearest_distanz,activebranchList,n);

        // jetzt nachsehen, ob auch Schnittmenge mit mbr abgeschnitten wurde:
        // falls ja: Schnitt wieder R"ueckg"angig machen

        last = Constants.testBranchList(activebranchList,sectionList,n,last);

        for( i = 0; i < last; i++)
        {
                    ((Node)entries[activebranchList[i].entry_number].get_son()).range_nnQuery(mbr,res,center,nearest_distanz,
                          Nearest,success);

                    last = Constants.pruneBranchList(nearest_distanz,activebranchList,last);
            last = Constants.testBranchList(activebranchList,sectionList,n,last);
        }

        //delete [] activebranchList;
        //delete [] sectionList;
    }

    public void overlapping(float p[], int nodes_t[])
    {
        int i, n;
        RTNode succ;

        // ein Knoten mehr besucht
        nodes_t[level]++;

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            if (entries[i].is_inside(p))
            {
                // Rechteck ist interessant -. rekursiv weiter
                succ = entries[i].get_son();
                ((Node)succ).overlapping(p, nodes_t);
            }
        }
    }

    // see RTree.nodes()
    public void nodes(int nodes_a[])
    {
        int i, n;
        RTNode succ;

        // ein Knoten mehr besucht
        nodes_a[level]++;

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
                    succ = entries[i].get_son();
                    ((Node)succ).nodes(nodes_a);
        }
    }

    /*
    void RTDirNode<Data>::writeinfo(FILE *f)
    {
        int i,j,n;

        float *mbr;

        mbr = get_mbr();
        fprintf(f,"%d\n",level+1);
        fprintf(f,"move %f %f\n",mbr[0],mbr[2]);
        fprintf(f,"draw %f %f\n",mbr[1],mbr[2]);
        fprintf(f,"draw %f %f\n",mbr[1],mbr[3]);
        fprintf(f,"draw %f %f\n",mbr[0],mbr[3]);
        fprintf(f,"draw %f %f\n",mbr[0],mbr[2]);
        fprintf(f,"\n");
        delete [] mbr;

        n = get_num();
        for( i = 0; i < n ; i++)
        entries[i].get_son().writeinfo(f);

    }

	*/
    
    public void delete()
    {
        if (dirty)
        {
            byte b[] = new byte[my_tree.file.get_blocklength()];
            try
            {
                write_to_buffer(b);
                my_tree.file.write_block(b,block);
            }
            catch (IOException e)
            {
                Constants.error("RTDirNode delete: Error in writing block", true);
            }
        }
        for (int i=0; i<num_entries; i++)
        {
            entries[i].delete();
        }
    }
    
    /*
    protected void finalize()
    {
        if (dirty)
        {
            byte b[] = new byte[my_tree.file.get_blocklength()];
            try
            {
                write_to_buffer(b);
                my_tree.file.write_block(b,block);
            }
            catch (IOException e)
            {
                Constants.error("RTDirNode finalize: Error in writing block", true);
            }
        }
    }*/
    
    public void constraints_query(rectangle rect, double distance[], short direction, short MBRtopo, short topology, SortedLinList res)
    {
    	int i, n;
        boolean s;
        RTNode succ;

      //  #ifdef ZAEHLER
        //page_access += my_tree.node_weight[level];
      //  #endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // test if the circle intersects the MBR of the entries one by one
        {
            rectangle mbr = Constants.toRectangle(entries[i].bounces);
            
            //check MBR topological constraints
            byte rel = relationSet.topoMapping(relation.topological(mbr, rect));
            int relation_match = relationSet.p2(rel) & MBRtopo;
            if (relation_match == 0) continue; // disqualified
            
            //check directional constraints
            //calculate rect's centroid
            int center[] = new int[2];
            center[0] = (rect.UX+rect.LX)/2;
            center[1] = (rect.UY+rect.LY)/2;
            
            // if direction constraints do not include some of {E,NE,N,NW,W} = bin(31)
            // and the mbr's south bound is on the north of the centroid
            // exclude the entry
            if (((direction & 31) == 0) && (mbr.UY<=center[1])) continue;
            // if direction constraints do not include some of {N,NW,W,SW,S} = bin(124)
            // and the mbr's east bound is on the west of the centroid
            // exclude the entry
            if (((direction & 124) == 0) && (mbr.UX<=center[0])) continue;
            // if direction constraints do not include some of {W,SW,S,SE,E} = bin(241)
            // and the mbr's north bound is on the south of the centroid
            // exclude the entry
            if (((direction & 241) == 0) && (mbr.LY>=center[1])) continue;
            // if direction constraints do not include some of {S,SE,E,NE,N} = bin(199)
            // and the mbr's west bound is on the east of the centroid
            // exclude the entry
            if (((direction & 199) == 0) && (mbr.LX>=center[0])) continue;
            
            //check distance constraints
            //convert center array to PPoint
            PPoint p = new PPoint(2);
            p.data[0] = (float) center[0];
            p.data[1] = (float) center[1];
            
            s = entries[i].section_ring(p, (float)distance[0], (float)distance[1]);
            // does the mbr intersect the ring defined by distance array?
            if (s)
            {
                // if c intersects mbr of entry i, follow that node
                succ = entries[i].get_son();
                ((Node)succ).constraints_query(rect, distance, direction, MBRtopo, topology, res);
            }
        }
    }
    
    public void test()
    {
    	System.out.println("----- dirnode level:" + this.level+ " entries num:"+this.get_num());
    	((Node)this.entries[0].get_son()).test();
    }
    
    /*********************************************************
     * new knn algorihtm
     *******************************************************/
    public void kNN(PPoint queryPoint, int k, SortedLinList nnpoints)
    {
    	//my_tree.page_access += my_tree.node_weight[level];
    	
    	if(Constants.recordLeavesOnly==false)
    		my_tree.page_access++;
    	
    	int i;
    	int numberOfEntries=this.get_num();
    	
    	if(my_tree.firstDown)
    		my_tree.queue_size+=numberOfEntries;
    	
    	BranchList activebranchList[] = new BranchList[numberOfEntries]; 
    	//System.out.println("===here==="+activebranchList.length+ "  nubmer of entries "+numberOfEntries);
    	for( i = 0; i < numberOfEntries; i++)
        {
    		activebranchList[i]=new BranchList();
    		//System.out.println("===here==="+activebranchList[i]);
            activebranchList[i].entry_number = i;//record the index of this entry
            //figure out MINDIST for each entry
            activebranchList[i].mindist = Constants.MINDIST(queryPoint,entries[i].bounces);
        }
    	//sort entries by MINDIST
    	Constants.quickSort(activebranchList,0 ,activebranchList.length - 1, Constants.SORT_MINDIST);
    	
    	for( i = 0; i < activebranchList.length; i++)
        {
    		//only visit those entries with mindist < nnpoints.maxDist
    		if(activebranchList[i].mindist < ((Data)nnpoints.get(k-1)).distanz)
    		{
                ((Node)entries[activebranchList[i].entry_number].get_son()).kNN(queryPoint, k, nnpoints);
    		}
    		else
    			break;
         } 
    }
    
    /*********************************************************
     * new knn algorihtm, pruning by maxdist
     *******************************************************/
    public void kNN(PPoint queryPoint, int k, SortedLinList nnpoints, float  lambdaNN)
    {
    	//my_tree.page_access += my_tree.node_weight[level];
    	if(Constants.recordLeavesOnly==false)
    		my_tree.page_access++;
    	
    	int i;
    	int numberOfEntries=this.get_num();
    	
    	if(my_tree.firstDown)
    		my_tree.queue_size+=numberOfEntries;
    	
    	BranchList activebranchList[] = new BranchList[numberOfEntries]; 
    	//System.out.println("===here==="+activebranchList.length+ "  level "+this.level);
    	for( i = 0; i < numberOfEntries; i++)
        {
    		activebranchList[i]=new BranchList();
    		//System.out.println("===here==="+activebranchList[i]);
            activebranchList[i].entry_number = i;//record the index of this entry, the sorting later will mess up the order
            //figure out MINDIST for each entry
            activebranchList[i].mindist = Constants.MINDIST(queryPoint,entries[i].bounces);          
        }
    	//sort entries by MINDIST
    	Constants.quickSort(activebranchList,0 ,activebranchList.length - 1, Constants.SORT_MINDIST);   	
    	
    	//System.out.println(" have a check here size:"+nnpoints.get_num()+" k-"+(k-1));
    	
    	for( i = 0; i < activebranchList.length; i++)
        {
    		//if the maxdist < the last lambdaNN returned previously, skip...
    		//System.out.println(Constants.MAXDIST(queryPoint, entries[i].bounces)+" ----- "+lastNNDis);
    		if(Constants.MAXDIST(queryPoint, entries[i].bounces) < lambdaNN)
    		{
    			//System.out.println("================= skip a mbr"+lastNNDis);
    			continue;
    		}
    		else if(activebranchList[i].mindist < ((Data)nnpoints.get(k-1)).distanz)
    		{   //only visit those entries with mindist < nnpoints.maxDist
                ((Node)entries[activebranchList[i].entry_number].get_son()).kNN(queryPoint, k, nnpoints, lambdaNN);
    		}
    		else
    			break;
         } 
    }
}