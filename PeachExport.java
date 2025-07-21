import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeachExport {

    private static class OrderData {
        String groupOrderNum = "Not Found";
        String username = "Not Found";
        String itemName = "Not Found";
        String quantity = "Not Found";
        String price = "Not Found";
        String dateTime = "Not Found";
        String deliveryId = "Not Found";
        String contact = "Not Found";
        String address = "Not Found";
        String filename = "Not Found";

        public String toCsvRow() {
            return String.join(",",
                    escapeCsv(groupOrderNum),
                    escapeCsv(username),
                    escapeCsv(itemName),
                    escapeCsv(quantity),
                    escapeCsv(price),
                    escapeCsv(dateTime),
                    escapeCsv(deliveryId),
                    escapeCsv(contact),
                    escapeCsv(address),
                    escapeCsv(filename)
            );
        }

        private String escapeCsv(String data) {
            if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
                return "\"" + data.replace("\"", "\"\"") + "\"";
            }
            return data;
        }
    }

    private static String findMatch(String text, String regex, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(group).trim();
        }
        return "Not Found";
    }

    public static OrderData parseOrder(String orderText, String filename) {
        OrderData data = new OrderData();
        data.filename = filename;

        int startIndex = orderText.indexOf("跟团号:");
        if (startIndex == -1) {
            return data;
        }

        int endIndex = orderText.indexOf("退款", startIndex);
        String orderBlock = (endIndex == -1) ?
                orderText.substring(startIndex) :
                orderText.substring(startIndex, endIndex);

        data.groupOrderNum = findMatch(orderBlock, "跟团号:\\s*(\\d+)", 1);

        Pattern userItemPattern = Pattern.compile("已(?:收|提)货\\s+(.+?)\\s+((?:特质|金桃|特级).*?)\\s*\\+\\d+件");
        Matcher userItemMatcher = userItemPattern.matcher(orderBlock);
        if (userItemMatcher.find()) {
            data.username = userItemMatcher.group(1).replaceAll("[@#]$", "").trim();
            data.itemName = userItemMatcher.group(2).replaceAll("\\(已全部核销\\)", "").trim();
        }

        data.quantity = findMatch(orderBlock, "\\+(\\d+)件", 1);
        data.price = findMatch(orderBlock, "(?:实收\\S*?|半|芈|4)(\\d+)", 1);
        data.dateTime = findMatch(orderBlock, "(\\d{4}/\\d{1,2}/\\d+\\s?\\d*[:;.]\\d+)", 1);
        data.deliveryId = findMatch(orderBlock, "(?:顺丰快递|圆通快递|京东配送):\\s*([A-Z0-9]+)", 1);
        data.contact = findMatch(orderBlock, "((?:[\\u4e00-\u9fa5a-zA-Z]+[*xX]?\\s*)?\\d{3}[*]{4}\\d{4})", 1);
        if (data.contact.equals("Not Found")) {
            data.contact = findMatch(orderBlock, "(鹤\\*\\d{3}\\*\\S+\\d{4})", 1);
        }
        data.address = findMatch(orderBlock, "((?:\\S+省)?\\S+市\\S+(?:区|县)\\*+)", 1);

        return data;
    }

    public static void main(String[] args) {
        Path currentPath = Paths.get("").toAbsolutePath();
        File txtDir = new File(currentPath.toFile(), "txt");
        File outputFile = new File(currentPath.toFile(), "output.csv");

        if (!txtDir.exists() || !txtDir.isDirectory()) {
            System.err.println("Error: Directory 'txt' not found in the current working directory.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            if (outputFile.length() == 0) {
                writer.println("groupOrderNum,username,itemName,quantity,price,dateTime,deliveryId,contact,address,filename");
            }

            File[] txtFiles = txtDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

            if (txtFiles == null || txtFiles.length == 0) {
                System.out.println("No .txt files found in the 'txt' directory.");
                return;
            }

            for (File file : txtFiles) {
                try {
                    String content = Files.readString(file.toPath());
                    OrderData data = parseOrder(content, file.getName());
                    writer.println(data.toCsvRow());
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
            System.out.println("Processing complete. Data appended to " + outputFile.getName());

        } catch (IOException e) {
            System.err.println("An error occurred while writing to output.csv: " + e.getMessage());
        }
    }
}