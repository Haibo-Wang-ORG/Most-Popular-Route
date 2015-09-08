package rstar;
////////////////////////////////////////////////////////////////////////
// RDataNode
////////////////////////////////////////////////////////////////////////

/**
* RDataNode implements data (leaf) nodes in the R*-tree

* the block of the RTDirNode is organised as follows:
* +--------+---------+---------+-----+------------------+
* | header | Data[0] | Data[1] | ... | Data[capacity-1] |
* +--------+---------+---------+-----+------------------+

* the header of the RTDirNode is organised as follows:
* +-------+-------------+
* | level | num_entries |
* +-------+-------------+
*/

import java.io.*;
import java.util.HashSet;

public final class RTDataNode extends RTNode implements Node
{
	Data data[];                         // array of data (leaf mbrs)

    public boolean is_data_node()        // this is a data node
    {return true;}

    public void overlapping(float p[], int nodes_t[])
    { nodes_t[0]++; }

    public void nodes(int nodes_a[])     // see RTree.nodes()
    { nodes_a[0]++; }

    public RTDataNode(RTree rt)
    // create a brand new RTDataNode
    {
        super(rt);

        byte b[];
        int header_size;
        Data d;

        level = 0;

        // mal kurz einen Dateneintrag erzeugen und schauen, wie gross der wird..
        d = new Data(dimension);

        // von der Blocklaenge geht die Headergroesse ab
        header_size = Constants.SIZEOF_SHORT  //level
                      + Constants.SIZEOF_INT; //num_entries
        capacity = (rt.file.get_blocklength() - header_size) / d.get_size();
        //System.out.println("RTDataNode created. Id: " + rt.num_of_dnodes);
        //System.out.println("RTDataNode capacity " + capacity);

        //delete d;

        // Eintraege erzeugen; das geht mit einem Trick, da C++ beim
        // Initialisieren von Objektarrays nur Defaultkonstruktoren kapiert.
        // Daher wird ueber statische Variablen die Information uebergeben.

        //RTDataNode__dimension = dimension;
        data = new Data[capacity];

        // create a new block for the node
        b = new byte[rt.file.get_blocklength()];
        try
        {
            block = rt.file.append_block(b);
        }
        catch (IOException e)
        {
            Constants.error("RTDatanode creation: error in block appending", true);
        }

        rt.num_of_dnodes ++;

        // Must be written to disk --> Set dirty bit
        dirty = true;
    }

    // this constructor reads an existing RTDataNode from the disk
    public RTDataNode(RTree rt, int _block)
    {
        super(rt);

        byte b[];
        int header_size;
        Data d;

        // mal kurz einen Dateneintrag erzeugen und schauen, wie gross der wird..
        d = new Data(dimension);

        // von der Blocklaenge geht die Headergroesse ab
          header_size = Constants.SIZEOF_SHORT //level
                      + Constants.SIZEOF_INT;  //num_entries
        capacity = (rt.file.get_blocklength() - header_size) / d.get_size();

        // Eintraege erzeugen; das geht mit einem Trick, da C++ beim
        // Initialisieren von Objektarrays nur Defaultkonstruktoren kapiert.
        // Daher wird ueber statische Variablen die Information uebergeben.

        //RTDataNode__dimension = dimension;
        data = new Data[capacity];

        // zugehoerigen Plattenblock holen und Daten einlesen
        // dies kann nicht in RTNode::RTNode(..) passieren, weil
        // beim Aufruf des Basisklassenkonstruktors RTDirNode noch
        // gar nicht konstruiert ist, also auch keine Daten aufnehmen kann
        block = _block;
        try
        {
            b = new byte[rt.file.get_blocklength()];
            rt.file.read_block(b, block);
            read_from_buffer(b);
        }
        catch (Exception e)
        {
            Constants.error("RTDataNode initialization: error in block reading", true);
        }

        // Plattenblock muss vorerst nicht geschrieben werden
        dirty = false;
    }
    
    // transform the sequential block information in buffer to object information
    public void read_from_buffer(byte buffer[]) throws IOException
    {
        ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
        DataInputStream in = new DataInputStream(byte_in);

        // read header
        level = in.readShort();
        level = 0; // level should be 0 anyway
        num_entries = in.readInt();
        
        //System.out.println("RTDataNode.read_from_buffer(): num_entries =" +num_entries);

        // read data
        for (int i = 0; i < num_entries; i++)
        {
            data[i] = new Data(dimension);
            data[i].read_from_buffer(in);
        }

        in.close();
        byte_in.close();
    }

