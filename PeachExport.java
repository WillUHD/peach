import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

public class PeachExport {
    private static final String NOT_FOUND = "Not Found";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("(?:已发货|已收货|已支付)\\s+(.*?)\\s+(?=最好吃的炎陵黄桃发售啦！|查看>)");
    private static final Pattern GROUP_ORDER_NUM_PATTERN = Pattern.compile("跟团号：(\\d+)");
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("(\\d{4}/\\d{1,2}/\\d{1,2}\\s*\\d{1,2}:\\d{2})");
    private static final Pattern DELIVERY_ID_PATTERN = Pattern.compile("(?:顺丰快递|圆通快递|京东配送)[:：]\\s*([A-Z0-9]+)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{11}|\\d{3}\\*+\\d{4})");
    private static final Pattern SHIPPING_BLOCK_PATTERN = Pattern.compile("(?:(?:十|\\+)\\s*(?:继续)?添加物流信息)\\s*(.*?)(?:团长备注|团员备注|疑似异常地址|复制订单|退款|$)");
    private static final Pattern MULTI_ITEM_PATTERN = Pattern.compile("((?:(?:十斤|五斤)(?:普装|精装)黄桃?))\\s*\\+\\s*(\\d+)\\s*件，\\s*[￥¥](\\d+)");
    private static final Pattern SINGLE_ITEM_NAME_PATTERN = Pattern.compile("(?:查看>|>)((?:十斤|五斤)(?:普装|精装)黄桃)");
    private static final Pattern SINGLE_QUANTITY_PATTERN = Pattern.compile("共(\\d+)件");
    private static final Pattern SINGLE_PRICE_PATTERN = Pattern.compile("实收[￥¥](\\d+)");

    private record OrderData(
        String groupOrderNum, String username, String itemName,
        String quantity, String price, String dateTime,
        String deliveryId, String contact, String address, String filename
    ) {
        public String toCSVRow() {
            return Stream.of(groupOrderNum, username, itemName, quantity, price,
                             dateTime, deliveryId, contact, address, filename)
                         .map(this::escapeCsv)
                         .collect(Collectors.joining(","));
        }

        private String escapeCsv(String data) {
            if (data == null || NOT_FOUND.equals(data) || data.isBlank())
                return "\"" + NOT_FOUND + "\"";
            if (data.contains(",") || data.contains("\"") || data.contains("\n"))
                return "\"" + data.replace("\"", "\"\"") + "\"";
            
            return data;
        }
    }

