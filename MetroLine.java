import java.awt.*;
import java.util.ArrayList;

public class MetroLine
{
    public MetroLine(String name, Color color)
    {
        _name = name;
        _color = color;
    }

    private String _name;
    public String get_name()
    {
        return _name;
    }

    private int _heightPosition = 0;
    public int get_heightPosition()
    {
        return _heightPosition;
    }
    public void set_heightPosition(int heightPosition)
    {
        _heightPosition = heightPosition;
    }

    private Color _color;
    public Color get_color()
    {
        return _color;
    }

    private ArrayList<MetroStop> _intersectingStops;
    public ArrayList<MetroStop> get_intersectingStops()
    {
        if (_intersectingStops == null)
            _intersectingStops = new ArrayList<MetroStop>();
        return _intersectingStops;
    }

    private int _lastOffSet;
    public int get_lastOffSet()
    {
        return _lastOffSet;
    }
    public void set_lastOffSet(int offSet)
    {
        _lastOffSet = offSet;
    }

    private MetroStop _lastStop;
    public MetroStop get_lastStop()
    {
        return _lastStop;
    }
    public void set_lastStop(MetroStop metroStop)
    {
        _lastStop = metroStop;
    }

    private boolean _labelHasBeenDrawn = false;
    public boolean get_labelHasBeenDrawn()
    {
        return _labelHasBeenDrawn;
    }
    public void set_labelHasBeenDrawn(boolean value)
    {
        _labelHasBeenDrawn = value;
    }
}
