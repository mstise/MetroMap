import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;

public class GUI extends JPanel
{
    public ArrayList<MetroStop> allMetroStops = new ArrayList<MetroStop>();
    public ArrayList<HiddenLane> hiddenLanes = new ArrayList<HiddenLane>();

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        drawMap(g);
    }
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
    }
    private void drawMap(Graphics g)
    {
        int w = getWidth();
        int h = getHeight();
        int distanceBetweenLines = 220;

        int counter = 0;
        for (MetroStop stop: allMetroStops)
        {
            for (MetroLine line: stop.get_intersectingLines())
            {
                if (line.get_heightPosition() == 0)
                {
                    line.set_heightPosition((h / 2) - (counter * distanceBetweenLines));
                    if (counter > -1)
                        counter++;
                    counter *= -1;
                }
            }
        }

        int brushWidth = 10;
        int granularityInDays = 1;
        int distancePerGranularity = 300;
        int granularityFactor = distancePerGranularity / granularityInDays;
        int margin = 350;
        int circleDiameter = 100;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(brushWidth));

        GregorianCalendar startDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, 1, 1);
        for (MetroStop metroStop: allMetroStops)
        {
            if (metroStop.get_date().before(startDate))
                startDate = metroStop.get_date();
        }

//        for (MetroStop metroStop: allMetroStops)
//        {
//            metroStop.clearSockets();
//            //Calculations for stop position, color and creation of the metro line hashSet
//            if (!metroStop.get_intersectingLines().isEmpty()) {
//                int height = metroStop.get_intersectingLines().get(0).get_heightPosition();
//                metroStop.set_color(metroStop.get_intersectingLines().get(0).get_color());
//                for (MetroLine line : metroStop.get_intersectingLines()) {
//                    if (line.get_heightPosition() - h / 2 == 0 ||
//                            line.get_heightPosition() > h / 2 && line.get_heightPosition() < height ||
//                            line.get_heightPosition() < h / 2 && line.get_heightPosition() > height) {
//                        height = line.get_heightPosition();
//                        metroStop.set_color(line.get_color());
//                    }
//                    if (!line.get_intersectingStops().contains(metroStop))
//                    {
//                        line.get_intersectingStops().add(metroStop);
//                    }
//                    metroLines.add(line);
//                }
//                metroStop.set_position(new Dimension((int) (margin + (ChronoUnit.DAYS.between(startDate.toInstant(), metroStop.get_date().toInstant()) * granularityFactor)), height));
//            }
//        }
        HashSet<MetroLine> metroLines = new HashSet<MetroLine>();
        SetStopPosition(metroLines, h, margin, startDate, granularityFactor);

        //Figure out which lines are going what directions
        for (MetroLine metroLine: metroLines)
        {
            for (MetroStop metroStop : metroLine.get_intersectingStops())
            {
                //Figure out if the line is a split line
                MetroStop firstStop = null, secondStop = metroStop;
                ArrayList<MetroStop> intersectingStops = new ArrayList<MetroStop>();
                int poundSignPosition = metroLine.get_name().indexOf('#');
                ArrayList<MetroLine> originalLines = null;
                if (poundSignPosition > 0)
                {
                    String originalLineName = metroLine.get_name().substring(0, poundSignPosition);
                    originalLines = metroLines.stream().filter(aLine -> aLine.get_name().length() > 2 && aLine.get_name().equals(originalLineName)).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
                }
                MetroLine originalLine = null;
                if (originalLines != null && originalLines.size() != 0)
                {
                    originalLine = originalLines.get(0);
                    final MetroLine finalOriginalLine = originalLine;
                    for (MetroStop stop: allMetroStops)
                    {
                        if (stop.get_intersectingLines().stream().filter(line -> line.get_name() == finalOriginalLine.get_name()).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>())).size() != 0)
                            intersectingStops.add(stop);
                    }
                    for (MetroStop stop: intersectingStops)
                    {
                        if (firstStop == null)
                            firstStop = stop;
                        else if (firstStop.get_date().before(stop.get_date()) && stop.get_date().before(secondStop.get_date()))
                            firstStop = stop;
                    }
                    metroLine.set_lastStop(firstStop);
                }
                if (metroLine.get_lastStop() != null)
                {
                    metroLine.get_lastStop().get_outgoingSockets().add(new MetroLineDirection(metroLine, metroLine.get_heightPosition() - metroLine.get_lastStop().get_position().height));
                    metroStop.get_incommingSockets().add(new MetroLineDirection(metroLine, metroLine.get_heightPosition() - metroStop.get_position().height));
                }
                metroLine.set_lastStop(metroStop);
            }
            metroLine.set_lastStop(null);
        }
        for (MetroStop metroStop: allMetroStops)
        {
            SetSocketsForStop(metroStop);
        }

        DrawOrphanLines(metroLines, g2d, brushWidth, circleDiameter, granularityFactor, margin);

        DrawLines(metroLines, g2d, brushWidth, circleDiameter, granularityFactor);


        for (MetroLine metroLine: metroLines)
        {
            metroLine.set_labelHasBeenDrawn(false);
        }
        for (MetroStop metroStop: allMetroStops)
        {
            if (metroStop.get_position() != null && metroStop.get_color() != null)
            {
                //Start drawing the stops
                g2d.setPaint(metroStop.get_color());
                int current = 0;
                int labelOffSet = 15;
                for (MetroLine metroLine : metroStop.get_intersectingLines())
                {
                    int totalLines = metroStop.get_intersectingLines().size();
                    g2d.setPaint(metroLine.get_color());
                    if (!metroLine.get_labelHasBeenDrawn())
                    {
                        labelOffSet += 15;
                        int poundSignPosition = metroLine.get_name().indexOf('#');
                        ArrayList<MetroLine> originalLines = null;
                        if (poundSignPosition > 0)
                        {
                            String originalLineName = metroLine.get_name().substring(0, poundSignPosition);
                            originalLines = metroLines.stream().filter(aLine -> aLine.get_name().length() > 2 && aLine.get_name().equals(originalLineName)).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
                        }
                        MetroLine originalLine = null;
                        if (originalLines != null && originalLines.size() != 0)
                            originalLine = originalLines.get(0);
                        if (originalLine == null)
                            g2d.drawString(metroLine.get_name(), metroStop.get_position().width, metroStop.get_position().height - labelOffSet);
                        metroLine.set_labelHasBeenDrawn(true);
                    }

                    g2d.drawArc(metroStop.get_position().width,
                            metroStop.get_position().height,
                            circleDiameter,
                            circleDiameter,
                            current, (360 / totalLines));
                    current += 360 / totalLines;
                }
                g2d.setPaint(Color.black);
                g2d.setFont(new Font("TimesRoman", Font.PLAIN, 12));
                TextLayout layout = new TextLayout(metroStop.get_name(), new Font("TimesRoman", Font.PLAIN, 12), g2d.getFontRenderContext());
                int textCenterW = (int)(layout.getBounds().getWidth() / 2);
                int textCenterH = (int)(layout.getBounds().getHeight() / 2);
                if (metroStop.get_mouseOver())
                {
                    g2d.setPaint(Color.WHITE);
                    int rectWidth = 400;
                    int rectHeight = 200;
                    g2d.fillRect(metroStop.get_position().width + (circleDiameter / 2) - rectWidth,
                                 metroStop.get_position().height + (circleDiameter / 2) - rectHeight,
                                 rectWidth, rectHeight);
                    g2d.setPaint(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(metroStop.get_position().width + (circleDiameter / 2) - rectWidth,
                                 metroStop.get_position().height + (circleDiameter / 2) - rectHeight,
                                 rectWidth, rectHeight);
                    g2d.setStroke(new BasicStroke(brushWidth));
                    g2d.drawString(metroStop.get_name(),
                            metroStop.get_position().width + (circleDiameter / 2) - rectWidth + 10, // - textCenterW,
                            metroStop.get_position().height + (circleDiameter / 2) - (rectHeight - textCenterH * 2) + 10); // - textCenterH);
                }
                g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                g2d.drawString(df.format(metroStop.get_date().getTime()), metroStop.get_position().width, h - 50);
            }
        }
    }

    private void SetSocketsForStop(MetroStop metroStop)
    {
        for (MetroLineDirection direction : metroStop.get_outgoingSockets())
            SetSocketForDirection(direction, metroStop.get_outgoingSockets(), metroStop);
        for (MetroLineDirection direction : metroStop.get_incommingSockets())
            SetSocketForDirection(direction, metroStop.get_incommingSockets(), metroStop);
    }
    private void SetSocketForDirection(MetroLineDirection direction, ArrayList<MetroLineDirection> allDirections, MetroStop metroStop)
    {
        int socketNumber = 0;
        if (direction.get_distance() > 0)
        {
            socketNumber = 1;
            for (MetroLineDirection otherDirection: allDirections)
            {
                if (otherDirection == direction)
                    continue;
                if (otherDirection.get_distance() > direction.get_distance() &&
                        otherDirection.get_socketNumber() > 0)
                {
                    otherDirection.increment_socketNumber();
                }
                else if (otherDirection.get_socketNumber() > 0)
                {
                    socketNumber++;
                }
            }
        }
        else if (direction.get_distance() < 0)
        {
            socketNumber = -1;
            for (MetroLineDirection otherDirection: allDirections)
            {
                if (otherDirection == direction)
                    continue;
                if (otherDirection.get_distance() < direction.get_distance() &&
                        otherDirection.get_socketNumber() < 0)
                {
                    otherDirection.decrement_socketNumber();
                }
                else if (otherDirection.get_socketNumber() < 0)
                {
                    socketNumber--;
                }
            }
        }
        direction.set_socketNumber(socketNumber);
    }

    private void SetStopPosition(HashSet<MetroLine> metroLines, int windowHeight, int margin, GregorianCalendar startDate, int granularityFactor)
    {
        java.time.Instant previousDate = new GregorianCalendar(2000, 1, 1).toInstant();
        int previousX = - granularityFactor;
        for (MetroStop metroStop: allMetroStops)
        {
            metroStop.clearSockets();
            //Calculations for stop position, color and creation of the metro line hashSet
            if (!metroStop.get_intersectingLines().isEmpty()) {
                int height = metroStop.get_intersectingLines().get(0).get_heightPosition();
                metroStop.set_color(metroStop.get_intersectingLines().get(0).get_color());
                for (MetroLine line : metroStop.get_intersectingLines()) {
                    if (line.get_heightPosition() - windowHeight / 2 == 0 ||
                            line.get_heightPosition() > windowHeight / 2 && line.get_heightPosition() < height ||
                            line.get_heightPosition() < windowHeight / 2 && line.get_heightPosition() > height) {
                        height = line.get_heightPosition();
                        metroStop.set_color(line.get_color());
                    }
                    if (!line.get_intersectingStops().contains(metroStop))
                    {
                        line.get_intersectingStops().add(metroStop);
                    }
                    metroLines.add(line);
                }
                int currentX = 0;
                SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
                String test1 = metroStop.get_date().toInstant().toString();
                String test2 = previousDate.toString();
                if (metroStop.get_date().toInstant().toString().substring(0, 10).equals(previousDate.toString().substring(0, 10)))
                {
                    currentX = previousX;
                }
                else
                {
                    previousDate = metroStop.get_date().toInstant();
                    currentX = previousX + granularityFactor;
                }
                metroStop.set_position(new Dimension((margin + currentX), height));
                previousX = currentX;
                //metroStop.set_position(new Dimension((int) (margin + (ChronoUnit.DAYS.between(startDate.toInstant(), metroStop.get_date().toInstant()) * granularityFactor)), height));
            }
        }
    }

    private void DrawLines(HashSet<MetroLine> metroLines, Graphics2D g2d, int brushWidth, int circleDiameter, int granularityFactor)
    {
        int metroLineNameOffset = 0;
        for (MetroLine metroLine: metroLines)
        {
            if (metroLine.get_name() != null) {
                g2d.setPaint(metroLine.get_color());
                g2d.setFont(new Font("TimesRoman", Font.BOLD, 16));
                int poundSignPosition = metroLine.get_name().indexOf('#');
                ArrayList<MetroLine> originalLines = null;
                if (poundSignPosition > 0)
                {
                    String originalLineName = metroLine.get_name().substring(0, poundSignPosition);
                    originalLines = metroLines.stream().filter(aLine -> aLine.get_name().length() > 2 && aLine.get_name().equals(originalLineName)).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
                }
                MetroLine originalLine = null;
                if (originalLines != null && originalLines.size() != 0)
                    originalLine = originalLines.get(0);
                if (originalLine == null) {
                    g2d.drawString(metroLine.get_name(), 5, 20 + metroLineNameOffset);
                    metroLineNameOffset += 25;
                }
                for (MetroStop metroStop : metroLine.get_intersectingStops()) {
                    if (metroLine.get_lastStop() != null) {
                        int startOffsetY = 0;
                        int endOffsetY = 0;
                        ArrayList<MetroLineDirection> outgoingSockets = metroLine.get_lastStop().get_outgoingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                        if (!outgoingSockets.isEmpty())
                            startOffsetY = brushWidth * outgoingSockets.get(0).get_socketNumber();
                        ArrayList<MetroLineDirection> incommingSockets = metroStop.get_incommingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                        if (!incommingSockets.isEmpty())
                            endOffsetY = brushWidth * incommingSockets.get(0).get_socketNumber();

                        //Start drawing
                        int startX = metroLine.get_lastStop().get_position().width + circleDiameter + brushWidth / 2,
                                startY = metroLine.get_lastStop().get_position().height + circleDiameter / 2 + startOffsetY,
                                endX = metroStop.get_position().width - brushWidth / 2,
                                endY = metroStop.get_position().height + circleDiameter / 2 + endOffsetY;
                        int arcWidth = granularityFactor / 2,
                                arcX = startX + endOffsetY + (granularityFactor / 2) - (circleDiameter / 2) - arcWidth,
                                arcY,
                                arcHeight,
                                arcStartAngle = 0,
                                arcMidAngle;
                        //If the two stops are the same height, just draw a straight line
                        if (startY == endY) {
                            ArrayList<MetroStop> stopsToGoArround = allMetroStops.stream().filter(stop -> stop.get_position().height == metroStop.get_position().height && stop.get_position().width > metroLine.get_lastStop().get_position().width && stop.get_position().width < metroStop.get_position().width).collect(Collectors.toCollection(() -> new ArrayList<MetroStop>()));
                            if (stopsToGoArround.size() == 0)
                            {
                                //if (endOffsetY < 0)
                                //    endOffsetY *= -1;
                                //for (MetroStop stop : stopsToGoArround) {
                                //g2d.draw(new Line2D.Float(startX, startY, stop.get_position().width - endOffsetY, endY));
                                //startX = stop.get_position().width + circleDiameter + endOffsetY;
                                //}
                                g2d.draw(new Line2D.Float(startX, startY, endX, endY));
                            }
                        }
                        //If the second stop is above the first, arc up
                        else if (startY < endY) {
                            ArrayList<MetroStop> stopsToGoArround = allMetroStops.stream().filter(stop -> stop.get_position().height == metroStop.get_position().height && stop.get_position().width > metroLine.get_lastStop().get_position().width && stop.get_position().width < metroStop.get_position().width).collect(Collectors.toCollection(() -> new ArrayList<MetroStop>()));

                            arcY = startY;
                            arcHeight = endY - startY;
                            arcMidAngle = 90;
                            if (stopsToGoArround.size() == 0)
                                g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                            if (stopsToGoArround.size() == 0)
                                g2d.draw(new Line2D.Float(startX, startY, arcX + (arcWidth / 2) - brushWidth, startY));
                            arcX += arcWidth;
                            arcY = endY - arcHeight;
                            arcStartAngle = -90;
                            arcMidAngle = -90;
                            startX = arcX + (arcWidth / 2) + brushWidth;
                            if (stopsToGoArround.size() == 0)
                                g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                            //if (endOffsetY < 0)
                            //   endOffsetY *= -1;
                            //for (MetroStop stop : stopsToGoArround) {
                            //    g2d.draw(new Line2D.Float(startX, endY, stop.get_position().width - endOffsetY, endY));
                            //    startX = stop.get_position().width + circleDiameter + endOffsetY;
                            //}
                            if (stopsToGoArround.size() == 0)
                                g2d.draw(new Line2D.Float(startX, endY, endX, endY));
                        }
                        //If the second stop is below the first, arc down
                        else if (startY > endY) {
                            ArrayList<MetroStop> stopsToGoArround = allMetroStops.stream().filter(stop -> stop.get_position().height == metroStop.get_position().height && stop.get_position().width > metroLine.get_lastStop().get_position().width && stop.get_position().width < metroStop.get_position().width).collect(Collectors.toCollection(() -> new ArrayList<MetroStop>()));
                            arcY = endY;
                            arcHeight = startY - endY;
                            arcMidAngle = -90;
                            if (stopsToGoArround.size() == 0)
                                g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                            if (stopsToGoArround.size() == 0)
                                g2d.draw(new Line2D.Float(startX, startY, arcX + (arcWidth / 2) - brushWidth, startY));
                            arcX += arcWidth;
                            arcY = startY - arcHeight;
                            arcStartAngle = 90;
                            arcMidAngle = 90;
                            startX = arcX + (arcWidth / 2) + brushWidth;
                            if (stopsToGoArround.size() == 0)
                                g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                            //if (endOffsetY < 0)
                            //    endOffsetY *= -1;
                            //for (MetroStop stop : stopsToGoArround) {
                            //    g2d.draw(new Line2D.Float(startX, endY, stop.get_position().width - endOffsetY, endY));
                            //    startX = stop.get_position().width + circleDiameter + endOffsetY;
                            //}
                            if (stopsToGoArround.size() == 0)
                                g2d.draw(new Line2D.Float(startX, endY, endX, endY));
                        }
                    }
                    else
                    {
                        if (originalLine != null)
                        {
                            MetroStop firstStop = null, secondStop = metroStop;
                            ArrayList<MetroStop> intersectingStops = new ArrayList<MetroStop>();
                            final MetroLine finalOriginalLine = originalLine;
                            for (MetroStop stop: allMetroStops)
                            {
                                if (stop.get_intersectingLines().stream().filter(line -> line.get_name() == finalOriginalLine.get_name()).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>())).size() != 0)
                                    intersectingStops.add(stop);
                            }
                            for (MetroStop stop: intersectingStops)
                            {
                                if (firstStop == null)
                                    firstStop = stop;
                                else if (firstStop.get_date().before(stop.get_date()) && stop.get_date().before(secondStop.get_date()))
                                    firstStop = stop;
                            }
                            g2d.setPaint(metroLine.get_color());
                            int arcWidth = granularityFactor / 2;
                            int arcHeight = firstStop.get_position().height - secondStop.get_position().height;
                            int startX = firstStop.get_position().width + circleDiameter + brushWidth / 2;
                            int startY = firstStop.get_position().height + circleDiameter / 2 - arcHeight;
                            int arcX = startX + (granularityFactor / 2) - (circleDiameter / 2) - arcWidth;
                            int arcY = startY;
                            int arcStartAngle = 0;
                            int arcMidAngle = -90;
                            if (arcHeight < 0)
                            {
                                arcHeight *= -1;
                                arcStartAngle = 0;
                                arcMidAngle = 90;
                                arcY -= arcHeight;
                            }
                            int startOffSetY = 0;
                            int endOffsetY = 0;
                            ArrayList<MetroLineDirection> outgoingSockets = firstStop.get_outgoingSockets().stream().filter(entry -> entry.get_metroLine() == finalOriginalLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                            if (outgoingSockets.isEmpty())
                                outgoingSockets = firstStop.get_outgoingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                            if (!outgoingSockets.isEmpty())
                                startOffSetY = brushWidth * outgoingSockets.get(0).get_socketNumber();
                            ArrayList<MetroLineDirection> incommingSockets = secondStop.get_incommingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                            if (!incommingSockets.isEmpty())
                                endOffsetY = brushWidth * incommingSockets.get(0).get_socketNumber();
                            arcY += startOffSetY;
                            arcHeight += endOffsetY;

                            g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);

                            arcX += arcWidth;
                            if (arcMidAngle == 90)
                            {
                                arcMidAngle = 90;
                                arcStartAngle = 180;
                                g2d.draw(new Line2D.Float(arcX - arcWidth / 2, arcY, firstStop.get_position().width + circleDiameter, arcY));
                                g2d.draw(new Line2D.Float(arcX + arcWidth / 2, arcY + arcHeight, secondStop.get_position().width, arcY + arcHeight));
                            }
                            else
                            {
                                arcMidAngle = -90;
                                arcStartAngle = 180;
                                g2d.draw(new Line2D.Float(arcX - arcWidth / 2, arcY + arcHeight, firstStop.get_position().width + circleDiameter, arcY + arcHeight));
                                g2d.draw(new Line2D.Float(arcX + arcWidth / 2, arcY, secondStop.get_position().width, arcY));
                            }
                            g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                        }
                    }
                    metroLine.set_lastStop(metroStop);
                }
            }
        }
    }

    private void DrawOrphanLines(HashSet<MetroLine> metroLines, Graphics2D g2d, int brushWidth, int circleDiameter, int granularityFactor, int margin)
    {
        for (MetroLine metroLine: metroLines)
        {
            g2d.setPaint(metroLine.get_color());
            for (MetroStop metroStop: metroLine.get_intersectingStops())
            {
                if (metroLine.get_lastStop() != null)
                {
                    ArrayList<MetroStop> stopsToGoArround = allMetroStops.stream().filter(stop -> stop.get_position().height == metroStop.get_position().height && stop.get_position().width > metroLine.get_lastStop().get_position().width && stop.get_position().width < metroStop.get_position().width).collect(Collectors.toCollection(() -> new ArrayList<MetroStop>()));
                    int endOffsetY = 0;
                    int startOffSetY = 0;
                    ArrayList<MetroLineDirection> incommingSockets = metroStop.get_incommingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                    ArrayList<MetroLineDirection> outgoingSockets = metroLine.get_lastStop().get_outgoingSockets().stream().filter(entry -> entry.get_metroLine() == metroLine).collect(Collectors.toCollection(() -> new ArrayList<MetroLineDirection>()));
                    if (!incommingSockets.isEmpty())
                        endOffsetY = brushWidth * incommingSockets.get(0).get_socketNumber();
                    if (!outgoingSockets.isEmpty())
                        startOffSetY = brushWidth * outgoingSockets.get(0).get_socketNumber();
                    if (!stopsToGoArround.isEmpty()) {
                        stopsToGoArround.sort(Comparator.comparing(MetroStop::get_date));
                        MetroStop firstStop = stopsToGoArround.get(0);
                        ArrayList<HiddenLane> existingHiddenLanes = null;
                        if (endOffsetY > 0)
                            existingHiddenLanes = hiddenLanes.stream().filter(lane -> lane.Y == metroStop.get_position().height + margin / 2).collect(Collectors.toCollection(() -> new ArrayList<HiddenLane>()));
                        else
                            existingHiddenLanes = hiddenLanes.stream().filter(lane -> lane.Y == metroStop.get_position().height - margin / 2).collect(Collectors.toCollection(() -> new ArrayList<HiddenLane>()));
                        HiddenLane existingHiddenLane = null;
                        if (existingHiddenLanes.size() != 0)
                            existingHiddenLane = existingHiddenLanes.get(0);
                        if (existingHiddenLane == null)
                            if (endOffsetY > 0) {
                                existingHiddenLane = new HiddenLane(metroStop.get_position().height + margin / 2, firstStop.get_position().width, metroStop.get_position().width, metroLine);
                                hiddenLanes.add(existingHiddenLane);
                            } else {
                                existingHiddenLane = new HiddenLane(metroStop.get_position().height - margin / 2, firstStop.get_position().width, metroStop.get_position().width, metroLine);
                                hiddenLanes.add(existingHiddenLane);
                            }
                        else
                            existingHiddenLane.AddMetroLine(metroLine);
                        int myLinePosition = existingHiddenLane.intersectingLines.stream().filter(line -> line.line == metroLine).collect(Collectors.toCollection(() -> new ArrayList<LineNumber>())).get(0).number;


                        int arcWidth = granularityFactor / 2,
                                arcX = metroLine.get_lastStop().get_position().width + circleDiameter / 2 - brushWidth * 2,
                                arcY,
                                arcHeight,
                                arcStartAngle = 0,
                                arcMidAngle;
                        int laneY = existingHiddenLane.Y + myLinePosition * brushWidth;
                        int stopY = metroLine.get_lastStop().get_position().height + (circleDiameter / 2) + startOffSetY;

                        boolean heightWasNegative = false;
                        arcHeight = laneY - stopY;
                        if (arcHeight > 0)
                        {
                            arcY = stopY;
                            arcMidAngle = 90;
                        }
                        else
                        {
                            arcY = laneY;
                            arcMidAngle = -90;
                            arcHeight *= -1;
                            heightWasNegative = true;
                        }

                        // Try to attach lines with socket number != 0 to the circle with small lines
                        if (myLinePosition != 0)
                        {
                            int posValue = myLinePosition * brushWidth;
                            if (posValue < 0)
                                posValue *= -1;
                            double pythagorasWidth = circleDiameter / 2 - Math.sqrt(Math.pow(circleDiameter / 2, 2) - Math.pow(posValue, 2));
                            int lineX1 = (int)(metroLine.get_lastStop().get_position().width + circleDiameter + pythagorasWidth);
                            int lineX2 = (int)(metroLine.get_lastStop().get_position().width + circleDiameter);
                            int lineY = (int)(metroLine.get_lastStop().get_position().height + circleDiameter / 2 + startOffSetY);
                            g2d.drawLine(lineX1, lineY, lineX2, lineY);
                        }

                        g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                        //g2d.drawRect(arcX, arcY, arcWidth, arcHeight);
                        int arcX2 = (metroStop.get_position().width - arcWidth - brushWidth) - (arcWidth / 2);
                        int stopY2 = metroStop.get_position().height + (circleDiameter / 2) + endOffsetY;
                        int arcMidAngle2 = 90;
                        int arcHeight2 = laneY - stopY2;
                        if (arcHeight2 < 0)
                            arcHeight2 *= -1;
                        int arcY2 = laneY;
                        if (laneY > metroStop.get_position().height)
                        {
                            arcMidAngle2 = -90;
                            arcY2 -= arcHeight2;
                        }
                        g2d.drawArc(arcX2, arcY2, arcWidth, arcHeight2, arcStartAngle, arcMidAngle2);
                        arcX += arcWidth;
                        arcMidAngle = 90;
                        if (heightWasNegative)
                        {
                            arcStartAngle = 180;
                            arcMidAngle = -90;
                        }
                        else
                        {
                            arcStartAngle = 180;
                        }
                        arcX2 += arcWidth;
                        g2d.drawArc(arcX, arcY, arcWidth, arcHeight, arcStartAngle, arcMidAngle);
                        g2d.drawArc(arcX2, arcY2, arcWidth, arcHeight2, arcStartAngle, arcMidAngle2);
                        //g2d.drawRect(arcX, arcY, arcWidth, arcHeight);
                        g2d.drawLine(metroStop.get_position().width - arcWidth - brushWidth, existingHiddenLane.Y + myLinePosition * brushWidth, metroLine.get_lastStop().get_position().width + circleDiameter + arcWidth + brushWidth, existingHiddenLane.Y + myLinePosition * brushWidth);
                    }
                }
                metroLine.set_lastStop(metroStop);
            }
            metroLine.set_lastStop(null);
        }
    }
}