    private static String findMatch(String text, Pattern pattern, int group) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group).trim() : NOT_FOUND;
    }

    public static List<OrderData> parseOrder(String fullOcrText, String filename) {
        // Pre-process to extract only the text between the first "跟团号：" and its subsequent "退款"
        int startIndex = fullOcrText.indexOf("跟团号：");
        if (startIndex == -1) {
            return Collections.emptyList(); // No valid order start found
        }
        int endIndex = fullOcrText.indexOf("退款", startIndex);
        if (endIndex == -1) {
            return Collections.emptyList(); // No valid order end found
        }
        String orderText = fullOcrText.substring(startIndex, endIndex + "退款".length());

        String singleLineText = orderText.replaceAll("\\s+", " ");
        String username = findMatch(singleLineText, USERNAME_PATTERN, 1);
        String groupOrderNum = findMatch(singleLineText, GROUP_ORDER_NUM_PATTERN, 1);
        String dateTime = findMatch(singleLineText, DATE_TIME_PATTERN, 1);

        List<String> deliveryIds = new ArrayList<>();
        Matcher deliveryMatcher = DELIVERY_ID_PATTERN.matcher(singleLineText);
        while (deliveryMatcher.find()) {
            deliveryIds.add(deliveryMatcher.group(1));
        }
        String deliveryId = deliveryIds.isEmpty() ? NOT_FOUND : String.join(" | ", deliveryIds);

        String contact = NOT_FOUND;
        String address = NOT_FOUND;
        String shippingBlock = findMatch(singleLineText, SHIPPING_BLOCK_PATTERN, 1);

        if (!NOT_FOUND.equals(shippingBlock) && !shippingBlock.trim().isEmpty()) {
            Matcher phoneMatcher = PHONE_PATTERN.matcher(shippingBlock);
            if (phoneMatcher.find()) {
                String phoneNumber = phoneMatcher.group(1);
                String namePart = shippingBlock.substring(0, phoneMatcher.start()).trim();
                namePart = namePart.replaceFirst("^\\d+\\s*", "");
                String addressPart = shippingBlock.substring(phoneMatcher.end()).trim();
                contact = namePart.isEmpty() ? phoneNumber : namePart + " " + phoneNumber;
                address = addressPart.replaceFirst("^([?？口门]\\s*)+", "").trim();
            } else {
                address = shippingBlock.replaceFirst("^([?？口门]\\s*)+", "").trim();
            }
        }

        List<OrderData> parsedOrders = new ArrayList<>();
        Matcher itemMatcher = MULTI_ITEM_PATTERN.matcher(singleLineText);
        
        boolean foundMultiItems = false;
        while (itemMatcher.find()) {
            foundMultiItems = true;
            parsedOrders.add(new OrderData(
                groupOrderNum, username, itemMatcher.group(1).trim(),
                itemMatcher.group(2).trim(), itemMatcher.group(3).trim(),
                dateTime, deliveryId, contact, address, filename
            ));
        }
        
        if (!foundMultiItems && !NOT_FOUND.equals(groupOrderNum)) {
             String itemName = findMatch(singleLineText, SINGLE_ITEM_NAME_PATTERN, 1);
             if (!NOT_FOUND.equals(itemName)) {
                parsedOrders.add(new OrderData(
                    groupOrderNum, username,
                    itemName,
                    findMatch(singleLineText, SINGLE_QUANTITY_PATTERN, 1),
                    findMatch(singleLineText, SINGLE_PRICE_PATTERN, 1),
                    dateTime, deliveryId, contact, address, filename
                ));
            }
        }
        return parsedOrders;
    }

    public static void main(String[] args) {
        System.out.println("""

        ███████╗██╗  ██╗██████╗  ██████╗ ████by╗ WillUHD█╗
        ██╔════╝╚██╗██╔╝██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝
        PEACH╗   ╚███╔╝ EXPORT╔╝v8║   ██║██████╔╝   ██║   
        ██╔══╝   ██╔██╗ ██╔═══╝ ██║   ██║██╔══██╗   ██║   
        ███████╗██╔╝ ██╗██║     ╚██████╔╝██║  ██║   ██║   
        ╚══════╝╚═╝  ╚═╝╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   
            PeachExport is starting up... Please wait
        """);

        if (args.length < 2) {
            System.err.println("Usage: java PeachExport <input_directory> <output_filename_no_extension>");
            return;
        }

        System.out.println("Reading arguments... ");
        Path txtDirPath = Paths.get(args[0]);
        Path outputFilePath = Paths.get(args[1] + ".csv");

        if (!Files.isDirectory(txtDirPath)) {
            System.err.println("Error: Directory '" + args[0] + "' not found or is not a directory.");
            return;
        }

        File[] txtFiles = txtDirPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (txtFiles == null || txtFiles.length == 0) {
            System.out.println("No .TXT files found in the '" + args[0] + "' directory.");
            return;
        }

        System.out.println("Converting data using virtual threads...");
        System.out.println("Begin parsing " + txtFiles.length + " files.");
        long start = System.currentTimeMillis();
        List<OrderData> allData = new CopyOnWriteArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (File file : txtFiles) {
                executor.submit(() -> {
                    try {
                        String content = Files.readString(file.toPath());
                        allData.addAll(parseOrder(content, file.getName()));
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName() + " - " + e.getMessage());
                    }
                });
            }
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFilePath))) {
            writer.println("跟团号,用户,商品,数量,价格,时间,快递号,联系方式,地址,FILENAME");
            allData.stream()
                   .sorted(Comparator.comparing(o -> {
                       try {return Integer.parseInt(o.groupOrderNum());}
                       catch (NumberFormatException e) {return Integer.MAX_VALUE;}
                   }))
                   .forEach(data -> writer.println(data.toCSVRow()));
        } catch (IOException e) {
            System.err.println("An error occurred while writing to " + outputFilePath.getFileName() + ": " + e.getMessage());
        }

        System.out.println("\nProcessing completed in " + ((System.currentTimeMillis() - start) / 1000f) +
                           "s. \nData written to " + outputFilePath.getFileName());
    }
}