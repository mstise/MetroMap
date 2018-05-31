import java.awt.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MetroStop
{
    public MetroStop(String name, GregorianCalendar date)
    {
        _name = name;
        _date = date;
        _incommingLinesBelow = 0;
        _outgoingLinesBelow = 0;
    }

    private ArrayList<MetroLine> _intersectingLines;
    public ArrayList<MetroLine> get_intersectingLines()
    {
        if (_intersectingLines == null)
            _intersectingLines = new ArrayList<MetroLine>();
        return _intersectingLines;
    }

    private String _name;
    public String get_name()
    {
        return _name;
    }

    private GregorianCalendar _date;
    public GregorianCalendar get_date()
    {
        return _date;
    }

    private Dimension _position;
    public Dimension get_position()
    {
        return _position;
    }
    public void set_position(Dimension position)
    {
        _position = position;
    }

    private Color _color;
    public Color get_color()
    {
        return _color;
    }
    public void set_color(Color color)
    {
        _color = color;
    }

    private int _outgoingLinesBelow;
    public int get_outgoingLinesBelow()
    {
        return _outgoingLinesBelow;
    }
    public void increment_outgoingLinesBelow()
    {
        _outgoingLinesBelow++;
    }

    private int _outgoingLinesAbove;
    public int get_outgoingLinesAbove()
    {
        return _outgoingLinesAbove;
    }
    public void increment_outgoingLinesAbove()
    {
        _outgoingLinesAbove++;
    }

    private int _incommingLinesBelow;
    public int get_incommingLinesBelow()
    {
        return _incommingLinesBelow;
    }
    public void increment_incommingLinesBelow()
    {
        _incommingLinesBelow++;
    }

    private int _incommingLinesAbove;
    public int get_incommingLinesAbove()
    {
        return _incommingLinesAbove;
    }
    public void increment_incommingLinesAbove()
    {
        _incommingLinesAbove++;
    }

    private ArrayList<MetroLineDirection> _outgoingSockets;
    public ArrayList<MetroLineDirection> get_outgoingSockets()
    {
        if (_outgoingSockets == null)
            _outgoingSockets = new ArrayList<MetroLineDirection>();
        return _outgoingSockets;
    }

    private ArrayList<MetroLineDirection> _incommingSockets;
    public ArrayList<MetroLineDirection> get_incommingSockets()
    {
        if (_incommingSockets == null)
            _incommingSockets = new ArrayList<MetroLineDirection>();
        return _incommingSockets;
    }

    private boolean _mouseOver = false;
    public boolean get_mouseOver()
    {
        return _mouseOver;
    }
    public void set_mouseOver(boolean mouseOver)
    {
        _mouseOver = mouseOver;
    }

    private String _falseTopic;
    public String get_falseTopic() { return _falseTopic; }
    public void set_falseTopic(String falseTopic)
    {
        _falseTopic = falseTopic;
    }

    public void clearSockets()
    {
        _incommingSockets = new ArrayList<MetroLineDirection>();
        _outgoingSockets = new ArrayList<MetroLineDirection>();
    }
}
