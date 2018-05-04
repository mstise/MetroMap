import java.util.ArrayList;

public class HiddenLane
{
    public int Y;
    public int XStart;
    public int XEnd;
    private int linePosition = 0;

    public ArrayList<LineNumber> intersectingLines = new ArrayList<LineNumber>();

    public HiddenLane(int y, int xStart, int xEnd, MetroLine metroLine)
    {
        Y = y;
        XStart = xStart;
        XEnd = xEnd;
        intersectingLines.add(new LineNumber(linePosition, metroLine));
        if (linePosition > -1)
            linePosition++;
        linePosition *= -1;
    }

    public void AddMetroLine(MetroLine metroLine)
    {
        intersectingLines.add(new LineNumber(linePosition, metroLine));
        if (linePosition > -1)
            linePosition++;
        linePosition *= -1;
    }
}
