import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String [] args)
    {
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(1800, 800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Metro Map");
        GUI gui = new GUI();

        JScrollPane jscrollpane = new JScrollPane(gui);
        frame.add(jscrollpane, BorderLayout.CENTER);

        gui.setLayout(null);
        JTextArea inputLabel = new JTextArea("Search:");
        inputLabel.setLocation(300, 10);
        inputLabel.setEditable(false);
        inputLabel.setSize(new Dimension(100, 18));
        inputLabel.setBackground(new Color(0, 0, 0, 0));
        gui.add(inputLabel);

        JTextField inputArea = new JTextField();
        inputArea.setLocation(350, 10);
        inputArea.setEditable(true);
        inputArea.setSize(new Dimension(400, 18));
        inputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inputArea.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                int test = 1;
                CreateMetroMap(e.getActionCommand(), gui, jscrollpane);
            }
        });
        gui.add(inputArea);
        frame.pack();
        frame.setVisible(true);
    }

    private static void CreateMetroMap(String searchString, GUI gui, JScrollPane jscrollpane)
    {
        try
        {
            //ProcessBuilder pb = new ProcessBuilder("python","/home/michael/TextAnalyzer/GUITest.py", searchString);
            //Process p = pb.start();
            File input_file = new File("/home/michael/TextAnalyzer/Metromap_generation/Search_documents/" + searchString + ".txt");
            BufferedReader in = new BufferedReader(new FileReader(input_file));
            ArrayList<MetroLine> allMetroLines = new ArrayList<MetroLine>();
            gui.allMetroStops = new ArrayList<MetroStop>();
            int numberOfStops = Integer.valueOf(in.readLine());
            for (int stopNumber = 1; stopNumber <= numberOfStops; stopNumber++)
            {
                int numberOfTopics = Integer.valueOf(in.readLine());
                String metroStopName = "";
                for (int topicNumber = 0; topicNumber < numberOfTopics; topicNumber++)
                {
                    metroStopName += in.readLine() + '\n';
                }
                int year = Integer.valueOf(in.readLine()), month = Integer.valueOf(in.readLine()), day = Integer.valueOf(in.readLine());
                MetroStop newMetroStop = new MetroStop(metroStopName, new GregorianCalendar(year, month - 1, day));
                gui.allMetroStops.add(newMetroStop);
                int numberOfLines = Integer.valueOf(in.readLine());
                for (int lineNumber = 1; lineNumber <= numberOfLines; lineNumber++)
                {
                    MetroLine currentLine = null;
                    String name = in.readLine();
                    ArrayList<MetroLine> existingMetroLine = allMetroLines.stream().filter(aLine -> aLine.get_name().equals(name)).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
                    if (existingMetroLine.size() > 0)
                        currentLine = existingMetroLine.get(0);
                    else
                    {
                        Random rand = new Random();
                        int poundSignPosition = name.indexOf('#');
                        currentLine = new MetroLine(name, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
                        if (poundSignPosition > 0)
                        {
                            String originalLineName = name.substring(0, poundSignPosition);
                            ArrayList<MetroLine> originalLines = allMetroLines.stream().filter(aLine -> aLine.get_name().length() > 2 && aLine.get_name().equals(originalLineName)).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
                            MetroLine originalLine = null;
                            if (originalLines != null && originalLines.size() != 0)
                                originalLine = originalLines.get(0);
                            if (originalLine != null)
                                currentLine = new MetroLine(name, originalLine.get_color());
                        }
//                        ArrayList<MetroLine> originalLines = allMetroLines.stream().filter(aLine -> aLine.get_name().length() > 3 && aLine.get_name().equals(name.substring(0, name.length() - 3))).collect(Collectors.toCollection(() -> new ArrayList<MetroLine>()));
//                        MetroLine originalLine = null;
//                        if (originalLines != null && originalLines.size() != 0)
//                            originalLine = originalLines.get(0);
//                        if (originalLine != null)
//                            currentLine = new MetroLine(name, originalLine.get_color());
//                        else
//                            currentLine = new MetroLine(name, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
                        allMetroLines.add(currentLine);
                    }
                    if (newMetroStop.get_intersectingLines().size() < 5)
                        newMetroStop.get_intersectingLines().add(currentLine);
                }
            }
            GregorianCalendar startDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, 1, 1);
            GregorianCalendar endDate = new GregorianCalendar(1, 1, 1);
            for (MetroStop metroStop: gui.allMetroStops)
            {
                if (metroStop.get_date().before(startDate))
                    startDate = metroStop.get_date();
                if (metroStop.get_date().after(endDate))
                    endDate = metroStop.get_date();
            }
            long startTime = startDate.getTime().getTime();
            long endTime = endDate.getTime().getTime();
            long timeDifference = endTime - startTime;
            long timeDifferenceInDays = timeDifference / (1000 * 60 * 60 * 24);
            int granularityInDays = 1;
            int distancePerGranularity = 300;
            int height = 375 + (allMetroLines.size() * 220);
            int widthForStops = 0;
            ArrayList<String> allDates = new ArrayList<String>();
            for (MetroStop metroStop: gui.allMetroStops)
            {
                if (!allDates.contains(metroStop.get_date().getTime().toString()))
                {
                    widthForStops += distancePerGranularity;
                    allDates.add(metroStop.get_date().getTime().toString());
                }
            }
            int width = 500 + widthForStops;
            //int width = 675 + (granularityInDays * (int)timeDifferenceInDays * distancePerGranularity);
            gui.setPreferredSize(new Dimension(width, height));
            jscrollpane.updateUI();
            gui.repaint();

            gui.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int circleDiameter = 100;
                    super.mouseMoved(e);
                    boolean changeOccured = false;
                    for (MetroStop metroStop : gui.allMetroStops)
                    {
                        if (e.getX() < metroStop.get_position().width + circleDiameter && e.getX() > metroStop.get_position().width &&
                                e.getY() < metroStop.get_position().height + circleDiameter && e.getY() > metroStop.get_position().height)
                        {
                            metroStop.set_mouseOver(true);
                            changeOccured = true;
                        }
                        else if (metroStop.get_mouseOver())
                        {
                            metroStop.set_mouseOver(false);
                            changeOccured = true;
                        }

                    }
                    if (changeOccured)
                        gui.repaint();
                }
            });
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
        gui.repaint();
    }
}