    // serialize the object's information to the buffer[]
    public void write_to_buffer(byte buffer[]) throws IOException
    {
    	ByteArrayOutputStream byte_out = new ByteArrayOutputStream(buffer.length);
        DataOutputStream out = new DataOutputStream(byte_out);

        // write header
        level = 0;  // level should be 0 anyway
        out.writeShort(level);
        out.writeInt(num_entries);

        // write data
        for (int i = 0; i < num_entries; i++)
                data[i].write_to_buffer(out);       
        
        byte[] bytes = byte_out.toByteArray();      
        for (int i = 0; i < bytes.length; ++i)
                buffer[i] = bytes[i];

        out.close();
        byte_out.close();
    }

    public void print()
    {
        int i, n;

        n = get_num();
        for (i = 0; i < n ; i++)
        {
            System.out.println(data[i].data[0] + " " + data[i].data[1]);
        }
        System.out.println("level " + level);
    }


    public int get_num_of_data()
    {
        return get_num();
    }

    public float[] get_mbr() // float
    // calculates the mbr of all data in this node
    {
        int i, j, n;
        float mbr[], tm[];

        mbr = data[0].get_mbr();
        n = get_num();
        for (j = 1; j < n; j++)
        {
                tm = data[j].get_mbr();
                for (i = 0; i < 2*dimension; i += 2)
                {
                    mbr[i]   = Constants.min(mbr[i],   tm[i]);
                    mbr[i+1] = Constants.max(mbr[i+1], tm[i+1]);
            }
        }
        return mbr;
    }


    /*
    * split this to this and the new node brother
    * called when the node overflows and a split has to take place.
    * This function splits the data into two partitions and assigns the
    * first partition as data of this node, and the second as data of
    * the new split node.
    * invokes RTNode.split() to calculate the split distribution
    */
    public void split(RTDataNode splitnode)
    // splittet den aktuellen Knoten so auf, dass m mbr's nach splitnode verschoben
    // werden
    {
        int i, distribution[][], dist, n;
        float mbr_array[][]; // to be passed as parameter to RTNode.split()
        Data new_data1[], new_data2[];

    //#ifdef SHOWMBR
    //    split_000++;
    //#endif

        // initialize n, distribution[0]
        n = get_num();
        distribution = new int[1][];

        // allocate mbr_array to contain the mbrs of all entries
        mbr_array = new float[n][2*dimension];
        for (i = 0; i < n; i++)
        {
            mbr_array[i] = data[i].get_mbr();
        }

        // calculate distribution[0], dist by calling RTNode.split()
        dist = super.split(mbr_array, distribution);
        
        // neues Datenarray erzeugen
        // -. siehe Konstruktor
        //RTDataNode__dimension = dimension;
        
        // create new Data arrays to store the split results
        new_data1 = new Data[capacity];
        new_data2 = new Data[capacity];

        for (i = 0; i < dist; i++)
            new_data1[i] = data[distribution[0][i]];

        for (i = dist; i < n; i++)
            new_data2[i-dist] = data[distribution[0][i]];

        // set the new arrays as data arrays of this and splitnode's data
        data = new_data1;
        splitnode.data = new_data2;

        // Anzahl der Eintraege berichtigen
        num_entries = dist;
        splitnode.num_entries = n - dist;  // muss wegen Rundung so bleiben !!
    }
    
