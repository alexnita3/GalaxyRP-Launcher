package com.galaxyrp.galaxyrplauncher.services;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.ThematicBreak;

import java.util.ArrayList;
import java.util.List;

public class NewsMarkdownRenderer {
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LINK_COLOR = Color.web("#8ab4f8");
    private static final Color QUOTE_COLOR = Color.LIGHTGRAY;

    private final ScrollPane scrollPane;
    private final List<ImageView> images = new ArrayList<>();
    private VBox content;

    public NewsMarkdownRenderer(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        scrollPane.viewportBoundsProperty().addListener(
                (observable, oldBounds, newBounds) -> resizeImages());
    }

    public void render(Node document) {
        content = new VBox(8);
        content.getStyleClass().add("news-content");
        content.setFillWidth(true);
        images.clear();
        renderBlocks(document, content);
        scrollPane.setContent(content);
        resizeImages();
    }

    private void renderBlocks(org.commonmark.node.Node parent, VBox target) {
        for (org.commonmark.node.Node node = parent.getFirstChild();
             node != null;
             node = node.getNext()) {
            if (node instanceof Paragraph paragraph) {
                target.getChildren().add(renderInline(paragraph));
            } else if (node instanceof Heading heading) {
                TextFlow headingFlow = renderInline(heading, headingFontSize(heading.getLevel()));
                headingFlow.getStyleClass().add("news-heading-" + heading.getLevel());
                target.getChildren().add(headingFlow);
            } else if (node instanceof FencedCodeBlock codeBlock) {
                target.getChildren().add(renderCodeBlock(codeBlock.getLiteral()));
            } else if (node instanceof IndentedCodeBlock codeBlock) {
                target.getChildren().add(renderCodeBlock(codeBlock.getLiteral()));
            } else if (node instanceof BlockQuote blockQuote) {
                VBox quote = new VBox(6);
                quote.getStyleClass().add("news-quote");
                renderBlocks(blockQuote, quote);
                target.getChildren().add(quote);
            } else if (node instanceof BulletList || node instanceof OrderedList) {
                renderList(node, target);
            } else if (node instanceof ThematicBreak) {
                Label divider = new Label();
                divider.getStyleClass().add("news-divider");
                target.getChildren().add(divider);
            } else if (node instanceof HtmlBlock htmlBlock) {
                target.getChildren().add(textBlock(htmlBlock.getLiteral()));
            } else if (node instanceof ListItem) {
                renderBlocks(node, target);
            }
        }
    }

    private void renderList(org.commonmark.node.Node list, VBox target) {
        boolean ordered = list instanceof OrderedList;
        int index = 1;
        for (org.commonmark.node.Node item = list.getFirstChild();
             item != null;
             item = item.getNext()) {
            if (!(item instanceof ListItem)) {
                continue;
            }
            Label marker = new Label(ordered ? index++ + "." : "\u2022");
            marker.getStyleClass().add("news-list-marker");
            VBox itemContent = new VBox(6);
            itemContent.setFillWidth(true);
            renderBlocks(item, itemContent);
            HBox row = new HBox(8, marker, itemContent);
            row.setPadding(new Insets(0, 0, 0, 16));
            HBox.setHgrow(itemContent, Priority.ALWAYS);
            target.getChildren().add(row);
        }
    }

    private TextFlow renderInline(org.commonmark.node.Node parent) {
        return renderInline(parent, 14);
    }

    private TextFlow renderInline(org.commonmark.node.Node parent, double fontSize) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("news-text-flow");
        appendInlineChildren(parent, flow, false, false, false, false, fontSize);
        return flow;
    }

    private void appendInlineChildren(
            org.commonmark.node.Node parent,
            TextFlow flow,
            boolean bold,
            boolean italic,
            boolean code,
            boolean link,
            double fontSize) {
        for (org.commonmark.node.Node node = parent.getFirstChild();
             node != null;
             node = node.getNext()) {
            if (node instanceof org.commonmark.node.Text text) {
                addText(flow, text.getLiteral(), bold, italic, code, link, fontSize);
            } else if (node instanceof Code inlineCode) {
                addText(flow, inlineCode.getLiteral(), bold, italic, true, link, fontSize);
            } else if (node instanceof Emphasis emphasis) {
                appendInlineChildren(emphasis, flow, bold, true, code, link, fontSize);
            } else if (node instanceof StrongEmphasis strong) {
                appendInlineChildren(strong, flow, true, italic, code, link, fontSize);
            } else if (node instanceof Link markdownLink) {
                appendInlineChildren(markdownLink, flow, bold, italic, code, true, fontSize);
            } else if (node instanceof org.commonmark.node.Image markdownImage) {
                addImage(flow, markdownImage.getDestination(), markdownImage.getTitle());
            } else if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
                flow.getChildren().add(new Text("\n"));
            } else if (node instanceof HtmlInline htmlInline) {
                addText(flow, htmlInline.getLiteral(), bold, italic, code, link, fontSize);
            } else if (node.getFirstChild() != null) {
                appendInlineChildren(node, flow, bold, italic, code, link, fontSize);
            }
        }
    }

    private void addText(
            TextFlow flow, String value, boolean bold, boolean italic,
            boolean code, boolean link, double fontSize) {
        Text text = new Text(value);
        FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;
        text.setFont(Font.font(code ? "Monospaced" : "System", weight, posture, fontSize));
        text.setFill(link ? LINK_COLOR : TEXT_COLOR);
        if (link) {
            text.setUnderline(true);
        }
        if (code) {
            text.getStyleClass().add("news-inline-code");
        } else {
            text.getStyleClass().add("news-text");
        }
        flow.getChildren().add(text);
    }

    private void addImage(TextFlow flow, String source, String title) {
        if (source == null || source.isBlank()) {
            return;
        }
        javafx.scene.image.Image image = new javafx.scene.image.Image(source, true);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("news-image");
        if (title != null && !title.isBlank()) {
            imageView.setAccessibleText(title);
        }
        images.add(imageView);
        image.progressProperty().addListener(
                (observable, oldProgress, newProgress) -> resizeImages());
        flow.getChildren().add(imageView);
    }

    private VBox renderCodeBlock(String literal) {
        VBox codeBox = new VBox();
        codeBox.getStyleClass().add("news-code-block");
        TextFlow codeFlow = textBlock(literal);
        codeFlow.getStyleClass().add("news-code-text");
        codeBox.getChildren().add(codeFlow);
        return codeBox;
    }

    private TextFlow textBlock(String value) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("news-text-flow");
        addText(flow, value, false, false, false, false, 14);
        return flow;
    }

    private static double headingFontSize(int level) {
        return switch (level) {
            case 1 -> 28;
            case 2 -> 24;
            case 3 -> 20;
            case 4 -> 18;
            case 5 -> 16;
            default -> 14;
        };
    }

    private void resizeImages() {
        if (content == null) {
            return;
        }
        double width = Math.max(1, scrollPane.getViewportBounds().getWidth() - 48);
        double height = Math.max(1, scrollPane.getViewportBounds().getHeight() - 24);
        for (ImageView imageView : images) {
            double imageWidth = imageView.getImage().getWidth();
            double imageHeight = imageView.getImage().getHeight();
            if (imageWidth > 0 && imageHeight > 0) {
                imageView.setFitWidth(Math.min(width, imageWidth));
                imageView.setFitHeight(Math.min(height, imageHeight));
            }
        }
    }
}
