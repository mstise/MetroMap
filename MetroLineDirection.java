public class MetroLineDirection
{
    public MetroLineDirection(MetroLine line, int distance)
    {
        _metroLine = line;
        _distance = distance;
    }

    private MetroLine _metroLine;
    public MetroLine get_metroLine()
    {
        return _metroLine;
    }

    private Direction _direction;
    public Direction get_direction()
    {
        return _direction;
    }
    public void set_direction(Direction _direction)
    {
        this._direction = _direction;
    }

    private int _distance;
    public int get_distance()
    {
        return _distance;
    }

    private int _socketNumber = 0;
    public int get_socketNumber()
    {
        return _socketNumber;
    }
    public void set_socketNumber(int _socketNumber)
    {
        this._socketNumber = _socketNumber;
    }
    public void increment_socketNumber()
    {
        this._socketNumber++;
    }
    public void decrement_socketNumber()
    {
        this._socketNumber--;
    }

    public enum Direction
    {
        stay,
        up,
        down
    }
}