    /*
    * insert a new data entry in this node
    * this function may cause some data to be reinserted and/or the node to split
    * NOTE: the parameter sn is a reference call to sn[0] which will be
    * a new node after a potential split and NOT an array of nodes
    */
    public int insert(Data d, RTNode sn[])
    // liefert false, wenn die Seite gesplittet werden muss
    {
        int i, last_cand;
        float mbr[], center[];
        SortMbr sm[]; //used for REINSERT
        Data nd, new_data[];

        if (get_num() == capacity)
                Constants.error("RTDataNode.insert: maximum capacity violation", true);

        // insert data into the node
        data[get_num()] = d;
        num_entries ++;

        // Plattenblock zum Schreiben markieren
        dirty = true;

        if (get_num() == (capacity - 1))
        // overflow
        {
            if (my_tree.re_level[0] == false)
            // there was no reinsert on level 0 during this insertion
            // --> reinsert 30% of the data
            {
                // calculate center of page
                mbr = get_mbr();
                center = new float[dimension];
                for (i = 0; i < dimension; i++)
                     center[i] = (mbr[2*i] + mbr[2*i+1]) / (float)2.0;

                // neues Datenarray erzeugen
                // -. siehe Konstruktor
                //RTDataNode__dimension = dimension;
                
                //construct a new array to hold the data sorted according to their distance
                //from the node mbr's center
                new_data = new Data[capacity];

                // initialize array that will sort the mbrs
                sm = new SortMbr[num_entries];
                for (i = 0; i < num_entries; i++)
                {
                    sm[i] = new SortMbr();
                    sm[i].index = i;
                    sm[i].dimension = dimension;
                    sm[i].mbr = data[i].get_mbr();
                    sm[i].center = center;
                }

                // sort by distance of each center to the overall center
                Constants.quickSort(sm, 0, sm.length - 1, Constants.SORT_CENTER_MBR);

                last_cand = (int) ((float)num_entries * 0.30);

                // copy the nearest 70% candidates to new array
                for (i = 0; i < num_entries - last_cand; i++)
                        new_data[i] = data[sm[i].index];

                // insert last 30% candidates into reinsertion list
                for ( ; i < num_entries; i++)
                {
                    nd = new Data(dimension);
                    nd = data[sm[i].index];
                    my_tree.re_data_cands.insert(nd);
                }
                
                data = new_data;

                my_tree.re_level[0] = true;

                // correct # of entries
                num_entries -= last_cand;

                // must write page
                dirty = true;

                return Constants.REINSERT;
            }
            else
            {
            // reinsert was applied before
            // --> split the node
                sn[0] = new RTDataNode(my_tree);
                sn[0].level = level;
                split((RTDataNode)sn[0]);
            }
            return Constants.SPLIT;
        }
        else
            return Constants.NONE;
    }

    public Data get(int i)
    {
        Data d;

        if (i >= get_num())
        // if there is no i-th object -. null
            return null;

        d = new Data(dimension);
        d = data[i];

        return d;
    }

