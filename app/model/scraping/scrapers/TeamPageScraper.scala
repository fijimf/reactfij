import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

public class Junk {

    public static void main(String[] args) {
        try {



            File f = new File("src/main/resources/junk2.html");
            final Document d = Jsoup.parse(f, "UTF-8", "http://www.ncaa.com/schools/georgetown/basketball-men");

            extractMeta(d);
            extractTables(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractMeta(Document d) {
        final Elements elements = d.select("li.school-info");
        for (Element element : elements) {
            final Elements labels = element.select("span.school-meta-label");
            if (!labels.isEmpty()){
                final String key = labels.get(0).ownText().trim();
                final String value = element.ownText();
                System.err.println(key+"->"+value);
            }
        }
    }
    private static void extractTables(Document d) {
        final Elements tables = d.select("table.ncaa-schools-sport-table");
        for (Element table : tables) {
            final Elements ths = table.select("thead");
            if (!ths.isEmpty()) {
                final Elements cols = ths.get(0).select("tr th");
                if ((cols.size() > 2) && cols.get(1).ownText().equalsIgnoreCase("name") && cols.get(2).ownText().equalsIgnoreCase("position")){
                    handlePlayerTable(table);
                }
                else if ((cols.size() > 2) && cols.get(0).ownText().equalsIgnoreCase("date") && cols.get(1).ownText().equalsIgnoreCase("opponent")){
                    handleGameTable(table);
                } else {
                    System.err.println("Unknown table");
                }
            }
        }

    }

    private static void handlePlayerTable(Element table) {
        final Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            final Elements cells = row.select("td");
            if (cells.size()>1) {
                String number = cells.get(0).ownText();
                String name = cells.get(1).ownText();
                String pos = (cells.size() > 2) ? cells.get(2).ownText() : "";
                String hgt = (cells.size() > 3) ? cells.get(3).ownText() : "";
                String cls = (cells.size() > 4) ? cells.get(4).ownText() : "";
                System.err.println(number+" "+name+ " "+pos);
            }
        }
    }

    private static void handleGameTable(Element table) {
        final Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            final Elements cells = row.select("td");
            if (cells.size()>1) {
                String date = cells.get(0).ownText();
                String ha = cells.get(1).ownText();
                final Elements oppLink = cells.get(1).select("a");
                String oppKey = oppLink.get(0).attr("href").replace("/schools/","");
                String oppName = oppLink.get(0).ownText();

                System.err.println(date+" "+ha+" "+oppKey+" "+oppName);
            }
        }
    }


}
