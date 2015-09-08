package rstar;
public class PPoint
{
    int dimension;
    float data[];
    float distanz;

    public PPoint()
    {
        dimension = Constants.RTDataNode__dimension;
        data = new float[dimension];
        distanz = 0.0f;
    }
    
    public PPoint(int dimension)
    {
        this.dimension = dimension;
        data = new float[dimension];
        distanz = 0.0f;
    }
    
    public PPoint(int dimension, float x, float y)
    {
        this.dimension = dimension;
        data = new float[dimension];
        distanz = 0.0f;
        data[0]=x;
        data[1]=y;
    }
}