    public void region(float mbr[])
    {
        int i, n,j;
        float dmbr[];

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section(dimension, dmbr, mbr))
            {
                System.out.println("( ");
                for( j = 0; j < dimension; j++)
                    System.out.println(" " + dmbr[j]);

                System.out.println(" )");
            }
          }
    }

    public void point_query(float p[])
    {
        int i, n,j;
        float dmbr[];

    //#ifdef ZAEHLER
    //    page_access++;
    //#endif

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section(dimension, p, dmbr))
                    {
                //        System.out.println("( ");
                //        for( j = 0; j < dimension; j++)
                //            System.out.println(" %f",dmbr[j]);
                //
                //        System.out.println(" ) \n");
                    }
                    //delete [] dmbr;
        }
    }

    public void point_query(PPoint p, SortedLinList res)
    {
        int i, n,j;
        float dmbr[];

    //#ifdef ZAEHLER
    //    page_access++;
    //#endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.inside(p.data, dmbr, dimension))
            {
                res.insert(data[i]);
            }
        }
    }
    
    public void rangeQuery(float mbr[], SortedLinList res)
    {
        int i, n,j;
        float dmbr[];
        Data rect;
        PPoint center;

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section(dimension, dmbr, mbr))
            {
                // Center des MBRs ausrechnen
                //center = new Data(dimension);
                //for(i = 0; i < dimension; i++)
                //    center.data[i] = (mbr[2*i] + mbr[2*i+1]) / 2;
                //point.distanz = Constants.objectDIST(point,center);
                res.insert(data[i]);
            }
        }
    }

    
    public void ringQuery(PPoint center, float radius1, float radius2, SortedLinList res)
    {
        int i, n,j,k;
        float dmbr[];

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section_ring(dimension, dmbr, center, radius1, radius2))
            {
                System.out.println("RTDataNode: section ring succeeded");
                //PPoint point;
                //point = new PPoint(dimension);
                //for( j = 0; j < dimension; j++)
                //    point.data[j] = dmbr[2*j];
                //point.distanz = Constants.objectDIST(point,center);
                res.insert(data[i]);
            }
        }
    }
    
    public void rangeQuery(PPoint center, float radius, SortedLinList res)
    {
        int i, n,j,k;
        float dmbr[];

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section_c(dimension, dmbr, center,radius))
            {
                //System.out.println("RTDataNode: section succeeded");
                //PPoint point;
                //point = new PPoint(dimension);
                //for( j = 0; j < dimension; j++)
                //    point.data[j] = dmbr[2*j];
                //point.distanz = Constants.objectDIST(point,center);
                res.insert(data[i]);
            }
        }
    }
    
    //new function
    public void range(PPoint center, float  radius, SortedLinList res)
    {
    	my_tree.page_access++;
    	
        int i, n;
        float dmbr[];
        
        n = get_num();
        for (i = 0; i < n; i++)
        {
            dmbr = data[i].get_mbr();
            if (Constants.section_c_2(dimension, dmbr, center,radius))
            {
                res.insert(data[i]);
            }
        }   	
    }


    /*
    //#ifdef S3
    public void neighbours(LinList sl,
                  float eps,
                  res rs,
                  norm_ptr norm)
    {
        int i;
        float value;
        Data s[];

        for (i = 0; i < get_num(); i++)
        {
            for (s = sl.get_first(); s != null; s = sl.get_next())
            {
                       value = norm(s.data, data[i].data, s.dimension);

           // wenn f im eps-Bereich um s liegt -. rekursiv weiter
              if (value < eps)
                           rs.insert_region(data[i], s, value);
            }
        }
    }
    //#endif // S3
    */
    public void NearestNeighborSearch(PPoint QueryPoint, PPoint Nearest,
                float nearest_distanz)
    {
        int i,j;
        float nearest_dist,distanz;

        //nearest_dist = objectDIST(QueryPoint,Nearest);

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        for(i = 0; i < get_num(); i++)
        {
            //distanz = Constants.objectDIST(QueryPoint,get(i));
            distanz = Constants.MINDIST(QueryPoint,get(i).get_mbr());

            if (distanz <= nearest_distanz)
            {
                nearest_distanz = distanz;
                Nearest.distanz = distanz;
                for(j = 0; j < 2*dimension; j++)
                    Nearest.data[j] = get(i).data[j];
            }
        }
    }

    public void NearestNeighborSearch(PPoint QueryPoint,
                SortedLinList res,
                float nearest_distanz)
    {
        int i,j,k;
        float nearest_dist,distanz;
        boolean t;
        Data neu,dummy,element;

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        k = res.get_num();

        for (i = 0; i < get_num(); i++)
        {
            element = get(i);
            //distanz = Constants.objectDIST(QueryPoint,element);
            distanz = Constants.MINDIST(QueryPoint,element.get_mbr());


            if (distanz <= nearest_distanz)
            {
                // l"osche letztes Elemente der res-Liste
                dummy = (Data)res.get(k-1);
                t = res.erase();

                // Erzeuge neuen Punkt in der res-Liste
                element.distanz = distanz;
                res.insert(element);

                nearest_distanz = ((Data)res.get(k-1)).distanz;
            }
        }
    }

    public void range_nnQuery(float mbr[],SortedLinList res,
                    PPoint center, float nearest_distanz,
                    PPoint Nearest, boolean success)

    {
        int i,j,n;
        float nearest_dist,distanz,dmbr[];
        PPoint point;

        if (success)
        {
            rangeQuery(mbr,res);
            return;
        }

    //#ifdef ZAEHLER
    //    page_access += my_tree.node_weight[0];
    //#endif

        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            dmbr = data[i].get_mbr();
            if (Constants.section(dimension, dmbr, mbr))
            {
                point = new PPoint(dimension);
                for( j = 0; j < dimension; j++)
                    point.data[j] = dmbr[2*j];
                point.distanz = Constants.objectDIST(point,center);
                res.insert(point);
                success = true;
            }
        }

        if (!success)
        {
            for(i = 0; i < get_num(); i++)
            {
                //distanz = Constants.objectDIST(center,get(i));
                distanz = Constants.MINDIST(center,get(i).get_mbr());
                
                if (distanz <= nearest_distanz)
                {
                    nearest_distanz = distanz;
                    for(j = 0; j < dimension; j++)
                    Nearest.data[j] = get(i).data[j];
                }
            }
        }
    }


    /*
    void writeinfo(FILE *f)
    {
        float mbr[];

        mbr = get_mbr();
        fSystem.out.println(f,"%d\n",level+1);
        fSystem.out.println(f,"move %f %f\n",mbr[0],mbr[2]);
        fSystem.out.println(f,"draw %f %f\n",mbr[1],mbr[2]);
        fSystem.out.println(f,"draw %f %f\n",mbr[1],mbr[3]);
        fSystem.out.println(f,"draw %f %f\n",mbr[0],mbr[3]);
        fSystem.out.println(f,"draw %f %f\n",mbr[0],mbr[2]);
        fSystem.out.println(f,"\n");
        delete [] mbr;
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
                Constants.error("RTDataNode delete: Error in writing block", true);
            }
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
    }
    */
    
    public void constraints_query(rectangle rect, double distance[], short direction, short MBRtopo, short topology, SortedLinList res)
    {
    	int i, n;

        my_tree.page_access++;
        
        n = get_num();
        for (i = 0; i < n; i++)
        // teste alle Rechtecke auf Ueberschneidung
        {
            rectangle data_rect = Constants.toRectangle(data[i].data);
            
            //check exact topological constraints
            byte rel = relationSet.topoMapping(relation.topological(data_rect, rect));
            int relation_match = relationSet.p2(rel) & topology;
            if (relation_match == 0) continue; // disqualified
            
            //check directional constraints
            //compute actual angle
            double angle = relation.angle(data_rect, rect);
            
            //compute actual direction(s)
            short dir = 0;
	        for (byte j=0;j<8;j++)
	            if (similarity.angleSimilarity(j*45,angle) > 0)
	                dir = relationSet.setBit(dir,j);
	                
	        if ((dir & direction) == 0) continue; //disqualified data
            
            //check distance constraints
            double dist = relation.distance(data_rect, rect);
            if ((dist>=distance[0]) && (dist<=distance[1]))
            {
                System.out.println("RTDataNode: query succeeded");
                //PPoint point;
                //point = new PPoint(dimension);
                //for( j = 0; j < dimension; j++)
                //    point.data[j] = dmbr[2*j];
                //point.distanz = Constants.objectDIST(point,center);
                res.insert(data[i]);
            }
        }
    }
    
    public void test()
    {
    	int number = this.get_num();
    	Data point;
    	System.out.println("======== leaf level: "+this.level +" number: "+this.get_num()+"===========");
    	for(int i =0 ;i< number;i++)
    	{
    		point =  this.get(i);
    		System.out.println("point "+i+" : x-" + point.data[0]+ " y-"+point.data[2]);
    		System.out.println("point "+i+" : x-" + point.data[1]+ " y-"+point.data[3]);
    	}    	
    }
    
    /*********************************************************
     * new knn algorihtm
     *******************************************************/
    public void kNN(PPoint queryPoint, int k, SortedLinList nnpoints)
    {
    	//my_tree.page_access += my_tree.node_weight[level];
    	my_tree.page_access++;
    	
    	int i;
    	int numberOfPoints=this.get_num();
    	float distance;
    	Data point, temp;

    	if(my_tree.firstDown)
    	{
    		my_tree.queue_size+=numberOfPoints;
    		my_tree.firstDown = false;
    	}
    	
    	for (i = 0; i < numberOfPoints; i++)
        {
    		point = get(i);
    		//the exact distance from querypoint to point
    		distance = Constants.MINDIST(queryPoint,point.get_mbr());
    		
            if (distance <= ((Data)nnpoints.get(k-1)).distanz)
            {        	
            	temp = (Data)nnpoints.get(k-1);
            	//delete the element at my current position, 
            	//which is the reason we put temp = (Data)res.get(k-1) before it
            	nnpoints.erase();
            
            	point.distanz = distance;
            	nnpoints.insert(point);
            }
        }
    }
    
    /*********************************************************
     * new knn algorihtm, pruning by maxdist
     *******************************************************/
    public void kNN(PPoint queryPoint, int k, SortedLinList nnpoints, float  lambdaNN)
    {
    	//my_tree.page_access += my_tree.node_weight[level];
    	my_tree.page_access++;
    	
    	int i;
    	int numberOfPoints=this.get_num();
    	float distance;
    	Data point, temp;
    	
    	if(my_tree.firstDown)
    	{
    		my_tree.queue_size+=numberOfPoints;
    		my_tree.firstDown = false;
    	}

    	for (i = 0; i < numberOfPoints; i++)
        {
    		point = get(i);
    		//the exact distance from querypoint to point
    		distance = Constants.MINDIST(queryPoint,point.get_mbr());
    		
            if (distance >=lambdaNN && distance <= ((Data)nnpoints.get(k-1)).distanz)
            {     
            	temp = (Data)nnpoints.get(k-1);
            	//delete the element at my current position, 
            	//which is the reason we put temp = (Data)res.get(k-1) before it
            	nnpoints.erase();
            
            	point.distanz = distance;
            	nnpoints.insert(point);
            }
        }
    }
}