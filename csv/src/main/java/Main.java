import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;


public class Main {

  public static void main(String[] args) throws Exception {
    // doCSVVisitor();

    doCSVListener();
  }

  public static void doCSVVisitor() {
    // 创建 ANTLR4 文件输入流
    String path = "D:\\project\\grammars-v4\\csv\\examples\\example1.csv";
    ANTLRInputStream inputStream = new ANTLRInputStream(FileUtil.readUtf8String(path));

    // 创建 CSV 词法解析器
    CSVLexer csvLexer = new CSVLexer(inputStream);

    // 创建 Token 字符流
    CommonTokenStream commonTokenStream = new CommonTokenStream(csvLexer);

    // 创建 CSV 语法解析器
    CSVParser csvParser = new CSVParser(commonTokenStream);

    // 创建 CSV Visitor 访问器 (继承 CSVBaseVisitor 或者 CSVBaseListener)
    CustomCSVBaseVisitor csvBaseVisitor = new CustomCSVBaseVisitor();

    // 开始解析 (从 CSV.g4 的 file 规则开始)
    csvBaseVisitor.visit(csvParser.csvFile());

    System.out.println(csvBaseVisitor.getHeader());
    System.out.println(csvBaseVisitor.getRows());
  }

  public static void doCSVListener() throws Exception {
    String path = "D:\\project\\grammars-v4\\csv\\examples\\example1.csv";
    ANTLRInputStream inputStream = new ANTLRInputStream(FileUtil.readUtf8String(path));

    // 创建 CSV 词法解析器
    CSVLexer csvLexer = new CSVLexer(inputStream);

    // 创建 Token 字符流
    CommonTokenStream commonTokenStream = new CommonTokenStream(csvLexer);

    // 创建 CSV 语法解析器
    CSVParser csvParser = new CSVParser(commonTokenStream);

    // 创建 CSV Visitor 访问器 (继承 CSVBaseVisitor 或者 CSVBaseListener)
    CustomCSVBaseListener csvBaseListener = new CustomCSVBaseListener();

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(csvBaseListener, csvParser.csvFile());

    System.out.println(csvBaseListener.header);
    System.out.println(csvBaseListener.rows);
  }

  static class CustomCSVBaseVisitor extends CSVBaseVisitor<Object> {

    // 定义 CSV header 信息
    private final List<String> header = new ArrayList<>();

    public List<String> getHeader() {
      return header;
    }

    private final List<Map<String, String>> rows = new ArrayList<>();

    public List<Map<String, String>> getRows() {
      return rows;
    }

    @Override
    public Object visitCsvFile(CSVParser.CsvFileContext ctx) {
      System.out.println("读取 csv 文件");
      return super.visitCsvFile(ctx);
    }

    @Override
    public Object visitHdr(CSVParser.HdrContext ctx) {
      System.out.println("读取 csv 文件头部信息");
      List<CSVParser.FieldContext> headerFieldContexts = ctx.row().field();
      for (CSVParser.FieldContext headerFieldContext : headerFieldContexts) {
        header.add(headerFieldContext.getText());
      }
      return super.visitHdr(ctx);
    }

    @Override
    public Object visitRow(CSVParser.RowContext ctx) {
      System.out.println("读取 csv 文件一行信息");
      if (ctx.parent instanceof CSVParser.HdrContext) {
        System.out.println("读取 csv 文件一行信息 (忽略头部信息)");
      } else {
        List<CSVParser.FieldContext> fieldContexts = ctx.field();
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < fieldContexts.size(); i++) {
          row.put(String.valueOf(i), fieldContexts.get(i).getText());
        }

        rows.add(row);
      }

      return super.visitRow(ctx);
    }

    @Override
    public Object visitField(CSVParser.FieldContext ctx) {
      return super.visitField(ctx);
    }
  }

  static class CustomCSVBaseListener extends CSVBaseListener {
    private static final String EMPTY = "";
    private final List<Map<String, String>> rows = new ArrayList<Map<String, String>>(16);
    private final List<String> header = new ArrayList<String>(16);
    private final List<String> currentRow = new ArrayList<String>(16);

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterCsvFile(CSVParser.CsvFileContext ctx) {
      super.enterCsvFile(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitCsvFile(CSVParser.CsvFileContext ctx) {
      super.exitCsvFile(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterHdr(CSVParser.HdrContext ctx) {
      super.enterHdr(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitHdr(CSVParser.HdrContext ctx) {
      header.addAll(currentRow);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterRow(CSVParser.RowContext ctx) {
      currentRow.clear();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitRow(CSVParser.RowContext ctx) {
      // 判断当前 RowContext 的父节点是否是 HdrContext
      if (ctx.getParent() instanceof CSVParser.HdrContext) {
        return;
      }
      Map<String, String> line = new HashMap<>(16);
      for (int i = 0; i < header.size(); i++) {
        line.put(header.get(i), currentRow.get(i));
      }
      rows.add(line);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterField(CSVParser.FieldContext ctx) {
      super.enterField(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitField(CSVParser.FieldContext ctx) {
      currentRow.add(ctx.getText());
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void visitTerminal(TerminalNode node) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
    }
  }
}
