package sphero.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Builder class to create a Configuration instance
 * either manually, or by using an input file

 * Example for manual configuration:
 * Configuration c = new ConfigurationBuilder().withMap(...).withStart(...).withDestinations(...).create();
 */
public class ConfigurationBuilder {

    private Map map;
    private Location start;
    private List<Location> locationList = new ArrayList<>();


    /**
     * Builds a configuration using an input file
     */
    public ConfigurationBuilder fromFile(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException();

        FileInputStream fileStream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fileStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(file.getAbsoluteFile())));

        int sizeX = 0;
        int sizeY = 0;
        if (scanner.hasNextLine()) sizeX = Integer.parseInt(scanner.nextLine().trim());
        if (scanner.hasNextLine()) sizeY = Integer.parseInt(scanner.nextLine().trim());
        map = new Map(sizeX, sizeY, 1.0, false);

        int currentPosX = -1;
        int currentPosY = 0;
        int txtRow = 0;

        String strLine;
        while ((strLine = br.readLine()) != null) {
            System.out.println(strLine);
            if (txtRow == 0) {
                txtRow++;
                continue;
            }

            //Line with positions and horizontal transitions.
            if (txtRow % 2 == 1) {
                for (int j = 1; j < strLine.length() - 1; j++) {
                    if (strLine.charAt(j) == Constants.GOALS) {
                        Field dest = map.getField(currentPosX, currentPosY);
                        locationList.add(new Location(dest, Location.Type.DESTINATION));
                    } else if (strLine.charAt(j) == Constants.ROBOT) {
                        Field f = map.getField(currentPosX, currentPosY);
                        start = new Location(f, Location.Type.START);
                        locationList.add(start);
                    } else if (strLine.charAt(j) == Constants.TRANSITION) {
                        Field a = map.getField(currentPosX, currentPosY - 1);
                        Field b = map.getField(currentPosX, currentPosY);
                        map.connect(a, b);
                    }
                    if (strLine.charAt(j) == Constants.FREE || strLine.charAt(j) == Constants.ROBOT || strLine.charAt(j) == Constants.GOALS) {
                        currentPosY++;
                    }
                }
                currentPosX++;
                currentPosY = 0;
            }

            // Line with vertical transitions
            else if (txtRow % 2 == 0) {
                for (int j = 1; j < strLine.length() - 1; j++) {
                    if (strLine.charAt(j) == Constants.TRANSITION) {
                        Field a = map.getField(currentPosX, currentPosY);
                        Field b = map.getField(currentPosX - 1, currentPosY);
                        map.connect(a, b);
                    }
                    if (strLine.charAt(j) == Constants.WALL || strLine.charAt(j) == Constants.TRANSITION) {
                        currentPosY++;
                    }
                }
                currentPosY = 0;
            }
            txtRow++;
        }
        in.close();
        return this;
    }


    public ConfigurationBuilder withMap(Map map) {
        this.map = map;
        return this;
    }

    public ConfigurationBuilder withStart(Location start) {
        this.start = start;
        return this;
    }

    public ConfigurationBuilder withLocations(List<Location> locationList) {
        this.locationList = locationList;
        return this;
    }

    public Map getMap() {
        return map;
    }

    public Location getStart() {
        return start;
    }

    public List<Location> getLocations() {
        return locationList;
    }

    public Configuration create() {
        if(map == null || start == null || locationList == null || locationList.isEmpty())
            throw new IllegalStateException("Konfiguration kann nicht erstellt werden: Daten fehlerhaft oder unvollständig");
        return new Configuration(this);
    }

}
